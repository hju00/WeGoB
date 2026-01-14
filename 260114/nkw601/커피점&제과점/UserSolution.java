import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

class UserSolution {
    private PriorityQueue<long[]> pq;
    private long[] distCafe, distBakery;

    private boolean[] isCafe, isBakery;
    private int N, K;
    private ArrayList<int[]>[] adjList;

    private final long INF = Long.MAX_VALUE;

    public void init(int N, int K, int sBuilding[], int eBuilding[], int mDistance[]) {
        this.N = N;
        this.K = K;

        // 배열 생성
        distCafe = new long[N];
        distBakery = new long[N];
        isCafe = new boolean[N];
        isBakery = new boolean[N];

        // 인접리스트 초기화
        adjList = new ArrayList[N];
        for(int i = 0; i < N; i++) {
            adjList[i] = new ArrayList<int[]>();
        }

        // i번째 건물들 연결: K개
        for(int i = 0; i < K; i++) {
            int s = sBuilding[i];
            int e = eBuilding[i];
            int dist = mDistance[i];

            // 양방향 연결
            adjList[s].add(new int[] {e, dist});
            adjList[e].add(new int[] {s, dist});
        }

        // pq 만들기
        pq = new PriorityQueue<>((o1, o2) -> Long.compare(o1[1], o2[1])); // to, dist 저장
    }

    // 간선 추가
    public void add(int sBuilding, int eBuilding, int mDistance) {
        adjList[sBuilding].add(new int[] {eBuilding, mDistance});
        adjList[eBuilding].add(new int[] {sBuilding, mDistance});
    }

    // M개의 커피, P개의 빵집
    public int calculate(int M, int mCoffee[], int P, int mBakery[], int R) {
        // 카페인지, 베이커리인지 확인 -> 아니면 집
        for(int i = 0; i < M; i++) {
            isCafe[mCoffee[i]] = true;
        }

        for(int i = 0; i < P; i++) {
            isBakery[mBakery[i]] = true;
        }

        // dist 배열 채우기
        Arrays.fill(distBakery, INF);
        Arrays.fill(distCafe, INF);

        // 모든 카페에서 시작하는 다익스트라
        dijkstra(distBakery, mCoffee, M, R);

        // 모든 제과점에서 시작하는 다익스트라
        dijkstra(distCafe, mBakery, P, R);

        long ans = INF;

        for(int i = 0; i < N; i++) {
            if (isCafe[i] || isBakery[i]) continue;

            long distC = distCafe[i];
            long distB = distBakery[i];

            if(distC <= R && distB <= R) {
                ans = Math.min(ans, distB + distC);
            }
        }

        // 초기화
        for(int i = 0; i < M; i++) {
            isCafe[mCoffee[i]] = false;
        }

        for(int i = 0; i < P; i++) {
            isBakery[mBakery[i]] = false;
        }

        return ans == INF ? -1 : (int) ans;
    }
    // 참고 https://velog.io/@ejjem/%EB%8B%A4%EC%9D%B5%EC%8A%A4%ED%8A%B8%EB%9D%BC-%EC%95%8C%EA%B3%A0%EB%A6%AC%EC%A6%98dijkstra-algorithm
    private void dijkstra(long[] dist, int[] starts, int size, int limit) {
        pq.clear();

        // 모든 start에서 시작
        for(int i = 0; i < size; i++) {
            pq.offer(new long[] {starts[i], 0});
            dist[starts[i]] = 0;
        }

        while(!pq.isEmpty()) {
            long[] current = pq.poll();

            int cur = (int) current[0];
            long curDist = current[1];
            if(curDist != dist[cur]) continue; // 이미 갱신된 최단거리 있으면 더 볼 필요 없음
            if(curDist > limit) break; // 이제 나올 애들은 볼 필요 없음

            for(int[] nodes : adjList[cur]) {
                int to = nodes[0];
                long nextDist = nodes[1] + curDist;

                if(nextDist > limit) continue;
                if(nextDist < dist[to]) {
                    dist[to] = nextDist;
                    pq.offer(new long[] {to, nextDist});
                }
            }
        }
    }
}