import java.util.*;

class UserSolution
{
    class Gate {
        int id;
        int r, c;
        boolean isAlive;

        public Gate(int id, int r, int c) {
            this.id = id;
            this.r = r;
            this.c = c;

            isAlive = true;
        }
    }

    int[][] map;
    int[][] gateDist; // gateDist[r][c]: r게이트와 c게이트 사이의 거리
    int N;
    int maxStamina;
    int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
    HashMap<Integer, Gate> idxToGate;
    PriorityQueue<int[]> pq;

    void init(int N, int mMaxStamina, int mMap[][])
    {
        this.map = mMap; // 0: 길, 1; 벽
        this.N = N;
        this.maxStamina = mMaxStamina;
        gateDist = new int[201][201]; // 1 <= mID <= 200
        int INF = Integer.MAX_VALUE;
        for (int i = 0; i <= 200; i++) Arrays.fill(gateDist[i], INF);
        idxToGate = new HashMap<>();

    }

    void addGate(int mGateID, int mRow, int mCol)
    {
        // 1. 게이트 생성
        Gate gate = new Gate(mGateID, mRow, mCol);
        idxToGate.put(mGateID, gate);
        map[mRow][mCol] = -mGateID;

        // 2. 그 게이트 -> 다른 게이트 거리 계산
        Queue<int[]> q = new ArrayDeque<>();
        boolean[][] visited = new boolean[N][N];

        q.offer(new int[] {mRow, mCol, 0});
        visited[mRow][mCol] = true;

        while(!q.isEmpty()) {
            int[] cur = q.poll();

            for(int d = 0; d < 4; d++) {
                int nr = cur[0] + dr[d];
                int nc = cur[1] + dc[d];
                int nd = cur[2] + 1;

                if (nd > maxStamina) continue;
                if(!isIn(nr, nc)) continue;
                if (map[nr][nc] == 1) continue;
                if(visited[nr][nc]) continue;

                visited[nr][nc] = true;
                q.offer(new int[]{nr, nc, nd});

                if(map[nr][nc] < 0) {
                    int toGate = -map[nr][nc];
                    Gate g = idxToGate.get(toGate);
                    if (g == null || !g.isAlive) continue;

                    if (gateDist[mGateID][toGate] > nd) {
                        gateDist[mGateID][toGate] = nd;
                        gateDist[toGate][mGateID] = nd;
                    }
                }
            }
        }
    }

    private boolean isIn(int r, int c) {
        return 0 <= r && r < N && 0 <= c && c < N;
    }

    void removeGate(int mGateID)
    {
        Gate gate = idxToGate.get(mGateID);
        gate.isAlive = false;
        map[gate.r][gate.c] = 0;
    }

    int getMinTime(int mStartGateID, int mEndGateID)
    {
        pq = new PriorityQueue<>((o1, o2) -> Integer.compare(o1[1], o2[1])); // {cur, dist};

        Gate start = idxToGate.get(mStartGateID);
        Gate end = idxToGate.get(mEndGateID);

        if(start == null || end == null || !start.isAlive || !end.isAlive) return -1;

        // 게이트: 1 ~ 201
        int[] dist = new int[201];
        int MAX = N * N;
        Arrays.fill(dist, MAX);

        // pq에 넣기
        dist[start.id] = 0;
        pq.offer(new int[] {start.id, 0});
        while(!pq.isEmpty()) {
            int[] cur = pq.poll();

            int curIdx = cur[0];
            int curDist = cur[1];

            if (curIdx == mEndGateID) return dist[mEndGateID]; // 도착!

            // 나와 연결될 수 있는 친구들 1 ~ 200
            for(int i = 1; i < 201; i++) {
                if(i == curIdx) continue; // 나랑 나랑 연결 확인 필요 없음

                Gate next = idxToGate.get(i);

                if(next == null || !next.isAlive) continue; // 게이트 없거나 삭제되었으면 넘어가기

                int d = gateDist[curIdx][i];
                if(d > maxStamina) continue; // 여기서부터 maxStamina보다 멀면 못 감

                int nextDist = curDist + d;
                if(dist[i] > nextDist) {
                    dist[i] = nextDist;
                    pq.offer(new int[] {i, nextDist});
                }

            }

        }

        return -1;
    }
}