import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

// List 초기화할 때 간선 개수만큼 초기화 해줬는데
// 그래서 메모리 초과가 났던거였네요
public class Main {
    private static int n;
    private static int c;

    private static int[] dist;
    private static List<List<Node>> graph;

    static class Node{
        int dest;
        int time;

        Node(int dest, int s){
            this.dest = dest;
            this.time = s;
        }
    }
    
    private static void dijkstra(){
        boolean[] visited = new boolean[n+1];
        int[] dist = Main.dist;
        Arrays.fill(dist, Integer.MAX_VALUE);
        
        visited[c] = true;
        dist[c] = 0;
        for(int i = 0; i < n+1; i++){
            int time = Integer.MAX_VALUE;
            int idx = c;

            // 현재 최소값 찾기
            for(int j = 1; j < n+1; j++){
                if(!visited[j] && dist[j] < time){
                    time = dist[j];
                    idx = j;
                }
            }

            visited[idx] = true;

            // 최소 거리 갱신
            for(int j = 0; j < graph.get(idx).size(); j++){
                Node cur = graph.get(idx).get(j);

                if(dist[cur.dest] > dist[idx] + cur.time){
                    dist[cur.dest] = dist[idx] + cur.time;
                }
            }
        }
    }
    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int T = Integer.parseInt(br.readLine());

        StringBuilder sb = new StringBuilder();
        for(int t = 0; t < T; t++){
            StringTokenizer st = new StringTokenizer(br.readLine());

            n = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            c = Integer.parseInt(st.nextToken());

            graph = new ArrayList<>(n+1);
            dist = new int[n+1];
            for(int i = 0; i < n+1; i++){
                graph.add(new ArrayList<>());
            }
            
            while(d-- > 0){
                st = new StringTokenizer(br.readLine());

                int a = Integer.parseInt(st.nextToken());
                int b = Integer.parseInt(st.nextToken());
                int s = Integer.parseInt(st.nextToken());

                graph.get(b).add(new Node(a, s));
            }

            dijkstra();

            int cnt = 1;
            int max = 0;
            for(int i = 1; i < n+1; i++){
                if(i == c) continue;

                int cur = dist[i];

                if(cur < Integer.MAX_VALUE){
                    max= Math.max(max, cur);
                    cnt++;
                }
            }

            sb.append(cnt + " " + max + "\n");
        }

        System.out.println(sb);
    }
}
