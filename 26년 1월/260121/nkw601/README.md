**# 던전 탈출

## 1. 문제 요약
- **문제 핵심 내용:**
  - 게이트1 => 게이트2(상하좌우, 기둥 이동 불가)
  - 탈출 최소 시간
  - 초기 기사 체력: mMaxStamina
  - 1칸 움직임 -> 체력 1 감소, 시간 1 증가
  - 체력이 0이 되면 움직일 수 없음
  - 닫혀있는 게이트 만나면 체력 mMaxStamina
- **입력 조건:** 
  - 
- **출력 조건:**
1) int getMinTime(int mStartGateID, int mEndGateID):
- mStartGatdID에서 출발, mEndGateID로 이동한 최단 시간
- 도착 못하면 -1


---

## 2. 접근 방식
- 그냥 다익스트라일 리가 없는데 애초에 가중치가...
- 출발게이트 -> 게이트1 -> ... -> 게이트n -> 도착!
- 체력 닿는 범위 안에 게이트 없으면 안되기 때문
- 가장 가까운 게이트까지의 거리: 가중치

### 2-0 자료구조
게이트 관련
```
Gate {
    int ID
    int r, c
    boolean isAlive
}

HashMap<Integer, Gate> gates;
int[][] gateDist; // 게이트끼리의 거리 리스트: 200개니까 여기에 저장해도 될듯 : 1 ~ 200
```

PriorityQueue<> pq : 다익스트라용
### 2-1. 생각
- getMinTime을 여러 번 하고
- 게이트들 사이의 거리는 변하지 않으니까
- 거리를 저장해서 가중치로 쓰기 

### 2-2. 함수 구상
1) void init(int N, int mMaxStamina, int mMap[][])
- N: 던전 지도의 크기
- mMaxStamina: 최대 체력
- mMap: 던전 지도(0 길 1 기둥)

2) void addGate(int mGateID, int mRow, int mCol)
- ID가 mGate인 게이트를 mRow, mCol에 추가
- map에 -mGateID 저장
- 다른 gate들과의 거리 조사(bfs)

3) void removeGate(int mGateID)
- mGateID 게이트 제거
- isAlive = false

4) int getMinTime(int mStartGateID, int mEndGateID)
- pq에 넣어서 다익
- 넣는 것: 게이트의 id
- 빼서 확인 -> 나와 연결된 게이트: 1~200번 게이트

## 3. 회고
- **배운 점:** 내가 가중치 만들어서 다익 넣어주기... bfs하는 법이 헷갈려요 비상입니다......