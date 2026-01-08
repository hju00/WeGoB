import java.util.*;

public class UserSolution {
	
	static int n;
	static int trainCnt; // 등록된 기차의 개수(인덱스 관리용)
	static boolean[] visitedTrain; // 기차 방문 체크 (인덱스 기준)
	static Train[] trainList; // 기차 객체 관리 배열
	static boolean[][] adj; // 기차 간 환승 가능 여부 그래프
	static HashMap<Integer, Integer> trainIdMap; // 기차 ID -> 배열 인덱스 매핑

	class Train {
		int id, start, end, gap;
		boolean isRemoved;

		public Train(int id, int start, int end, int gap) {
			this.id = id;
			this.start = start;
			this.end = end;
			this.gap = gap;
			this.isRemoved = false;
		}
		
		// 해당 기차가 target 역에 정차 가능한 지 확인
		public boolean canGo(int target) {
			if(target < start || target > end) return false;
			return (target - start) % gap == 0;
		}
	}
	
	public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
		n = N;
		trainCnt = 0;
		
		// 초기 할당 최대 50 + add 메서드 호출 최대 150
		trainList = new Train[200];
		adj = new boolean[200][200];
		visitedTrain = new boolean[200];
		trainIdMap = new HashMap<>();
		
		for(int i=0; i<K; i++) {
			add(mId[i], sId[i], eId[i], mInterval[i]);
		}
	}
	
	// 두 기차가 겹치는 역이 하나라도 있는지 확인
	private boolean checkIntersection(Train t1, Train t2) {
		// 1. 운행 구간이 겹치는가?
		int start = Math.max(t1.start, t2.start);
		int end = Math.min(t1.end, t2.end);
		if(start > end) return false;
		
		// 2. 정차 구역이 한 곳이라도 겹치는가? -> 최대공약수로 판별하기
		int gcd = getGCD(t1.gap, t2.gap);
		if((Math.abs(t1.start - t2.start) % gcd != 0)) return false;
		
		// 3. 두 gap의 최소공배수까지가 탐색 지점. 이 구간을 넘어가도 만나는 역이 없다면 더 찾아볼 필요도 없음.
		int lcm = (t1.gap * t2.gap) / gcd;
		int limit = Math.min(end, start + lcm);
		
		// 4. 시작점 보정
		int offset = (start - t1.start) % t1.gap;
		if(offset != 0) {
			start += (t1.gap - offset);
		}
		
		// 5. 보정된 시작점이 범위를 벗어난 경우도 false
		if(start > end) return false;
		
		// 6. LCM 시뮬레이션
		for(int i=start; i<=limit; i+=t1.gap) {
			if((i - t2.start) % t2.gap == 0) return true;
		}
		return false;
	}
	
	private int getGCD(int a, int b) {
		while(b != 0) {
			int temp = a % b;
			a = b;
			b = temp;
		}
		
		return a;
	}

	public void add(int mId, int sId, int eId, int mInterval) {
		Train t = new Train(mId, sId, eId, mInterval);
		int idx = trainCnt++; // 현재 기차의 내부 인덱스 부여
		
		trainIdMap.put(mId, idx);
		trainList[idx] = t;
		
		for(int i=0; i<idx; i++) {
			if(trainList[i].isRemoved) continue;
			if(checkIntersection(t, trainList[i])) {
				adj[idx][i] = true;
				adj[i][idx] = true;
			}
		}
	}

	public void remove(int mId) {
		// 맵을 통해 인덱스를 찾고 삭제 플래그 처리
		if(trainIdMap.containsKey(mId)) {
			int idx = trainIdMap.get(mId);
			trainList[idx].isRemoved = true;
			trainIdMap.remove(mId);
		}
	}

	public int calculate(int sId, int eId) {
		// 방문 배열 초기화
		Arrays.fill(visitedTrain, false);
		
		// 큐에는 {기차 인덱스, 환승 횟수} 저장
		Queue<int[]> q = new ArrayDeque<>();
		
		// 1. 출발역(sId)을 지나는 모든 기차 확인 (초기 탑승)
		for(int i=0; i<trainCnt; i++) {
			Train t = trainList[i];
			if(t.isRemoved) continue;
			
			if(t.canGo(sId)) {
				if(t.canGo(eId)) return 0;
				visitedTrain[i] = true;
				q.add(new int[] {i, 0});
			}
		}
		
		// 2. 기차 중심 BFS 탐색
		while(!q.isEmpty()) {
			int[] cur = q.poll();
			int curIdx = cur[0];
			int cnt = cur[1];
			
			for(int i=0; i<trainCnt; i++) {
				// 환승 안되거나, 이미 방문했거나
				if(!adj[curIdx][i] || visitedTrain[i]) continue;
				
				Train nextTrain = trainList[i];
				if(nextTrain.isRemoved) continue;
				
				if(nextTrain.canGo(eId)) return cnt + 1;
				
				visitedTrain[i] = true;
				q.add(new int[] {i, cnt+1});
			}
		}
		
		return -1;
	}

}