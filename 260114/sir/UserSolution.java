import java.util.*;

class UserSolution {

    // 그래프: graph[u]에 {v, w} 저장 (u <-> v, 가중치 w)
    static List<int[]>[] graph;
    static int N;

    // 주택 제외용 stamp 마킹
    static int[] coffeeMark;
    static int[] bakeryMark;
    static int order;

    static int INF = 1000000000;

    public void init(int n, int K, int[] sBuilding, int[] eBuilding, int[] mDistance) {
        N = n;

        graph = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int i = 0; i < K; i++) {
            int a = sBuilding[i];
            int b = eBuilding[i];
            int w = mDistance[i];
            graph[a].add(new int[]{b, w});
            graph[b].add(new int[]{a, w});
        }

        coffeeMark = new int[N];
        bakeryMark = new int[N];
        order = 0;
    }

    public void add(int sBuilding, int eBuilding, int mDistance) {
        graph[sBuilding].add(new int[]{eBuilding, mDistance});
        graph[eBuilding].add(new int[]{sBuilding, mDistance});
    }

    public int calculate(int M, int[] mCoffee, int P, int[] mBakery, int R) {
        // 이번 호출 stamp
        order++;
        if (order == Integer.MAX_VALUE) { // 극단 방지
            Arrays.fill(coffeeMark, 0);
            Arrays.fill(bakeryMark, 0);
            order = 1;
        }

        // 커피/제과점 마킹 (주택 제외)
        for (int i = 0; i < M; i++) coffeeMark[mCoffee[i]] = order;
        for (int i = 0; i < P; i++) bakeryMark[mBakery[i]] = order;

        // 멀티소스 다익스트라 2번 (int 거리)
        int[] distC = dijkstra(mCoffee, M, R);
        int[] distB = dijkstra(mBakery, P, R);

        int ans = INF;

        for (int i = 0; i < N; i++) {
            // 커피점/제과점 자체는 주택 아님
            if (coffeeMark[i] == order || bakeryMark[i] == order)
                continue;

            int dc = distC[i];
            int db = distB[i];

            if (dc <= R && db <= R) {
                int sum = dc + db;
                if (sum < ans) ans = sum;
            }
        }

        if (ans == INF) {
            return -1;
        } else {
            return ans;
        }
    }

    // starts 배열의 앞 len개를 시작점으로, 거리 R까지만 퍼지는 멀티소스 다익스트라
    static int[] dijkstra(int[] starts, int len, int R) {
        int[] dist = new int[N];
        Arrays.fill(dist, INF);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        // pq 원소: {정점 v, 거리 d}

        // 시작점들 초기화
        for (int i = 0; i < len; i++) {
            int s = starts[i];
            if (dist[s] > 0) {     // 중복 시작점 방지
                dist[s] = 0;
                pq.offer(new int[]{s, 0});
            }
        }

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int v = cur[0];
            int d = cur[1];

            if (d != dist[v]) continue; // stale skip
            if (d > R) break;           // R 넘으면 더 볼 필요 없음

            for (int[] nx : graph[v]) {
                int to = nx[0];
                int w = nx[1];

                int nd = d + w;         // 여기서 d<=R라 int overflow 걱정 없음
                if (nd <= R && nd < dist[to]) {
                    dist[to] = nd;
                    pq.offer(new int[]{to, nd});
                }
            }
        }

        return dist;
    }
}