import java.util.*;

class UserSolution {

    /*
     * 다익스트라에 사용할 노드 클래스
     * 현재 방문 상태인 도시의 번호와 현재까지의 이동 거리, 현재까지 통과한 가장 긴 도로의 길이
     * 정렬 : (1) 이동 거리, (2) 현재까지 통과한 가장 긴 도로의 길이
     * */
    static class Node implements Comparable<Node> {
        int city, cost, longestWay;

        public Node(int city, int cost, int longestWay) {
            this.city = city;
            this.cost = cost;
            this.longestWay = longestWay;
        }

        @Override
        public int compareTo(Node o) {
            if(this.cost == o.cost) return this.longestWay - o.longestWay;
            return this.cost - o.cost;
        }
    }

    /*
     * 도로 정보 관리 노드 클래스
     * 단방향 도로의 도착 도시 번호, 도로의 길이, 도로 제거 여부 관리
     * */
    static class Way {
        int e, dist;
        boolean removed = false;

        public Way(int e, int dist) {
            this.e = e;
            this.dist = dist;
        }
    }

    int n, cap; // 도시 수, 수도 번호
    HashMap<Integer, Way> waysMap = new HashMap<>(); // 도로 정보 관리 해시맵(도로의 ID는 최대 10억)
    List<Way>[] graph; // 도시 간 인접리스트
    int[] dist, longest; // 수도로부터의 최단 거리, 그 거리를 이동하는 중 거치게 되는 가장 긴 도로의 길이
    PriorityQueue<Node> q = new PriorityQueue<>(); // 다익스트라를 위한 우선순위 큐
    boolean dirty; // 데이터 오염 여부. remove()로 인해 특정 도로가 제거되면 활성화

    static final int INF = Integer.MAX_VALUE;

    /**
     * 초기화 메서드. 초기 1회 호출
     * 도로 추가시 add() 호출, 모든 도로 설치 이후 rebuild() 메서드를 호출하여 수도로부터의 거리 전체 저장
     * 모든 도로는 단방향이다.
     * @param N 도시 수
     * @param mCapital 수도 번호
     * @param K 도로 수
     * @param mId 각 도로의 ID
     * @param sCity 각 도로의 출발 도시
     * @param eCity 각 도로의 도착 도시
     * @param mDistance 각 도로의 길이
     */
    public void init(int N, int mCapital, int K, int mId[], int sCity[], int eCity[], int mDistance[]) {
        n = N;
        cap = mCapital;
        dirty = false;

        waysMap.clear();
        graph = new ArrayList[N];
        for(int i=0; i<N; i++) graph[i] = new ArrayList<>();

        dist = new int[N];
        longest = new int[N];

        for(int i=0; i<K; i++) add(mId[i], sCity[i], eCity[i], mDistance[i]);

        rebuild();
    }

    /**
     * 도로 추가 메서드. 최대 14_000회 호출(init()에서의 호출 포함 최대 24_000회 호출)
     * 새로운 도시가 주어지는 경우, 두 도시가 같은 경우, 같은 번호의 도로 또는 이미 동일 경로가 존재하는 도로가 주어지는 경우는 없다.
     * 도로를 추가한 뒤, 해당 도로의 설치로 인해 dist[eCity] 또는 longest[eCity]가 갱신되는 경우
     * 도착 도시부터 시작하여 국소 갱신(다익스트라) 진행
     * 데이터 오염(dirty) 상태인 경우 국소 갱신은 하지 않음
     * @param mId 도로의 ID
     * @param sCity 도로의 출발 도시
     * @param eCity 도로의 도착 도시
     * @param mDistance 도로의 길이
     */
    public void add(int mId, int sCity, int eCity, int mDistance) {
        Way newWay = new Way(eCity, mDistance);
        waysMap.put(mId, newWay);
        graph[sCity].add(newWay);

        // 데이터 오염 상태거나, 시작 도시가 수도와 연결되지 않은 경우 무시
        if(dirty) return;
        if(dist[sCity] == INF) return;

        // 현재 도로의 추가로 인해 eCity로의 경로가 더 최적화된다면 국소 갱신 다익스트라 실행
        // 아니라면 기존 경로 유지
        int candCost = dist[sCity] + mDistance;
        int candLongest = Math.max(longest[sCity], mDistance);
        if(better(candCost, candLongest, dist[eCity], longest[eCity])) {
            dist[eCity] = candCost;
            longest[eCity] = candLongest;

            q.clear();
            q.add(new Node(eCity, candCost, candLongest));
            dijkstra();
        }
    }

