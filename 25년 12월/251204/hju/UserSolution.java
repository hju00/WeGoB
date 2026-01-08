import java.util.*;

class UserSolution {
	
	static class Train {
		int s, e, interval;
		HashSet<Integer> adj;

		public Train(int s, int e, int interval) {
			super();
			this.s = s;
			this.e = e;
			this.interval = interval;
			this.adj = new HashSet<>();
		}
		
		public boolean contains(int station) {
			return station >= s && station <= e && (station - s) % interval == 0;
		}
	}
	
	// 다익스트라 인줄 알고 Comparable 써놓은 흔적
	static class Node implements Comparable<Node>{
		int cur, cnt;

		public Node(int cur, int cnt) {
			super();
			this.cur = cur;
			this.cnt = cnt;
		}

		@Override
		public int compareTo(Node o) {
			return Integer.compare(this.cnt, o.cnt);
		}
	}
	
	static int N;
	static HashMap<Integer, Train> trainDB;
	
	// 두 열차의 환승 가능 여부 반환
	static boolean canTransfer(Train a, Train b) {
		
		if(a.e < b.s || a.s > b.e) return false;
		
		// 최대 연산 33,333
		// 두 열차 중 연산량이 더 적은 열차를 선택해 시간 단축 가능
		for(int i = a.s; i <= a.e; i += a.interval) {
			if(i < b.s)	continue;
			if(i > b.e) continue;
			
			if((i - b.s) % b.interval == 0)	return true;
		}
		
		return false;
	}
	
	public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
		
		this.N = N;
		trainDB = new HashMap<>();
		
		for(int i = 0; i < K; i++)
			add(mId[i], sId[i], eId[i], mInterval[i]);
		
	}

	// 한 테스트케이스 내에서 add는 최대 200회 호출
	// 200 X 200 X 33,333 = 1,333,320,000 ...?
	public void add(int mId, int sId, int eId, int mInterval) {
		
		Train t = new Train(sId, eId, mInterval);
		
		// 최대 연산 200회
		for(int key : trainDB.keySet()) {
			Train nt = trainDB.get(key);
			
			// 최대 연산 33,333회
			if(canTransfer(t, nt)) {
				nt.adj.add(mId);
				t.adj.add(key);
			}
		}
		
		trainDB.put(mId, t);
	}

	// O(1)
	public void remove(int mId) {
		
		trainDB.remove(mId);
		return;
	}

	// 최대 50회 호출
	// BFS의 시간복잡도 O(V + E), 여기서 V는 열차, E는 인접한 열차들의 개수(간선)
	// 최대 연산은 모든 노드가 인접한 경우, 즉 완전그래프를 이루는 경우 O(V^2) = 40,000
	// 40,000 X 50 = 2,000,000
	public int calculate(int sId, int eId) {
		
		Queue<Node> q = new ArrayDeque<>();
		HashSet<Integer> visited = new HashSet<>();
		
		for(int key : trainDB.keySet()) {
			if(!trainDB.containsKey(key))	continue;
			
			Train t = trainDB.get(key);
			
			if(!t.contains(sId)) continue;
			if(t.contains(eId))	return 0;
			
			visited.add(key);
			q.add(new Node(key, 0));
		}
		
		while(!q.isEmpty()) {
			Node c = q.poll();
			
			int cId = c.cur;
			int cnt = c.cnt;
			
			if(!trainDB.containsKey(cId))	continue;
			
			Train cTrain = trainDB.get(cId);
			
			for(int nId : cTrain.adj) {
				if(!trainDB.containsKey(nId))	continue;
				
				Train nt = trainDB.get(nId);
				
				if(visited.contains(nId))	continue;
				if(nt.contains(eId)) return cnt + 1;
				
				visited.add(nId);
				q.add(new Node(nId, cnt + 1));
			}
		}
		
		return -1;
	}
}
