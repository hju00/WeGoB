import java.util.*;

class UserSolution {

    static final int INF = 1e9;
    // 도로 정보 담는 클래스
    static class Edge {
        int to; //도착 도시
        int w; //거리
        boolean active; // 삭제여부

        Edge(int to, int w) {
            this.to = to;
            this.w = w;
            this.active = true;
        }
    }

    int N, capital;

    ArrayList<Edge>[] graph; //도시에서 나가는 모든 간선 목록(edge들의 리스트)
    HashMap<Integer, Edge> idMap; // 도로 id -> edge 매핑/ Remove 호출시 해당 edge찾기

    int[] dist, best; // 최단거리 최단거리 경로중 가장 최소 간선

    public void init(int N, int mCapital, int K, int[] mId, int[] sCity, int[] eCity, int[] mDistance) {

        this.N = N;
        this.capital = mCapital;

        // 각도시마다 인접 리스트를 만들어둠
        graph = new ArrayList[N];
        for (int i = 0; i < N; i++) {
            graph[i] = new ArrayList<>();
        }
        // 초기 도로 수 K 기준으로 넉넉하게 해시맵 준비
        idMap = new HashMap<>(K * 2);
        // 초기 K개 도로를 그래프에 추가
        //sCity[i] -> eCity[i] 간선 생성 후 graph[sCity[i]]에 넣음.
	    //그 도로의 ID(mId[i])로 Edge 객체를 맵에 저장(삭제용).
        for (int i = 0; i < K; i++) {
            Edge e = new Edge(eCity[i], mDistance[i]);
            graph[sCity[i]].add(e);
            idMap.put(mId[i], e);
        }
        //결과 저장 배열 준비
        dist = new int[N];
        best = new int[N];
        // 초기 그래프 기준으로 다익스트라 한번 계산
        fullDijkstra();
    }

    public void add(int mId, int sCity, int eCity, int mDistance) {
        // 새 edge 객체 생성
        // 출발 sCity의 인접 리스트에 추가
        // ID -> Edge 매핑 저장
        Edge e = new Edge(eCity, mDistance);
        graph[sCity].add(e);
        idMap.put(mId, e);
        // 부분 갱신 : sCity에서 시작하는 경로들이 더 좋아질 수 있으니 그 영햔만 전파
        relaxFrom(sCity);
    }

    public void remove(int mId) {
        //도로 ID로 삭제 요청
        Edge e = idMap.get(mId); // 해시맵에서 해당 edge 객체를 찾음
        e.active = false;   // 비활성 처리
        idMap.remove(mId); // id edge 매핑 제거

        fullDijkstra();     // 간선 삭제는 최단경로가 바뀔수있으니 다익스트라 계산
    }
    // 불가능할시 -1, 가능할시 최단거리중 최대간선 최솟값
    public int calculate(int mCity) {
        if (dist[mCity] == INF) {
            return -1;
        } else {
            return best[mCity];
        }
    }

    //다익스트라 계산
    //수도에서 모든 도시까지 거리와,best를 다시구하기
    private void fullDijkstra() {
        // 전부 불가로 초기화
        Arrays.fill(dist, INF);
        Arrays.fill(best, INF);

        PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[0]!=b[0]?a[0]-b[0]:a[1]-b[1]);

        //수도로 시작
        dist[capital] = 0;
        best[capital] = 0;
        // 수도 부터 도착지까지 총거리, 그 경로에서 가장 긴 간선 길이 , 현재도시 순서로 pq
        // 총거리가 적은게 먼저 총거리가 같으면 그 경로에서 가장 긴 간선의 길이가 최소인것 비교
        pq.add(new int[]{0,0,capital});
        //pq 가 남아있는동안 반복
        while(!pq.isEmpty()) {
            int[] cur = pq.poll();//pq값을 꺼내고 삭제
            //현재 발견된 후보중 총거리가 가장 짧고, 최대간선도 가장 작은 상태를 꺼냄
            int d = cur[0];
            int b = cur[1];
            int u = cur[2];
            // (d,b,u) 총거리, 최대간선, 현재 도시
            // 총거리가 u에서 온 총거리보다 길면 버림
            // 총거리가 같은데 최대간선이 더 크면 버림
            // 즉, pq값이 현재보다 나쁘면 버림
            if(d > dist[u] || (d==dist[u] && b>best[u])) {
                continue;
            }
            // u 에서 나가는 간선들을 하나씩 확인
            // graph[u] 는 u에서 출발하는 간선 리스트
            // i 로 돌면서 efmf gkskTlr RJso ghkrdls
            for (int i = 0, sz = graph[u].size(); i < sz; i++) {
                Edge e = graph[u].get(i);
                // 삭제 간선 건너뛰기
                if (!e.active){
                    continue;
                }
                // v로 가는 새후보 계산
                // nd: 수도 → u 까지 거리 d + (u→v 간선 길이)
                // nb: 현재 경로에서의 최대간선(b)과 이번 간선 길이(e.w) 중 큰 값
                // 수도 -> v로 가는 이 경로에서 가장 긴 도로
                int v = e.to;
                int nd = d + e.w;
                int nb = Math.max(b, e.w);
                // nd < dist[v]
                // 총 거리가 더 짧음. 무조건 갱신
                // nd == dist[v] && nb < best[v]
                // 총 거리는 같지만, 그 최단거리 경로들 중에서
                // 가장 긴 도로 길이가 더 작음. 이게 문제의 2차 목표라 갱신.
                if (nd < dist[v] || (nd == dist[v] && nb < best[v])) {
                    // v에 대한 최선값을 업데이트하고
                    // v에서 또 확장해야 하니까 PQ에 후보로 넣는다.
                    dist[v] = nd;
                    best[v] = nb;
                    pq.add(new int[]{nd, nb, v});
                }
            }
        }
    }
    // 전체 계산 하지않고 add 후 부분갱신
    private void relaxFrom(int start) {
        // start가 수도 도달 불가면 종료
        if(dist[start] == INF) {
            return;
        }
        // 우선순위 기준은 동일
        PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[0]!=b[0]?a[0]-b[0]:a[1]-b[1]);
        //start 를 시작 후보로 넣기
        pq.add(new int[]{dist[start], best[start], start});

        while(!pq.isEmpty()) {

            int[] cur = pq.poll();
            int d = cur[0];
            int b = cur[1];
            int u = cur[2];

            if(d > dist[u] || (d==dist[u] && b>best[u])) {
                continue;
            }

            for (int i = 0, sz = graph[u].size(); i < sz; i++) {
                Edge e = graph[u].get(i);

                if (!e.active) {
                    continue;
                }

                int v = e.to;
                int nd = d + e.w;
                int nb = Math.max(b, e.w);

                if (nd < dist[v] || (nd == dist[v] && nb < best[v])) {

                    dist[v] = nd;
                    best[v] = nb;
                    pq.add(new int[]{nd, nb, v});
                }
            }
        }
    }
}