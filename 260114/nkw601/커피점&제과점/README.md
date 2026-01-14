**# 커피점 & 제과점

## 1. 문제 요약
- **문제 핵심 내용:** 커피점 ~ 주택 거리 <= R, 제과점 ~ 주택 거리 <= R인 주택 중, 각 거리 합의 최솟값
- **입력 조건:** 
  - 6 <= N <= 10,000 (건물의 개수)
  - 6 <= K <= 30,000 (도로의 개수)
  - 0 ≤ sBuilding[i] < N (도로 i와 연결된 건물)
  - 0 ≤ eBuilding[i] < N (도로 i와 연결된 건물)
  - 1 ≤ mDistance[i] ≤ 1,000 (도로 i의 거리)
- **출력 조건:**
1) int calculate(int M, int mCoffee[], int P, int mBakery[], int R)
- 커피점 ~ 주택 거리 <= R, 제과점 ~ 주택 거리 <= R인 주택 중, 각 거리 합의 최솟값
- 거리가 R 이하인 주택 없으면, -1

---

## 2. 접근 방식

### 2-0 자료구조
- PriorityQueue pq: 다익이니까... 제과점용, 카페용 두 개
- ArrayLisv<int[]>[] adjList: 인접 리스트 구현 용(add), [id, dist] 담음
- int[] distC, distB: 거리 배열: 최소 거리 담기
- 카페랑 베이커리 구분용?
### 2-1. 생각
- 다익스트라 아닌가?
- 다익스트리 두 번 돌리면 될 것 같다는 생각 <- 실제로 시험장에서 답이 나오긴 했음
- 근데 시간초과가 남 -> 최적화...
### 2-2. 함수 정리
1) void init(int N, int K, int sBuilding[], int eBuilding[], int mDistance[])
- N개의 건물(0~N-1 ID)
- K개의 양방향 도로 정보(도로마다 연결된 2개의 건물, 거리)
  - 2개의 건물을 연결하는 도로는 1개만 주어짐

2) void add(int sBuilding, int eBuilding, int mDistance)
- sBuilding 건물과 eBuilding 건물을 연결하는 양방향 도로를 추가
- init()에 없던 건물 주어지지 X
- 도로 중복 X
- sBuilding != eBuilding

3) int calculate(int M, int mCoffee[], int P, int mBakery[], int R)
- 커피점 ~ 주택 거리 <= R, 제과점 ~ 주택 거리 <= R인 주택 중, 각 거리 합의 최솟값


### 2-3. 함수 구상
1) init
초기화 하기...
2) add
- adjList에 추가

3) calculate
- isBakery, isCafe 채우기
- INF로 dist 배열들 채우기
- 다익스트라 돌리기(카페 ~ 집)
- 다익스트라 돌리기(제과점 ~ 집)
  - R 넘으면 계산 그만하기(더 봐도 의미 없으니까...)
  - 플래그 처리 해야하나?
- 합해서 최솟값 계산하기

## 3. 회고
- **배운 점:** 다익스트라 너무 오랜만이라 다 까먹어버렸어요...
- 근데 아무래도 이 때 B형 땄어야 했던 것 같아요