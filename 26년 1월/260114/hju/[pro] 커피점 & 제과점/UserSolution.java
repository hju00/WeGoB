package wegoB;

import java.util.*;

class UserSolution {
	
	static int N;
	static List<Edge> edges[];
	
	static class Edge implements Comparable<Edge> {
		int to;
		int cost;
		int flag;	// dist[0] : 커피점, dist[1] : 제과점
		
		public Edge(int to, int cost) {
			this.to = to;
			this.cost = cost;
		}
		
		public Edge(int to, int cost, int flag) {
			this.to = to;
			this.cost = cost;
			this.flag = flag;
		}
		
		@Override
		public int compareTo(Edge o) {
			return Integer.compare(this.cost, o.cost);
		}
	}
	
	public void init(int N, int K, int sBuilding[], int eBuilding[], int mDistance[]) {
		this.N = N;
		edges = new ArrayList[N];
		
		for(int i = 0; i < N; i++)
			edges[i] = new ArrayList<Edge>();
		
		for(int i = 0; i < K; i++) 
			add(sBuilding[i], eBuilding[i], mDistance[i]);
		
		return;
	}

	public void add(int sBuilding, int eBuilding, int mDistance) {
		edges[sBuilding].add(new Edge(eBuilding, mDistance));
		edges[eBuilding].add(new Edge(sBuilding, mDistance));
		return;
	}

	public int calculate(int M, int mCoffee[], int P, int mBakery[], int R) {
		
		PriorityQueue<Edge> pq = new PriorityQueue<Edge>();
		int dist[][] = new int[2][N];
		
		Arrays.fill(dist[0], -1);
		Arrays.fill(dist[1], -1);
		int ans = 2 * R + 1;
		
		for(int i = 0; i < M; i++)
			pq.add(new Edge(mCoffee[i], 0, 0));

		for(int i = 0; i < P; i++)
			pq.add(new Edge(mBakery[i], 0, 1));
		
		
		// lazy dijkstra
		while(!pq.isEmpty()) {
			Edge c = pq.poll();
			
			// 최초로 방문한 경우만 갱신
			// pq를 사용하고 있기 때문에 최초로 방문한 경우가 가장 작은 cost임이 보장됨
			if(dist[c.flag][c.to] != -1) continue;
			
			dist[c.flag][c.to] = c.cost;
			
			// 그렇기 때문에 ans가 갱신된 경우 해당 위치에서 다른 곳을 탐색할 필요가 없어진다.
			if(dist[0][c.to] > 0 && dist[1][c.to] > 0) {
				ans = Math.min(ans, dist[0][c.to] + dist[1][c.to]);
				continue;
			}
			
			for(Edge nx : edges[c.to]) {
				int newCost = dist[c.flag][c.to] + nx.cost;
				
				if(dist[c.flag][nx.to] != -1)	continue;
				if(newCost > R)	continue;
				if(newCost > ans)	continue;
				
				pq.add(new Edge(nx.to, newCost, c.flag));
			}
		}
		
		return ans == 2 * R + 1 ? -1 : ans;
	}
}