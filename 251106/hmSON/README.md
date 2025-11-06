# SWEA_25005 (Pro) : 전기차여행

## 문제 요약

N개의 도시 : 각 도시의 ID, 도시 내 전기차 충전소의 단위 시간 당 충전량<br>
단방향 간선 : 해당 도로의 ID, 소요 시간, 전력 소모량<br>
M개 도시에 전염병 발생. 단, 도시마다 전염병 발생 시기가 다를 수 있다.<br>
전염병이 존재하는 도시로부터 각 도로의 소요 시간이 경과하면 인접한 다음 도시로 전염된다.<br><br>
최대 충전 용량이 B인 전기차를 이용해 출발 도시 -> 도착 도시까지 이동하는 데 필요한 최단 시간을 구할 것.<br> 
초기에 배터리는 완충된 상태이다.<br>
전염병이 퍼진 도시는 접근 불가. 동시에 도착해도 안된다.<br>
충전중 전염병을 만나면 이동 불가<br><br>

## 풀이 전 설계

2번의 다익스트라가 요구될 것이다.<br>
전염병이 각 도시까지 퍼지는 시간을 구하는 다익스트라 1회,<br>
그리고 전기차가 전염병을 피해 목표 도시까지 이동하는 다익스트라 1회.<br><br>
단, 전기차는 이동시 충전량을 소모한다. 또, 각 도시마다 단위 시간당 충전량이 다르다.<br>
따라서 충전량 * 최단 시간을 다루는 2차원 배열 메모이제이션을 활용하여 최단 거리를 구해야 할 것이다.
<br><br>
또한 도로의 개수는 최대 4000 + 3000 = 7000이나 각 도로의 번호가 최대 10억까지 주어질 수 있으므로 도로의 정보는 Hash로 관리해야 할 것으로 보인다.<br><br>

## 자료구조

이 문제에서는 도로의 동적 추가 및 제거, 전염병 전파 시간 관리, 전기차의 상태(남은 배터리, 현재 시간) 등을 동시에 처리해야 한다.<br>
이를 위해 다음과 같은 자료구조를 사용한다.<br><br>
| 자료구조                   | 타입                       | 설명                                          |
| ---------------------- | ------------------------ | ------------------------------------------- |
| `graph`                | `List<Node>[]`           | 각 도시에서 출발하는 도로들의 인접 리스트. (단방향 그래프)          |
| `nodes`                | `HashMap<Integer, Node>` | 도로 ID → 도로 객체 매핑. `add()` 및 `remove()` 시 활용 |
| `distVirus`            | `int[]`                  | 각 도시별 전염병 도달 최소 시간. 전염병 BFS(다익스트라) 결과 저장    |
| `amountCharge`         | `int[]`                  | 각 도시의 단위 충전 속도 (시간당 충전량)                    |
| `distCar`              | `int[v][maxCharge+1]`    | 전기차의 “도시 + 잔여 전력” 상태별 최소 소요 시간              |
| `PriorityQueue<Car>`   | 최소 힙                     | (도시, 경과 시간, 잔여 전력) 기반 다익스트라 우선순위 큐          |
| `PriorityQueue<Virus>` | 최소 힙                     | (도시, 전염 시각) 기반 전염병 전파 시뮬레이션용 우선순위 큐     |

## 주요 클래스

```java
static class Node {
    int to, time, power;
    boolean removed;
    
    public Node(int to, int time, int power) {
        this.to = to;       // 도착 도시
        this.time = time;   // 이동 소요 시간
        this.power = power; // 이동 시 전력 소모량
        removed = false;    // remove() 호출 시 true로 설정
    }
}
```
도로 정보를 표현. 실제 삭제는 수행하지 않고,
removed = true 플래그로 지연 삭제 처리.<br>

```java
static class Car implements Comparable<Car> {
    int to, time, power;  // 현재 도시, 경과 시간, 남은 전력
    public int compareTo(Car o) { return time - o.time; }
}
```
전기차의 상태를 나타내며,
“현재 시간”이 작은 순으로 우선순위 큐에서 처리된다.<br>

```java
static class Virus implements Comparable<Virus> {
    int to, time;  // 감염된 도시와 해당 시각
    public int compareTo(Virus o) { return time - o.time; }
}
```
전염병의 전파를 도시 단위로 시뮬레이션하기 위한 클래스<br><br>

## 함수 설명

### 1️⃣ init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[])

그래프 초기화 및 도로 정보 등록 함수

입력된 도시 수(N)와 각 도시의 충전속도(mCharge) 저장

각 도로의 정보를 graph와 nodes에 등록

