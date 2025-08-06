import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

class Main {
    // 컴퓨터 개수 n, 의존성 개수 d, 해킹당한 컴퓨터의 번호 c가 주어진다
    static int n, d, c; // (1 ≤ n ≤ 10,000, 1 ≤ d ≤ 100,000, 1 ≤ c ≤ n)
    static ArrayList<ArrayList<Node>> graph;
    static int infected_cnt, time; // 총 감염되는 컴퓨터 수, 마지막 컴퓨터가 감염되기까지 걸리는 시간

    static class Node {
        int idx;
        int cost;

        Node(int idx, int cost) {
            this.idx = idx;
            this.cost = cost;
        }
    }

    static void dijkstra(int start) {
        int dist[] = new int[n + 1];
        for (int i = 0; i <= n; i++)
            dist[i] = Integer.MAX_VALUE;

        PriorityQueue<Node> pq = new PriorityQueue<>((o1, o2) -> Integer.compare(o1.cost, o2.cost));
        pq.offer(new Node(start, 0));
        dist[start] = 0;

        while (!pq.isEmpty()) {
            Node cNode = pq.poll();

            if (dist[cNode.idx] < cNode.cost)
                continue;

            for (int i = 0; i < graph.get(cNode.idx).size(); i++) {
                Node nxNode = graph.get(cNode.idx).get(i);
                if (dist[nxNode.idx] > cNode.cost + nxNode.cost) {
                    dist[nxNode.idx] = cNode.cost + nxNode.cost;
                    pq.offer(new Node(nxNode.idx, dist[nxNode.idx]));
                }
            }
        }

        // dist 배열에서 MAX_VALUE가 아니면 도달을 했음. 마지막 컴퓨터는 감염되는 시간이 가장 오래걸림
        for (int d : dist) {
            if (d != Integer.MAX_VALUE) {
                infected_cnt++;
                time = Math.max(time, d);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int T = Integer.parseInt(br.readLine());

        for (int test_case = 1; test_case <= T; test_case++) {
            infected_cnt = 0;
            time = 0;

            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            d = Integer.parseInt(st.nextToken());
            c = Integer.parseInt(st.nextToken());

            graph = new ArrayList<ArrayList<Node>>(n + 1);
            for (int i = 0; i <= n; i++)
                graph.add(new ArrayList<>());

            while (d-- > 0) {
                st = new StringTokenizer(br.readLine());
                int a = Integer.parseInt(st.nextToken());
                int b = Integer.parseInt(st.nextToken());
                int s = Integer.parseInt(st.nextToken());

                // b가 a로 가는데(감염시키는데) 걸리는 비용(시간)
                graph.get(b).add(new Node(a, s));
            }

            dijkstra(c);

            System.out.println(infected_cnt + " " + time);
        }
    }
}
