# [Pro] 고대 통신망 문제 풀이

## 1. 문제 분석

- **상황:** cost가 존재하는 최단 경로 문제.
- **우선순위:** 
  1. 최단 경로 (총 이동 거리가 최소가 되는 경로)
  2. 총 거리가 같다면, 그 경로에 포함된 가장 긴 도로 길이(`maxLen`)가 더 작은 경로
- **핵심 아이디어:** 최단 경로가 아니면 두 번째 조건은 고려할 필요가 없다. 
  - 쳐낼 수 있는 부분: 
    1. 기존 다익스트라보다 거리가 긴 경로
    2. 길이가 긴 경로 쳐내기 (최단 거리는 같으나 `maxLen`이 더 긴 경로)

---

## 2. 시간 복잡도 분석 및 고찰

- 주어진 조건의 호출 횟수:
  - `add()` $\le 14,000$
  - `remove()` $\le 1,000$
  - `calculate()` $\le 5,000$ (보통 다익스트라 문제의 경우 함수 호출 횟수가 500 이하인데 5,000인 이유가 있음)

- **초기 생각 (시간 초과 이슈):**
  - 만약 `calculate()` 매 호출마다 다익스트라를 돌리게 된다면?
  - 간선의 개수 $E \approx 24,000$
  - $5,000 \times 24,000 \times \log 5,000 \approx 120,000,000 \times 3.6$ 이상
  - 이미 루프가 1억을 넘어가므로 시간 초과(TLE)에 걸리게 된다.
  - "간선의 추가, 삭제가 빈번하게 일어나는데 다익 결과를 전역 변수로 관리하는 게 의미가 있나?"라는 고민 발생.

- **접근 방식 변경 (최적화):**
  - **`add()` 최적화:** 간선이 추가될 때마다 전체를 재계산할 필요 없이, 새로 추가된 간선의 시작점(도착지)에서부터 주변으로 갱신될 때만 부분 다익스트라(`update()`)를 돌리면 됨.
  - **`remove()` 최적화:** 
    1. 간선 삭제는 `edgeDB`에서 Lazy Delete 처리. 
    2. 단, 삭제된 간선이 목적지에 도달하는 핵심 메인 경로(가장 효율적인 경로)였다면 그 최단 경로가 깨졌기 때문에 **쌩 다익스트라를 돌려 전체를 재계산** 해야 함. (`dijkstra()`)
    3. 이 때문에 함수 호출 횟수가 전체를 다시 계산해도 시간 제한 안에 들어올 수 있도록 최대 수치가 1,000회 뿐이구나라고 유추 가능.
  - **`calculate()` 최적화:** 이미 전역변수(`dist`, `maxDist`)로 도달할 수 있는 모든 도시들의 최적값이 관리되고 있으므로 $O(1)$의 시간복잡도로 결과를 리턴하면 됨.

---

## 3. 핵심 알고리즘 및 편의 구상

### (1) Custom 자료 구조 (우선순위 적용)

```java
static class Edge implements Comparable<Edge> {
    int id, from, to, cost, maxLen;
    boolean isValid = true; 
    
    // ... 생성자 ...
    
    @Override
    public int compareTo(Edge o) {
        if(this.cost == o.cost) 
            return Integer.compare(this.maxLen, o.maxLen);
        return Integer.compare(this.cost, o.cost);
    }
}
```
- **Lazy Delete:** `HashSet` 이나 `ArrayList` 에서 인스턴스를 찾아서 지우지 않고 `isValid` 플래그를 두어 $O(1)$로 무효화만 함.
- **정렬 우선순위:** `cost`를 1순위, `maxLen`을 2순위로 오름차순.

### (2) 국부적 업데이트 로직 (add)

```java
// 새로 생긴 길의 조건이 도착지(eCity)의 기존 기록보다 더 좋거나 같고 maxLen이 더 작을 때만
if (newCost < dist[eCity] || (newCost == dist[eCity] && newMaxLen < maxDist[eCity])) {
    dist[eCity] = newCost;
    maxDist[eCity] = newMaxLen;
    update(eCity);  // 해당 지점부터 파급 효과 분석
}
```

### (3) 전체 재구성 로직 (remove)

```java
if (dist[e.from] != INF) {
    int costThrough = dist[e.from] + e.cost;
    int maxLenThrough = Math.max(maxDist[e.from], e.cost);
    
    // 이 간선이 특정 도시에 도달하는 메인 경로로 쓰였을 확률이 높으므로 '전체 다익스트라 재계산'
    if (dist[e.to] == costThrough && maxDist[e.to] == maxLenThrough) {
        dijkstra();
    }
}
```
