
# [Pro] 커피점 & 제과점 문제 풀이

## 문제 분석

크게 2가지 기능

1. **도로 추가 (init / add)**
* 양방향 간선 그래프 구축
* 건물 개수 N, 도로 개수 K, 도로 정보(출발, 도착, 거리) 주어짐


2. **최소 만남 거리 계산 (calculate)**
* M개의 커피점과 P개의 제과점이 주어짐
* 커피점 중 하나와 제과점 중 하나를 선택하여 만날 수 있는 최단 거리 계산
* 단, 각자의 이동 거리는 R을 초과할 수 없음
* 커피점과 제과점이 만나지 못하거나 조건 만족 불가 시 -1 반환



## 알고리즘 선정

**다익스트라 (Multi-source Dijkstra) + Lazy Update**

1. **가중치 그래프:** 간선(도로)마다 거리 비용이 다르므로 BFS 불가 -> 다익스트라 선정
2. **Multi-source:**
* 커피점 그룹과 제과점 그룹을 각각 시작점으로 잡아야 함
* PQ에 `(cost, node, type)`을 넣고 동시에 돌림 (type 0: 커피, type 1: 제과)
* 사실상 2개의 다익스트라를 하나의 PQ에서 수행하는 효과


3. **Lazy Update & Pruning (최적화):**
* 큐에 넣을 때 `dist`를 갱신하지 않고, **꺼낼 때(poll)** 최초 방문 여부를 체크하여 갱신 (Lazy)
* 가지치기 조건:
1. `newCost > R`: 제한 반경 초과 시 탐색 중단
2. `newCost > ans`: 이미 찾은 최단 만남 거리보다 멀어지면 탐색 중단
3. 만남 성사 시(`dist[0]`, `dist[1]` 모두 갱신됨): 해당 경로는 더 깊이 탐색 안 함 (`continue`)


## 시간 복잡도

* **init()** X 1 :  인접 리스트 초기화
* **add()** X 2,000 :  단순 추가
* **calculate()** X 100 :
* 일반 다익스트라: 
* R 제한과 ans 가지치기로 인해 실제 탐색 범위는 전체 그래프보다 훨씬 좁음
* 대략  (: R 반경 내 유효 간선 수)



## 자료 구조

```java
// 그래프 간선 정보
static class Edge implements Comparable<Edge> {
    int to;
    int cost;
    int flag;   // 0: 커피점 출발 경로, 1: 제과점 출발 경로

    // 그래프 구성용 생성자
    public Edge(int to, int cost) {
        this.to = to;
        this.cost = cost;
    }

    // PQ 탐색용 생성자
    public Edge(int to, int cost, int flag) {
        this.to = to;
        this.cost = cost;
        this.flag = flag;
    }

    @Override
    public int compareTo(Edge o) {
        return Integer.compare(this.cost, o.cost);
    }
}

static int N;
static List<Edge>[] edges; // 인접 리스트 (공간 복잡도 O(V+E))
static int[][] dist;       // 방문 체크 및 거리 저장 (dist[0][]: 커피, dist[1][]: 제과)

```
