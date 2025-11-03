import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    static List<Node>[] list;
    static int[] dist;

    public static void main(String[] args) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int tc = Integer.parseInt(br.readLine());

        while (tc-- > 0) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken()); // 컴퓨터의 개수
            int d = Integer.parseInt(st.nextToken()); // 의존성의 개수 -> 간선의 개수
            int c = Integer.parseInt(st.nextToken()); // 해킹당한 컴퓨터의 번호 c
            dist = new int[n];

            list = new List[n];
            for (int i = 0; i < n; i++) {
                list[i] = new ArrayList<>();
            }
            for (int i = 0; i < d; i++) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken()) - 1;
                int b = Integer.parseInt(st.nextToken()) - 1;
                int s = Integer.parseInt(st.nextToken()); // 소요 시간
                list[b].add(new Node(a, s));
            }

            dijkstra(c);
        }

    }

    private static void dijkstra(int start) {
        PriorityQueue<Node> pq = new PriorityQueue<>();

        dist[start] = 0;
        pq.offer(new Node(start, 0)); // 도착지, 거리

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int currentV = current.num;
            int currentE = current.cost;

            // 최단거리보다 길다면 더 볼 필요가 없다.
            if (currentE > dist[currentV]) continue;

            for (Node next : list[currentV]) {
                int nextV = next.num;
                int nextE = next.cost;

                // 현재 노드를 거쳐 다음 노드로 가는 거리가 기존에 최단 거리보다 짧다면
                if (dist[currentV] + nextE < dist[nextV]) {
                    dist[nextV] = dist[currentV] + nextE; // 최단 거리를 갱신
                    pq.offer(new Node(nextV, nextE));
                }
            }

        }

    }

    static class Node implements Comparable<Node> {
        int num;
        int cost;
        Node(int num, int cost) {
            this.num = num;
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.cost, o.cost);
        }
    }

}
