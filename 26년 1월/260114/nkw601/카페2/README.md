\*\*# 커피점 & 제과점

## 1. 문제 요약

- **문제 핵심 내용:**
  - 1 ~ N 고유번호를 가지는 음료
  - 어떤 음료 제조 <- 가장 먼저 받은 주문에 배치
  - 주문에 있는 모든 음료가 제조되면 주문은 삭제
  - 손님: 주문 취소 가능
    - 주문에 있던 음료는 재배치
  - 가장 급하게 처리해야할 주문 최대 5개 보여줌
    - 남은 음료의 수가 가장 많은 주문
    - 그 중 가장 먼저 받은 주문
- **입력 조건:**
  - order: 20,000번 이하 호출
  - supply: 50,000번 이하 호출
  - cancel: 1,000번 이하 호출
  - getStatus: 5,000번 이하 호출
  - hurry: 10,000번 이하 호출
  - 1 ≤ mID ≤ 1,000,000,000
  - 1 ≤ M ≤ 10
  - 1 ≤ mBeverages[] ≤ N
- **출력 조건:**

1. int order(int mID, int M, int mBeverages[])
   - 남은 주문의 개수
2. int supply(int mBeverage)
   - 음료가 배치된 주문의 ID 반환, 실패하면 -1
3. int cancel(int mID)
   - 취소되기 전 주문에 남은 음료의 개수 반환
   - 이미 전달된 주문의 경우 0 반환
   - 이미 취소된 주문의 경우 -1 반환
4. int getStatus(int mID)
   - 주문 mID의 남은 음료의 개숩 ㅏㄴ환
   - 전달된 경우 0
   - 취소된 경우 -1 반환
5. RESULT hurry()

- 가장 급하게 처리해야하는 주문 최대 5개 담아 RESULT에 반환
- 주문의 개수: RESULT.cnt
- i번째 급하게 처리해야 할 아이디: RESULT.IDs[i-1]

---

## 2. 접근 방식

### 2-0 자료구조

```
RESULT {
  cnt: 급하게 처리해야 할 주문의 개수
  IDs: 급하게 처리해야 할 주문의 ID
}
global idx = 0;
Order {
  int remaining : 남은 음료의 개수
  int idx: 주문 순서
  ArrayList<Integer> beverages: 남은 음료들의 mID 저장
  ArrayList<Integer> madeBev: 만들어진 음료들의 mID 저장
  boolean isCanceled: 취소 여부
  boolean isCompleted: 완료 여부(cnt 0이면 완료로 해도 되긴 할 듯)
}

HashMap<mId, Order> orders
PriorityQueue<CurOrder> hurry: cnt, idx로 구분
```

### 2-1. 생각

- PQ인가? <- 아니었다...
- 클래스 만들어서 관리하면 될 것 같음

### 2-2. 함수 구상

1. void init(int N)

- 카페의 음료 개수: N개(1 ~ N)
- int remain = 0;

2. int order(int mID, int M, int mBeverages[])

- ID가 mID, M개의 음료로 구성된 새로운 주문을 받음
- 남은 주문의 개수 반환

3. int supply(int mBeverage)

- 공급
- 종류가 mBeverage인 음료 1개 제조
- 완료 여부 확인하고, 완료됐으면 주문 개수 --
- 가장 먼저 받은 주문에 배치구나

4. int cancel(int mID)

- 이미 취소됐으면 -1
- 다 만들어졌으면 0
- 취소하고
- 음료 재배치

5. int getStatus(int mID)

- mID 꺼내서 남은 개수 반환
- 제조됐으면 0
- 취소됐으면 -1
-

6. RESULT hurry()

- pq에서 꺼냄
- 상태 일치 여부 확인(HashMap에서)
- 담아서 5개 채워지면 보냄

## 3. 회고

- **배운 점:** 뭔가 잘못됐습니다...
