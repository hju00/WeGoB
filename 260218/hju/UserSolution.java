package old_communicate;

import java.util.*;

class UserSolution {
	
	static class Edge implements Comparable<Edge> {
		int id, from, to, cost, maxLen;
		boolean isValid = true;
		
		public Edge(int id, int from, int to, int cost, int maxLen) {
			this.id = id;
			this.from = from;
			this.to = to;
			this.cost = cost;
			this.maxLen = maxLen;
		}
		
		public Edge(int to, int cost, int maxLen) {
			this.to = to;
			this.cost = cost;
			this.maxLen = maxLen;
		}

		@Override
		public int compareTo(Edge o) {
			if(this.cost == o.cost) 
				return Integer.compare(this.maxLen, o.maxLen);
			return Integer.compare(this.cost, o.cost);
		}
	}
	
	static final int INF = 987654321;
	static int N;
	static int C;
	static ArrayList<Edge> edges[];
	static HashMap<Integer, Edge> edgeDB;
	
	static PriorityQueue<Edge> pq;
	static int dist[];
	static int maxDist[];
	
	public void init(int N, int mCapital, int K, int mId[], int sCity[], int eCity[], int mDistance[]) {
		this.N = N;
		this.C = mCapital;
		edgeDB = new HashMap<>();
		edges = new ArrayList[N];
		
		pq = new PriorityQueue<>();
		dist = new int[N];
		maxDist = new int[N];
		
		for(int i = 0; i < N; i++)
			edges[i] = new ArrayList<>();
		
		for(int i = 0; i < K; i++) {
			Edge e = new Edge(mId[i], sCity[i], eCity[i], mDistance[i], 0);
			edgeDB.put(mId[i], e);
			edges[sCity[i]].add(e);
		}
		
		dijkstra();
	}

	public void add(int mId, int sCity, int eCity, int mDistance) {
		Edge e = new Edge(mId, sCity, eCity, mDistance, 0);
		
		edgeDB.put(mId, e);
		
		edges[sCity].add(e);
		
		// 출발지까지 가는 길조차 없다면 무시
		if (dist[sCity] == INF) return;
		
		int newCost = dist[sCity] + mDistance;
		int newMaxLen = Math.max(maxDist[sCity], mDistance);
		
		// 새로 생긴 길이 도착지(eCity)의 기존 기록보다 빠를 때만 갱신 및 탐색
		if (newCost < dist[eCity] || (newCost == dist[eCity] && newMaxLen < maxDist[eCity])) {
			dist[eCity] = newCost;
			maxDist[eCity] = newMaxLen;
			update(eCity); // eCity부터 탐색
		}
	}

	public void remove(int mId) {
		Edge e = edgeDB.get(mId);
		
		e.isValid = false;
		
		edgeDB.remove(mId);
		
		// e.from에서 이 간선을 타고 e.to로 갔을 때의 비용이 현재 e.to의 최단거리와 완벽히 일치할 때
		if (dist[e.from] != INF) {
			int costThrough = dist[e.from] + e.cost;
			int maxLenThrough = Math.max(maxDist[e.from], e.cost);
			
			// 일치한다면 이 간선이 메인 경로였을 확률이 매우 높으므로 전체 재계산
			if (dist[e.to] == costThrough && maxDist[e.to] == maxLenThrough)
				dijkstra();
		}
	}

	public int calculate(int mCity) {
		return dist[mCity] == INF ? -1 : maxDist[mCity];
	}
	
	static void dijkstra() {
		pq.clear();
		Arrays.fill(dist, INF);
		Arrays.fill(maxDist, INF);
		
		dist[C] = 0;
		maxDist[C] = 0;
		pq.add(new Edge(C, 0, 0));
		
		while(!pq.isEmpty()) {
			Edge c = pq.poll();
			
			if(c.cost > dist[c.to])	continue;
			if(c.cost == dist[c.to] && c.maxLen > maxDist[c.to]) continue;
			
			for(Edge n : edges[c.to]) {
				if(!n.isValid)	continue;
				
				int newCost = dist[c.to] + n.cost;
				int newMaxLen = Math.max(c.maxLen, n.cost);
				
				if(newCost < dist[n.to] || (newCost == dist[n.to] && newMaxLen < maxDist[n.to])) {
					dist[n.to] = newCost;
					maxDist[n.to] = newMaxLen;
					pq.add(new Edge(n.to, newCost, newMaxLen));
				}
			}
		}
	}
	
	static void update(int startNode) {
		
		pq.clear();
		pq.add(new Edge(startNode, dist[startNode], maxDist[startNode]));
		
		while(!pq.isEmpty()) {
			Edge c = pq.poll();
			
			if(c.cost > dist[c.to])	continue;
			if(c.cost == dist[c.to] && c.maxLen > maxDist[c.to]) continue;
			
			for(Edge n : edges[c.to]) {
				if(!n.isValid)	continue;
				
				int newCost = dist[c.to] + n.cost;
				int newMaxLen = Math.max(c.maxLen, n.cost);
				
				if(newCost < dist[n.to] || (newCost == dist[n.to] && newMaxLen < maxDist[n.to])) {
					dist[n.to] = newCost;
					maxDist[n.to] = newMaxLen;
					pq.add(new Edge(n.to, newCost, newMaxLen));
				}
			}
		}
	}
}