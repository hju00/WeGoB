import java.util.*;

class UserSolution {
    private static final int MAX_GATE = 201;
    private static final int INF = 1_000_000_000;
    
    private static final int[] dx = {-1, 1, 0, 0};
    private static final int[] dy = {0, 0, -1, 1};
    
    private int N;
    private int maxStamina;
    private int[][] map;
    
    private int[][] gateGrid;
    private int[][] gates;
    private boolean[] isActive;
    
    private int[][] adj; 
    
    public void init(int N, int mMaxStamina, int mMap[][]) {
        this.N = N;
        this.maxStamina = mMaxStamina;
        
        this.map = new int[N][N];
        this.gateGrid = new int[N][N];
        
        for(int i = 0; i < N; i++) {
            System.arraycopy(mMap[i], 0, this.map[i], 0, N);
            Arrays.fill(this.gateGrid[i], 0);
        }
        
        gates = new int[MAX_GATE][2];
        isActive = new boolean[MAX_GATE];
        adj = new int[MAX_GATE][MAX_GATE];
        
        for(int i = 0; i < MAX_GATE; i++) {
            Arrays.fill(adj[i], INF);
            adj[i][i] = 0;
        }
    }

    public void addGate(int mGateID, int mRow, int mCol) {
        int x = mRow;
        int y = mCol;

        gates[mGateID][0] = x;
        gates[mGateID][1] = y;
        isActive[mGateID] = true;
        gateGrid[x][y] = mGateID;
        
        bfs(mGateID, x, y);
    }

    public void removeGate(int mGateID) {
        isActive[mGateID] = false;
        int x = gates[mGateID][0];
        int y = gates[mGateID][1];
        gateGrid[x][y] = 0;
    }

    public int getMinTime(int mStartGateID, int mEndGateID) {
        return dijkstra(mStartGateID, mEndGateID);
    }
    
    private void bfs(int startGateID, int startX, int startY) {
        Queue<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{startX, startY, 0});
        
        int[][] visitedDist = new int[N][N];
        for(int i=0; i<N; i++) Arrays.fill(visitedDist[i], -1);
        
        visitedDist[startX][startY] = 0;

        while(!q.isEmpty()) {
            int[] curr = q.poll();
            int x = curr[0];
            int y = curr[1];
            int d = curr[2];
            
            int currentGateID = gateGrid[x][y];
            if (currentGateID != 0 && currentGateID != startGateID && isActive[currentGateID]) {
                adj[startGateID][currentGateID] = d;
                adj[currentGateID][startGateID] = d;
            }
            
            if (d >= maxStamina) continue;
            
            for(int i = 0; i < 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                
                if (nx < 0 || nx >= N || ny < 0 || ny >= N) continue;
                
                if (map[nx][ny] == 1) continue;
                
                if (visitedDist[nx][ny] != -1 && visitedDist[nx][ny] <= d + 1) continue;
                
                visitedDist[nx][ny] = d + 1;
                q.offer(new int[]{nx, ny, d + 1});
            }
        }
    }
    
    private int dijkstra(int startID, int endID) {
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        int[] dist = new int[MAX_GATE];
        Arrays.fill(dist, INF);
        
        dist[startID] = 0;
        pq.offer(new int[]{startID, 0});
        
        while(!pq.isEmpty()) {
            int[] curr = pq.poll();
            int id = curr[0];
            int d = curr[1];
            
            if (d > dist[id]) continue;
            if (id == endID) return d;
            
            for (int nextID = 1; nextID < MAX_GATE; nextID++) {
                if (isActive[nextID] && adj[id][nextID] != INF) {
                    int newDist = d + adj[id][nextID];
                    if (newDist < dist[nextID]) {
                        dist[nextID] = newDist;
                        pq.offer(new int[]{nextID, newDist});
                    }
                }
            }
        }
        
        return -1;
    }
}