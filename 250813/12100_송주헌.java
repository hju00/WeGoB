import java.io.*;
import java.util.*;

public class Main {
    private static int N;

    private static int max = 0;

    // 맵의 최대값을 저장하기 위한 메소드
    private static void getMax(int[][] map) {
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                max = Math.max(max, map[i][j]);
            }
        }
    }

    private static int[][] rotate(int[][] map) {
        int[][] rotated = new int[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                rotated[j][N - 1 - i] = map[i][j];
            }
        }

        return rotated;
    }

    private static void pressMap(int[][] map){
        for(int i = 0; i < N; i++){
            int[] t = new int[N];

            int idx = 0;
            for(int j = 0; j < N; j++){
                if(map[j][i] == 0) continue;

                if(t[idx] == 0){
                    t[idx] = map[j][i];
                }
                else if(t[idx] == map[j][i]){
                    t[idx] *= 2;
                    idx++;
                }else{
                    idx++;
                    t[idx] = map[j][i];
                }
            }

            for(int j = 0; j < N; j++){
                map[j][i] = t[j];
            }
        }
    }

    private static void dfs(int[][] map, int depth){
        if(depth == 5) {
            getMax(map);
            return;
        }

        for (int i = 0; i < 4; i++) {
            int[][] newMap = rotate(map);

            pressMap(newMap);
            dfs(newMap, depth + 1);

            map = rotate(map);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        N = Integer.parseInt(br.readLine());
        
        int[][] map = new int[N][N];
        
        StringTokenizer st;
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        dfs(map, 0);

        System.out.println(max);
    }
}