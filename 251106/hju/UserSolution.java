import java.util.*;

class UserSolution {
	
	static class Edge implements Comparable<Edge> {
		int from;
		int to;
		int time;
		int power;
		
		public Edge(int to, int time) {
			super();
			this.to = to;
			this.time = time;
		}
		
		public Edge(int to, int time, int power) {
			super();
			this.to = to;
			this.time = time;
			this.power = power;
		}

		public Edge(int from, int to, int time, int power) {
			super();
			this.from = from;
			this.to = to;
			this.time = time;
			this.power = power;
		}
		
		@Override
		public int compareTo(Edge o) {
			if(this.time == o.time)
				return Integer.compare(this.power, o.power);
			return Integer.compare(this.time, o.time);
		}
	}
	
	static final int INF = 987654321;
	static int n;
	static HashMap<Integer, Edge> edgeDB;
	static HashSet<Edge> edges[];
	static int[] infectTime;	// 도시가 감염되는 최단 시간을 저장
	static int[][]	dist;		
	static int[] charge;
	
	
	// 연산 : 1 * (500 + 4000) = 4500
	public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
		
		// 전역 변수 초기화
		n = N;
		edgeDB = new HashMap<>();
		edges = new HashSet[n];
		infectTime = new int[n];
		charge = new int[n];
		
		for(int i = 0; i < n; i++) {
			edges[i] = new HashSet<>();
			charge[i] = mCharge[i];
		}
		
		for(int i = 0; i < K; i++) {
			int id = mId[i];
			int from = sCity[i];
			int to = eCity[i];
			int time = mTime[i];
			int power = mPower[i];
			
			Edge e = new Edge(from, to, time, power);
			edgeDB.put(id, e);
			edges[from].add(e);
		}
		
		return;
	}
	
	// 연산 : 3000
	public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
		
		Edge e = new Edge(sCity, eCity, mTime, mPower);
		edgeDB.put(mId, e);
		edges[sCity].add(e);
		
		return;
	}
	
	// 연산 : 900
	public void remove(int mId) {
		
		Edge toRemove = edgeDB.get(mId);
		edges[toRemove.from].remove(toRemove);
		edgeDB.remove(mId);
		
		return;
	}

	public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
		int answer = INF;
		
		dist = new int[n][B + 1];
		
		dijkstra_city(M, mCity, mTime);
		answer = dijkstra_car(sCity, eCity, B);
		
		return answer;
	}
	
	// 도시가 최초로 감염되는 시간을 구하는 다익스트라
	static void dijkstra_city(int M, int mCity[], int mTime[]) {
		PriorityQueue<Edge> pq = new PriorityQueue<>();
		Arrays.fill(infectTime, INF);
		
		for(int i = 0; i < M; i++) {
			infectTime[mCity[i]] = mTime[i];
			pq.add(new Edge(mCity[i], mTime[i]));
		}
		
		while(!pq.isEmpty()) {
			Edge cur = pq.poll();
			
			if(cur.time > infectTime[cur.to])	continue;
			
			for(Edge nx : edges[cur.to]) {
				int newTime = infectTime[cur.to] + nx.time;
				if(newTime < infectTime[nx.to]) {
					infectTime[nx.to] = newTime;
					pq.add(new Edge(nx.to, newTime));
				}
			}
		}
	}
	
	static int dijkstra_car(int start, int end, int B) {
		PriorityQueue<Edge> pq = new PriorityQueue<>();
		for(int i = 0; i < n; i++)
			Arrays.fill(dist[i], INF);
		
		dist[start][B] = 0;
		pq.add(new Edge(start, 0, B));
		
		while(!pq.isEmpty()) {
			Edge cur = pq.poll();
			
			if(cur.to == end)	return cur.time;	
			if(cur.time > dist[cur.to][cur.power])	continue;
			
			for(Edge nx : edges[cur.to]) {
				// 현재 도시에서 충전 구현
				// 모든 충전의 경우의 수를 넣었지만 그럴 필요가 없음
				// 2가지 케이스로 나뉨
				
				// 1. 충전 없이 다음 도시로 출발하는 경우
				// 현재 충전량이 다음 도시로의 전력 소모량보다 커야함
				if(cur.power >= nx.power) {
					int nPower = cur.power - nx.power;
					int newTime = dist[cur.to][cur.power] + nx.time;
					
					if(newTime >= infectTime[nx.to])	continue;
					
					if(newTime < dist[nx.to][nPower]) {
						for(int i = nPower; i >= 0; i--) {
							if(dist[nx.to][i] < newTime)	break;
							dist[nx.to][i] = newTime;
						}
						pq.add(new Edge(nx.to, newTime, nPower));
					}
				}
				
				// 2. 현재 위치에서 1시간 충전하는 경우
				// 현재 위치에서 충전하는 경우는 다음 도시를 현재 도시로 설정하면 됨
				if(cur.power < B) {
					int nPower = cur.power + charge[cur.to];
					nPower = nPower > B ? B : nPower;
					int newTime = dist[cur.to][cur.power] + 1;
					
					if(newTime >= infectTime[cur.to]) continue;
					
					if(newTime < dist[cur.to][nPower]) {
						for(int i = nPower; i >= 0; i--) {
							if(dist[cur.to][i] < newTime)	break;
							dist[cur.to][i] = newTime;
						}
						pq.add(new Edge(cur.to, newTime, nPower));
					}
				}
				
//				for(int i = 0; i <= 300; i++) {
//					int nPower = cur.power + charge[cur.to] * i;
//					
//					if(nPower < nx.power)	continue;
//					if(nPower > B)	break;
//					if(dist[cur.to][cur.power] + i >= infectTime[cur.to])	break;
//					
//					int newTime = dist[cur.to][cur.power] + i + nx.time;
//					if(newTime < dist[nx.to][nPower - nx.power]) {
//						dist[nx.to][nPower - nx.power] = newTime;
//						pq.add(new Edge(nx.to, newTime, nPower - nx.power));
//					}
//				}
			}
		}
		
		return -1;
	}
}