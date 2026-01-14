# 1) 문제 분석
   -	건물 0 ~ N-1 (최대 10,000), 도로는 양방향 + 가중치(1~1000).
   -	add()로 도로가 계속 추가됨(삭제 없음) → 그래프는 증가만.
   -	calculate(M, mCoffee, P, mBakery, R)에서 해야 할 일:
   -	커피점 집합, 제과점 집합이 주어짐.
   -	커피점/제과점 건물은 주택이 아님(후보에서 제외).
   -	어떤 주택 v
   -	v의 가장 가까운 커피점까지 거리 ≤ R
   -	v의 가장 가까운 제과점까지 거리 ≤ R
   -	위 조건을 만족하는 주택 중
   -	(커피까지 최단거리 + 제과점까지 최단거리) 최소값 반환
-	없으면 -1.
  - 양수 가중치 최단거리찾기 -> 다익스트라
  - 커피점에서 한번 제과점에서 한번 2번 사용하면 되겠다



# 2) 알고리즘 정리

### 그래프 구조
- List<int[]>[] graph
- graph[u]에 {v, w} 저장 (양방향)

### calculate 흐름
1.	주택 제외 처리
- coffeeMark[], bakeryMark[] + order 스탬프 방식
- 매 calculate마다 order++
    커피/제과점 노드를 order로 마킹
- coffeeMark[i]==order 또는 bakeryMark[i]==order면 후보 제외
- boolean 배열 매번 초기화 안 해도 돼서 빠름

2. 다익스트라 2번

	- 	distC = dijkstra(mCoffee, M, R)
	- 	distB = dijkstra(mBakery, P, R)

dijkstra()는:
-	시작점들을 전부 dist=0으로 PQ에 넣음
-	PQ에서 꺼낸 거리 d가 R 넘어가면 break  → R 이내만 확장하는 컷팅이라 계산량 줄어듦
-	relax 할 때도 nd <= R일 때만 갱신/푸시

3. 모든 노드 i 순회하며

	-	i가 커피/제과점이면 스킵
	-	distC[i] <= R && distB[i] <= R인 주택만
	-	ans = min(ans, distC[i] + distB[i])
	-	없으면 -1



# 3) 시간 복잡도

- 마킹: O(M+P)
- 다익스트라 2번: 2 * O((N+E) log N)  
- 마지막 for문 전체 스캔: O(N)


O((N+E)logN)