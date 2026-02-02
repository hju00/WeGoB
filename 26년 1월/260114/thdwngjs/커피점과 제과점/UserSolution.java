import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
 
class UserSolution {
    static class Edge implements Comparable<Edge>{
        int cost;
        int to;
        int type;
 
        Edge(int cost, int to, int type){
            this.cost = cost;
            this.to = to;
            this.type = type;
        }
 
        @Override
        public int compareTo(Edge e){
            return Integer.compare(this.cost, e.cost);
        }
    }
 
    final int INF = 1 << 30;
    int N;
    List<List<int[]>> edgeList;
     
    public void init(int N, int K, int sBuilding[], int eBuilding[], int mDistance[]) {
        this.N = N;
        edgeList = new ArrayList<>();
 
        for(int i = 0; i < N; i++){
            edgeList.add(new ArrayList<>());
        }
         
        for (int i = 0; i < K; i++) {
            int u = sBuilding[i];
            int v = eBuilding[i];
            int w = mDistance[i];
 
            add(u, v, w);
        }
        return;
    }
 
    public void add(int sBuilding, int eBuilding, int mDistance) {
        edgeList.get(sBuilding).add(new int[]{eBuilding, mDistance});
        edgeList.get(eBuilding).add(new int[]{sBuilding, mDistance});
    }
 
    public int calculate(int M, int mCoffee[], int P, int mBakery[], int R) {
        int ans = INF;
 
        Queue<Edge> pq = new PriorityQueue<>();
 
        int[][] visit = new int[2][N];
        Arrays.fill(visit[0], -1);
        Arrays.fill(visit[1], -1);
 
        for(int i = 0; i < M; i++) pq.add(new Edge(0, mCoffee[i], 0));
        for(int i = 0; i < P; i++) pq.add(new Edge(0, mBakery[i], 1));
 
        while (!pq.isEmpty()) {
            Edge cur = pq.poll();
            int dist = cur.cost;
            int curId = cur.to;
            int type = cur.type;
 
            if (visit[type][curId] != -1) continue;
             
            visit[type][curId] = dist;
 
            if (visit[0][curId] > 0 && visit[1][curId] > 0) {
                int totalDist = visit[0][curId] + visit[1][curId];
                if (totalDist < ans) {
                    ans = totalDist;
                }
            }
 
            for (int[] edge : edgeList.get(curId)) {
                int nxt = edge[0];
                int cost = edge[1];
                int nxtDist = dist + cost;
 
                if (nxtDist > R) continue;
                if (nxtDist >= ans) continue;
                if (visit[type][nxt] != -1) continue;
 
                pq.add(new Edge(nxtDist, nxt, type));
            }
        }
 
        return ans == INF ? -1 : ans;
    }
}
