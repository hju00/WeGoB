# [pro] 직선열차 문제 풀이

## 문제 분석

크게 3가지 기능

1. 열차 추가
- 열차 id, 시작역, 종착역, 정차 역의 간격이 주어짐
- 이미 존재하는 열차의 id는 주어지지 않음

2. 열차 제거
- mId의 열차 제거
- 존재하지 않는 열차의 id는 주어지지 않음

3. 최소 환승 횟수 계산
- sId 에서 eId 로 가는 필요한 최소 환승 횟수 반환
- 갈 수 있는 방법이 없다면 -1 반환
- sId와 eId가 같은 경우는 주어지지 않음

## 알고리즘 선정
열차 DB는 확정 <br>
cost가 존재하지 않고 최소 경로?? 그냥 BFS 아닌가?<br>
근데 N이 최대 10만이고 열차가 최대 200대<br>
딱봐도 역으로 BFS돌리면 터지고 기차로 돌리란 소리죠


## 시간 복잡도
- init() X 1
- add() X 200 = 200 X 200 X a = 40,000 X a
- remove() X 100 = 1 X 100 = 100 (제외해도 무방)
- calculate() X 50 = O(V^2) X 50 = 40,000 X 50 = 2,000,000

## 자료 구조
    static class Train {
		int s, e, interval;
		HashSet<Integer> adj;
	}
	
	static class Node implements {
		int cur, cnt;
	}
	
	static int N;
	static HashMap<Integer, Train> trainDB;