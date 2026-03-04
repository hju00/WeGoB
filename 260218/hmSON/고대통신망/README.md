# [pro] 고대통신망 문제 풀이

## 문제 분석

N(≤ 5,000)개의 도시와 단방향 도로(초기 K ≤ 10,000, 이후 add ≤ 14,000 / remove ≤ 1,000)가 주어진다. 수도 `mCapital`에서 목적지 `mCity`로 전령이 이동할 때,

- **1차 목표**: 총 이동 거리(가중치 합)가 **최단**인 경로를 따른다.
- **2차 목표**: 그 “최단 경로들 중에서”, 지나간 도로들 중 **가장 긴 도로의 길이(max edge)** 를 **최소화**한다.

도달 불가능하면 `-1`.

<br>

## 문제에서 요구하는 쿼리 분석

### 1. `init(N, mCapital, K, ...)`
- 초기 그래프 구성.
- 도로 ID가 최대 10^9 → ID로 직접 배열 관리 불가 → `HashMap` 요구.
- 이후 add/remove/calculate가 반복되므로, **동적 업데이트** 전략 요구.

### 2. `add(mId, sCity, eCity, mDistance)`
- 단방향 간선 추가 (중복 (s,e) 없음, ID 중복 없음).
- 핵심: `calculate()`가 최대 5000번이므로 매번 전체 경로를 재계산하면 많은 시간이 소요된다.
- 따라서 **추가 간선이 “거리/최장간선”을 개선할 수 있을 때만 국소적으로 갱신**하는 방식을 선택한다.

### 3. `remove(mId)`
- 간선 제거는 최단거리 구조를 깨뜨릴 수 있다.
- remove는 최대 1000번으로 상대적으로 적으므로, **지연 삭제 + 필요한 경우(`calculate()`) 전체 rebuild**가 합리적.

### 4. `calculate(mCity)`
- 요청 시점 그래프 기준으로 `(최단거리, 그 최단거리 경로들 중 최장간선 최소)` 값을 반환.
- remove가 있었던 구간이라면 이전 dist 정보가 무효일 수 있으므로 **dirty 체크 후 재계산(`rebuild()`)**.

<br><br>

> ## 자료구조 및 알고리즘 설계

### 1. 다익스트라
이 문제는 “총 거리 최단”이 1순위, “최장 간선 최소”가 2순위이므로 상태를 다음처럼 정의한다.

- 각 도시 v에 대해 최적 상태를 `(dist[v], longest[v])`로 관리  
  - `dist[v]`: 수도→v 최소 총거리  
  - `longest[v]`: 그 최소 총거리 경로들 중 경로 내 최장 간선의 최소값

비교 코드:

- `(c1 < c2) || (c1 == c2 && l1 < l2)` 이면 더 우선 순위가 높은 경로이다.

우선순위 큐도 `(cost, longestWay)` 오름차순으로 정렬해서 **다익스트라를 그대로 확장**한다.

---

### 2. 그래프 및 도로 ID 관리
- `List<Way>[] graph`: 인접 리스트
- `HashMap<Integer, Way> waysMap`: `mId → Way`로 매핑해서 remove 시 O(1) 접근
- `Way` 객체에 `removed` 플래그를 둬서 **지연 삭제(Lazy Deletion)** 처리
  - remove 호출 시 리스트에서 삭제하지 않고 표시만 해둠
  - 다익스트라 탐색 중 `if(next.removed) continue;`로 무시

---

### 3. 국소 증가(Incremental Relaxation) 최적화: add 시 부분 다익스트라
`add()`는 “새 간선 하나”가 기존 최단거리 구조를 개선하는 경우에만 영향을 준다.

코드의 핵심 로직:
1) `dist[sCity] == INF`면 수도에서 출발도시까지 못 가므로 새 간선은 영향 없음  
2) 새 간선을 통해 도착 도시의 후보 상태 계산  
   - `candCost = dist[sCity] + mDistance`
   - `candLongest = max(longest[sCity], mDistance)`
3) 이 후보가 `(dist[eCity], longest[eCity])`를 개선한다면,
   - `dist[eCity]`, `longest[eCity]` 갱신 후
   - `eCity` 하나만 PQ에 넣고 **부분 다익스트라** 수행 (`dijkstra()`)

즉, 새 간선이 만들어낸 개선이 **전파(relaxation)** 되는 구간만 다시 탐색한다는 점에서
전체 rebuild 대비 크게 절약된다.

---

### 4. remove에서 dirty 처리 후 calculate에서 rebuild
간선 삭제는 기존 최단경로가 “끊기거나 우회해야 하는 상황”을 만들 수 있어,
영향 범위를 `add()`처럼 국소적으로 제한하기 어렵다.

그래서:
- `remove()` 시 `dirty = true`
- `calculate()` 시 dirty면 `rebuild()`로 전체 다익스트라 재수행 후 dirty 해제

remove 횟수가 1000 이하이고, calculate는 5000번이지만 **remove 직후에만 rebuild**하므로,
수도부터 시작하는 전체 다익스트라는 1000회 이하로만 실행되게끔 통제할 수 있다.

<br>

## 구현 핵심 포인트

- `Node(city, cost, longestWay)`를 PQ에 넣고,
  - 이미 더 좋은 상태가 기록돼 있으면 스킵:
    - `cur.cost > dist[cur.city]` 이면 스킵
    - 같은 cost라도 `cur.longestWay > longest[cur.city]` 이면 스킵  
- 가지 치기:
  - `newCost = cur.cost + next.dist`
  - `newLongest = max(cur.longestWay, next.dist)`
  - `better(newCost, newLongest, dist[next], longest[next])`면 갱신

<br>

## 시간 복잡도

- `rebuild()` (전체 다익스트라): `O((N + M) log N)`  
  - N ≤ 5000, M는 초기+추가 합쳐도 대략 2~3만 수준
- `add()`:
  - 대부분은 조건에서 컷되거나(도달 불가/개선 없음)  
  - 개선 시 “영향 구간만” 국소 갱신 다익스트라 → 평균적으로 훨씬 작게 동작
- `remove()`:
  - O(1) (플래그만 세팅) + dirty 표시
- `calculate()`:
  - dirty가 아니면 O(1)
  - dirty면 rebuild 비용 1회