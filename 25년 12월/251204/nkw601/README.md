# 🚅 직선 열차

## 1. 문제 요약
- **문제 핵심 내용:** 출발 역 -> 도착 역까지의 최소 환승 횟수
- **입력 조건:** N개의 기차 역(1 ~ N ID), K개의 왕복 열차 정보
- **출력 조건:**
1) void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]): N 기차역, K 기차 정보, mId 열차 id, sId 시작역, eId 종착역, mInterval 역 간격
2) void add(int mId, int sId, int eId, int mInterval)
- 시작역이 sId, 종착역이 eId, 역 간격이 mInterval인 mId 열차 추가 
- 종착역 = 시작역 + 정차 간격의 배수
- 종착역 != 시작역
3) void remove(int mId)
- ID가 mId인 열차 지우기
- 지울 수 없는 경우 X
4) int calculate(int sId, int eId)
- sId 역에서 eId 역으로 가는데 필요한 최소 환승 횟수
- 환승 없이 갈 수 있으면 횟수는 0
- 이동할 수 있는 방법 없으면 -1
- sId != eId

---

## 2. 접근 방식

### 2-0 자료구조

class Train {
    int mId, int start, int end, int interval, boolean isAlive
    boolean willStop(int stationId) // 특정 역 정차 여부 return
}
boolean[][] connected; // connected[i][j] == true 이면 i ↔ j 열차 환승 가능
Map<Integer, Integer> id2idx; // mId 넣으면 idx 알려주는 map

### 2-1. 생각
- 다익스트라 아닌가
- 환승 수를 구하는거면 가중치 1 고정 아닌가...
- 다익스트라 아니고 그냥 BFS인듯... B형에도 BFS가 나오는구나...
- 근데 환승 횟수랑 역 간격은 무슨 상관이 있을까
- 아!!! 같은 역에 서는지 확인하려면 간격이 필요하구나...
- N <= 100,000이고, K <= 50이니까 K 기준으로 확인하기...

### 2-2. 해결 흐름(단계별 로직)
1) void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[])
- 기본 정보로 초기화
2) void add(int mId, int sId, int eId, int mInterval):
- 새로운 열차 추가
- 저장된 열차들과 환승 가능한지 확인해서 인접행렬 채우기
- 2-1) boolean canTransfer(Train t1, Train t2) 
- 환승 가능 여부를 확인하는 함수
- 최소공배수...? 근데 시작점이 달라서 순수 최소공배수로 안되는데...?
- 아 수학이 너무 싫다...
- 지피티 찬스를 사용했습니다...
- 최대공약수로 실제 만나는지 확인
- 최소공배수로 몇 분마다 만나는지 확인
- 최소공배수 이후로는 반복이므로 -> 최소공배수만큼 돌면서 동시에 만나는지 확인
3) void remove(int mId):
- isAlive false로 바꾸기 -> 인접행렬은 나중에 계산할 때 확인

4) int calculate(int sId, int eId):
- isAlive 확인하고, bfs 돌리기

---

## 3. 회고
- **배운 점:** 코테를 풀려면 수학을 알아야 하는구나... 