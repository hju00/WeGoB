import java.util.*;

class UserSolution {

    // 주요 자료구조 및 알고리즘 : BFS(격자 그래프), 다익스트라(인접 리스트), 지연 삭제

    // 경로의 목적지 및 이동 거리 정보를 가진 노드 클래스
    // 이동 거리 기반 오름차순 정렬 지원
    static class Node implements Comparable<Node> {
        int to, cost;

        public Node(int to, int cost) {
            this.to = to;
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            return this.cost - o.cost;
        }
    }

    // 맵의 크기, 기사의 최대 스태미나
    int n, maxStamina;
    // 각 테스트 케이스별 게이트의 최대 개수 + 1( 1-based 이므로 배열의 크기를 201로 설정 )
    static final int MAX_GATE = 201;
    // 4방향 배열, 출발점으로부터 각 게이트까지의 최단 거리를 기록할 배열
    int[] dy = {-1, 0, 1, 0}, dx = {0, 1, 0, -1}, dist;
    // 방문 처리 배열
    boolean[][] visited;
    // 맵 정보
    int[][] map;
    // 각 게이트별 y축과 x축(0: y, 1: x)
    int[][] gates = new int[2][MAX_GATE];
    // 각 게이트별 인접 관계를 저장할 인접 리스트
    List<Node>[] graph;
    // 알고리즘 1 : BFS -> 게이트 생성시 해당 게이트로부터 이동 가능한 다른 게이트 목록을 조사하기 위함
    Queue<int[]> q = new ArrayDeque<>();
    // 알고리즘 2 : 다익스트라 -> 출발 게이트부터 목적 게이트까지의 최단 거리를 구하기 위함
    PriorityQueue<Node> pq = new PriorityQueue<>();

    void init(int N, int mMaxStamina, int[][] mMap) {
        n = N;
        maxStamina = mMaxStamina;
        graph = new ArrayList[MAX_GATE];
        dist = new int[MAX_GATE];
        for(int i=0; i<2; i++) {
            Arrays.fill(gates[i], -1);
        }

        visited = new boolean[N][N];
        map = new int[N][N];
        for(int i=0; i<N; i++) {
            for(int j=0; j<N; j++) {
                // 이동 불가능한 위치가 1로 입력되므로 -1로 전환
                // 이유 : 게이트 생성시 게이트 번호(1~200)를 맵 상에 등록하기 위함
                map[i][j] = mMap[i][j] * -1;
            }
        }
    }

    // 알고리즘 1 : BFS
    // 기사가 해당 게이트에서 시작할 경우 maxStamina 거리 이내에 다른 게이트가 존재하는 지 확인
    // 다른 게이트가 존재하면 인접 리스트에 연결 정보 추가
    void bfs(int id, int row, int col) {
        q.clear();
        for(int i=0; i<n; i++) {
            Arrays.fill(visited[i], false);
        }

        visited[row][col] = true;
        q.add(new int[]{row, col, 0});

        while(!q.isEmpty()) {
            int[] cur = q.poll();
            // 지정된 스태미나를 전부 다 쓰면 추가 이동 불가
            if(cur[2] == maxStamina) continue;

            for(int i=0; i<4; i++) {
                int y = cur[0] + dy[i];
                int x = cur[1] + dx[i];
                // 배열 범위 초과 방지 + 이동 불가 지역과 이미 방문한 지역 무시
                if(y < 0 || x < 0 || y >= n || x >= n || map[y][x] == -1 || visited[y][x]) continue;

                visited[y][x] = true;

                // 다음 위치가 게이트인 경우 -> 쌍방향 연결 관계를 인접 리스트에 추가
                if(map[y][x] >= 1) {
                    graph[id].add(new Node(map[y][x], cur[2]+1));
                    graph[map[y][x]].add(new Node(id, cur[2]+1));
                }
                q.add(new int[]{y, x, cur[2]+1});
            }
        }
    }

    // 신규 게이트 생성 함수
    // 호출 횟수 : 최대 200회
    // 해당 위치에는 다른 게이트나 기둥이 없음이 보장된다.
    void addGate(int mGateID, int mRow, int mCol) {
        // 게이트 좌표 저장
        gates[0][mGateID] = mRow;
        gates[1][mGateID] = mCol;

        // 맵 상에 게이트 등록 및 인접 관계 확인(BFS)
        map[mRow][mCol] = mGateID;
        graph[mGateID] = new ArrayList<>();
        bfs(mGateID, mRow, mCol);
    }

    // 게이트 철거 함수
    // 호출 횟수 : 최대 200회
    // 현재 던전 내에 존재하는 게이트만 주어짐이 보장된다.
    void removeGate(int mGateID) {
        int y = gates[0][mGateID];
        int x = gates[1][mGateID];

        // 게이트 철거 표현을 위해 좌표를 다시 (-1, -1)로 초기화
        // 맵 상에서 해당 번호 제거
        gates[0][mGateID] = -1;
        gates[1][mGateID] = -1;
        map[y][x] = 0;
    }

    // 최단 시간 탐색 함수
    // 호출 횟수 : 최대 800회
    // 시작점과 도착점의 번호가 서로 다르고, 던전 내에 존재하는 게이트의 번호만 주어짐이 보장된다.
    int getMinTime(int mStartGateID, int mEndGateID) {
        pq.clear();
        // 각 게이트까지의 최단 거리 초기화
        Arrays.fill(dist, Integer.MAX_VALUE);
        pq.add(new Node(mStartGateID, 0));
        // 시작점의 최단 거리는 0
        dist[mStartGateID] = 0;

        while(!pq.isEmpty()) {
            Node cur = pq.poll();
            // 현재 위치가 도착점이면 현재까지의 이동 거리를 반환하고 즉시 종료
            if(cur.to == mEndGateID) return cur.cost;
            // 이미 더 짧은 거리로 접근한 경우 현재 경로 무시
            if(dist[cur.to] < cur.cost) continue;

            // 현재 게이트와 인접한 다른 게이트의 리스트
            // for each문을 사용하지 않는 이유
            // 순회 도중 이미 철거+된 게이트 번호가 발견되면 지연 삭제하는 로직이 존재
            // 이를 for each문으로 사용하면 Iterator 구조를 만들어서 순회시키므로 내부 데이터가 변경되면 즉시 예외 발생
            // 안전한 처리를 위해 단순 for문 사용
            List<Node> nextList = graph[cur.to];
            for(int i=0; i<nextList.size(); i++) {
                Node next = nextList.get(i);
                // 이미 철거된 게이트는 좌표가 (-1, -1)임. 이러한 경우 인접 리스트에서도 해당 게이트 번호를 제거
                if(gates[0][next.to] == -1) {
                    nextList.remove(i--);
                    continue;
                }

                int newCost = cur.cost + next.cost;
                // 현재까지의 이동 거리보다 더 짧거나 같은 경로로 이미 도착한 상태라면 더 확인할 필요 없음
                if(dist[next.to] <= newCost) continue;

                // 다음 게이트까지의 최단 경로 갱신
                dist[next.to] = newCost;
                pq.add(new Node(next.to, newCost));
            }
        }

        // 어떤 경로로도 도착점에 도달하지 못하면 -1 반환
        return -1;
    }
}