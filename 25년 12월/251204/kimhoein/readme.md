# [pro] 직선열차 문제 풀이

## 문제 분석

열차 역  20 ~ 100,000
열차 개수 3~50
열차 id 1 ~ 1,000,000,000 -> hashmap으로 관리
시작역 종착역 을 무조건 지나는 조건
열차역 간격 -> 등차수열

add
새열차 추가
hashmap으로 추가 관리

remove
열차 제거
hashmap remove 
만약 제거 회수가 많았으면 고민 했겠지만 그렇지 않아서 굳이 그러지 않음

calulate
sid -> eid로 가는데 최소 환승 개수

만나는지 만나지 않는지 판단 함수 하나 추가해서 판단 해주고
판단에 따라서 열차를 환승
도착 하면 환승 개수 반환
못하면 -1 반환

1.

## 알고리즘 선정
열차 는 hashmap 관리 <br>
그냥 역 기준으로 bfs는 무리 200개인 기차로 틀었다<br>
다만 문제 사항은 gcd 이용해서 특해 일반해 구하고 만나는지 만나지 않는지 판단을 해보려고 했으나
구현 난이도 이슈로 만들지 못함

그래서 그렇게 하지 않고 만나는지 만나지 않는지 판단은 직접 돌리면서 판단


## 시간 복잡도
- init() X 1
- add() X 200 = 200 X N
- remove() X 100 = 1 X 100 = 100 
- calculate() 이부분이 계산이 안됨

## 자료 구조
   static class Tomas
	{
		int id;
		int start;
		int end;
		int distance;
		int transfer;
		Tomas(int id, int start, int end, int distance)
		{
			this.id = id;
			this.start = start;
			this.end = end;
			this.distance = distance;
		}
	}
	
	static class Node implements {
		int cur, cnt;
	}
	
	static int N;
	static int K;
	HashMap<Integer, Tomas> hash = new HashMap<>();