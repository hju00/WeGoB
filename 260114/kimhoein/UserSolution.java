
import java.util.*;

class UserSolution {
    
    static class Node implements Comparable<Node> {
        int to, distance, type;

        Node(int to, int distance, int type) {
            this.to = to;
            this.distance = distance;
            this.type = type;
        }

        @Override
        public int compareTo(Node o) {
            // 거리가 짧은 순서대로 정렬
            return Integer.compare(this.distance, o.distance);
        }
    }

    // 멤버 변수로 선언하여 메모리 재사용 (시간 초과 방지)
    ArrayList<Node>[] adj;
    int[][] dist;
    boolean[] isSpot;
    PriorityQueue<Node> pq;
    int N;
    final int INF = Integer.MAX_VALUE;

    public void init(int N, int K, int sBuilding[], int eBuilding[], int mDistance[]) {
        this.N = N;
        adj = new ArrayList[N + 1];
        dist = new int[N + 1][2];
        isSpot = new boolean[N + 1];
        pq = new PriorityQueue<>();

        for (int i = 0; i <= N; i++) {
            adj[i] = new ArrayList<>();
        }

        for (int i = 0; i < K; i++) {
            // 간선 추가 (type은 탐색 시에 결정되므로 초기값은 -1)
            adj[sBuilding[i]].add(new Node(eBuilding[i], mDistance[i], -1));
            adj[eBuilding[i]].add(new Node(sBuilding[i], mDistance[i], -1));
        }
    }

    public void add(int sBuilding, int eBuilding, int mDistance) {
        adj[sBuilding].add(new Node(eBuilding, mDistance, -1));
        adj[eBuilding].add(new Node(sBuilding, mDistance, -1));
    }

    public int calculate(int M, int mCoffee[], int P, int mBakery[], int R) {
        // 1. 초기화
        for (int i = 1; i <= N; i++) {
            dist[i][0] = INF;
            dist[i][1] = INF;
            isSpot[i] = false;
        }
        pq.clear();

        // 2. 커피숍(Type 0) 시작점 설정
        for (int i = 0; i < M; i++) {
            dist[mCoffee[i]][0] = 0;
            isSpot[mCoffee[i]] = true; // 커피숍은 만남 장소 제외
            pq.add(new Node(mCoffee[i], 0, 0));
        }

        // 3. 빵집(Type 1) 시작점 설정
        for (int i = 0; i < P; i++) {
            dist[mBakery[i]][1] = 0;
            isSpot[mBakery[i]] = true; // 빵집은 만남 장소 제외
            pq.add(new Node(mBakery[i], 0, 1));
        }

        int minTotal = INF;

        // 4. 다익스트라 시작
        while (!pq.isEmpty()) {
            // curr: 큐에서 꺼낸 '현재' 노드 정보
            Node curr = pq.poll();

            // 이미 더 짧은 경로를 찾았다면 무시
            if (dist[curr.to][curr.type] < curr.distance) continue;
            // 현재 거리가 이미 찾은 최소합보다 크면 무시 (가지치기)
            if (curr.distance >= minTotal) continue;

            // curr.to 건물과 연결된 이웃 건물들(next)을 하나씩 확인
            for (Node edge : adj[curr.to]) {
                int nextDist = curr.distance + edge.distance;

                // 문제 조건: 개별 거리가 R을 넘으면 안 됨
                if (nextDist > R) continue;

                // 더 짧은 경로를 발견했을 때만 갱신
                if (dist[edge.to][curr.type] > nextDist) {
                    dist[edge.to][curr.type] = nextDist;
                    
                    // 큐에 넣을 때 curr.type(커피인지 빵인지)을 그대로 전달!
                    pq.add(new Node(edge.to, nextDist, curr.type));
                    //System.out.println(edge.to + " " + curr.type + " " + nextDist);
                    // 만남 장소 확인: 제3의 장소(!isSpot)이고 반대편 파동(otherType)이 도착해 있다면
                    int otherType = 1 - curr.type;
                    if (!isSpot[edge.to] && dist[edge.to][otherType] != INF) {
                        int sum = nextDist + dist[edge.to][otherType];
                        if (sum < minTotal) {
                            minTotal = sum;
                        }
                    }
                }
            }
        }
        
        // 5. 최종 결과 반환 (R 미만인지 확인)
        if (minTotal == INF) return -1;
        return minTotal;
    }
}