    /**
     * 도로 제거 메서드. 최대 1_000회 호출.
     * 없는 도로의 ID는 주어지지 않는다.
     * 인접 리스트에서 해당 도로를 찾아 제거하는 대신, 상태만 변경
     * 해당 도로 삭제로 인해 수도로부터의 거리 및 해당 경로의 최장 도로 길이 정보가 갱신될 가능성이 존재
     * 그러므로 데이터 오염 상태 활성화
     * @param mId 삭제하려는 도로의 번호
     */
    public void remove(int mId) {
        Way target = waysMap.get(mId);
        target.removed = true;
        dirty = true;
    }

    /**
     * 수도로부터 목적 도시까지의 최단 경로 중 거쳐간 가징 긴 도로의 길이를 조회하는 메서드. 최대 5_000회 호출.
     * 목적지가 수도인 경우는 주어지지 않는다.
     * 데이터 오염 상태인 경우 rebuild()를 호출하여 데이터를 갱신한 뒤 데이터 오염 상태 해제
     * @param mCity 목적 도시
     * @return 전령이 이동한 최단 경로 중, 거쳐간 가장 긴 도로의 길이 반환. 목적 도시로 이동 불가능한 경우 -1 반환.
     */
    public int calculate(int mCity) {
        if(dirty) {
            rebuild();
            dirty = false;
        }

        return dist[mCity] == INF ? -1 : longest[mCity];
    }

    /**
     * 비교 메서드.
     * 현재 다루는 경로가 기존 경로보다 짧은가?
     * 만약 총 길이가 같다면, 그 중 가장 긴 도로의 길이가 현재 기록보다 짧은가?
     * @param c1,l1 현재 경로의 총 길이와 가장 긴 도로의 길이
     * @param c2,l2 기존 경로의 총 길이와 가장 긴 도로의 길이
     * @return 현재 경로가 우위이면 true, 아니라면 false;
     */
    private boolean better(int c1, int l1, int c2, int l2) {
        return (c1 < c2) || (c1 == c2 && l1 < l2);
    }

    /**
     * 거리 및 최장 도로 데이터 전체 갱신 메서드.
     * 초기화시(init()), 또는 데이터 오염 상태(dirty)에서 calculate() 호출시 데이터 전체 갱신
     * add()에서의 국소 갱신과 달리 수도부터 시작하여 모든 도시를 다시 순회한다.
     */
    private void rebuild() {
        Arrays.fill(dist, INF);
        Arrays.fill(longest, INF);

        q.clear();
        dist[cap] = 0;
        longest[cap] = 0;
        q.add(new Node(cap, 0, 0));

        dijkstra();
    }

    /**
     * 다익스트라 메서드.
     * 상기한 정렬 조건에 따라 최단 경로 및 가장 긴 도로의 길이를 기록한다.
     */
    private void dijkstra() {
        while(!q.isEmpty()) {
            Node cur = q.poll();

            // 이미 더 짧은 경로로 온 경우 무시, 같은 거리를 움직였더라도 더 긴 도로를 통과하여 도착한 경우 무시
            if(cur.cost > dist[cur.city] || cur.longestWay > longest[cur.city]) continue;

            for(Way next : graph[cur.city]) {
                // 제거된 도로는 무시
                if(next.removed) continue;

                int newCost = cur.cost + next.dist;
                int longestWay = Math.max(cur.longestWay, next.dist);
                if(!better(newCost, longestWay, dist[next.e], longest[next.e])) continue;

                dist[next.e] = newCost;
                longest[next.e] = longestWay;
                q.add(new Node(next.e, newCost, longestWay));
            }
        }
    }

}