전염병 거리 배열(distVirus) 초기화
```java
public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
    v = N;
    amountCharge = mCharge;
    graph = new ArrayList[v];
    distVirus = new int[v];
    nodes = new HashMap<>();

    for (int i = 0; i < v; i++) graph[i] = new ArrayList<>();

    for (int i = 0; i < K; i++) {
        Node node = new Node(eCity[i], mTime[i], mPower[i]);
        nodes.put(mId[i], node);
        graph[sCity[i]].add(node);
    }
}
```

### 2️⃣ add(int mId, int sCity, int eCity, int mTime, int mPower)

새로운 단방향 도로를 추가한다.

도로 ID 중복 없이 nodes와 graph에 추가

removed 여부는 기본적으로 false
```java
public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
    Node newNode = new Node(eCity, mTime, mPower);
    nodes.put(mId, newNode);
    graph[sCity].add(newNode);
}
```

### 3️⃣ remove(int mId)

도로를 제거한다. 실제 삭제 대신 “비활성화 플래그”만 변경하여 효율적으로 처리한다.

```java
public void remove(int mId) {
    Node target = nodes.get(mId);
    target.removed = true;
}
```

### cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[])

전염병이 퍼지는 시간 계산 + 전기차의 최소 시간 경로 계산
두 개의 다익스트라를 순차적으로 수행한다.

dijkVirus()로 각 도시의 감염 도달 시각 계산

dijkCar()로 전염병을 피하면서 최소 시간 이동 경로 계산
```java
public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
    dijkVirus(M, mCity, mTime);
    return dijkCar(B, sCity, eCity);
}
```

### 5️⃣ dijkVirus(int m, int city[], int time[])

전염병 전파 시뮬레이션용 다익스트라

초기 감염 도시와 시간을 우선순위 큐에 넣고 시작

도로의 소요 시간(next.time)이 지나면 다음 도시로 감염이 퍼진다고 가정

distVirus[to]는 해당 도시에 전염병이 도달하는 최소 시각을 의미
```java
public void dijkVirus(int m, int city[], int time[]) {
    Arrays.fill(distVirus, INF);
    PriorityQueue<Virus> q = new PriorityQueue<>();

    for (int i = 0; i < m; i++) {
        distVirus[city[i]] = time[i];
        q.add(new Virus(city[i], time[i]));
    }

    while (!q.isEmpty()) {
        Virus cur = q.poll();
        if (distVirus[cur.to] < cur.time) continue;

        for (Node next : graph[cur.to]) {
            if (next.removed) continue;
            int newTime = cur.time + next.time;
            if (distVirus[next.to] <= newTime) continue;

            distVirus[next.to] = newTime;
            q.add(new Virus(next.to, newTime));
        }
    }
}
```

### 6️⃣ dijkCar(int maxCharged, int start, int end)

전염병 시간(distVirus)을 고려하며 전기차의 이동 최단 시간을 구한다.
상태: (도시, 현재 남은 배터리)
행동:

① 충전: 시간 +1, 배터리 +충전속도

② 이동: 시간 +도로시간, 배터리 -소모량
감염 도시에 도착하거나 감염 시각과 동일한 경우는 불가능.

목표 도시로 도착한 경우 최단 시간을, 그렇지 못한 경우 -1 반환

```java
public int dijkCar(int maxCharged, int start, int end) {
    int[][] distCar = new int[v][maxCharged + 1];
    for (int i = 0; i < v; i++) Arrays.fill(distCar[i], INF);

    PriorityQueue<Car> q = new PriorityQueue<>();
    distCar[start][maxCharged] = 0;
    q.add(new Car(start, 0, maxCharged));

    while (!q.isEmpty()) {
        Car cur = q.poll();
        if (distCar[cur.to][cur.power] < cur.time || cur.time >= distVirus[cur.to]) continue;

        if (cur.to == end) return cur.time;

        // (1) 충전
        if (cur.power < maxCharged) {
            int newCharge = Math.min(maxCharged, cur.power + amountCharge[cur.to]);
            int newTime = cur.time + 1;
            if (newTime < distVirus[cur.to] && newTime < distCar[cur.to][newCharge]) {
                distCar[cur.to][newCharge] = newTime;
                q.add(new Car(cur.to, newTime, newCharge));
            }
        }

        // (2) 이동
        for (Node next : graph[cur.to]) {
            if (next.removed) continue;
            int nextTime = cur.time + next.time;
            if (distVirus[next.to] <= nextTime) continue;

            int nextCharge = cur.power - next.power;
            if (nextCharge < 0 || nextTime >= distCar[next.to][nextCharge]) continue;

            distCar[next.to][nextCharge] = nextTime;
            q.add(new Car(next.to, nextTime, nextCharge));
        }
    }

    return -1;
}

```