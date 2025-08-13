import java.io.*;
import java.util.*;

public class Main {
    private static int N;

    private static int max = 0;

    private static int[][] map;
    private static int[][] copyMap;

    // 맵의 최대값을 저장하기 위한 메소드
    private static void getMax(){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                max = Math.max(max, map[i][j]);
            }
        }
    }
    
    // 각 방향으로 이동하는 메소드들
    private static void goLeft(){
        for(int i = 0; i < N; i++){
            int[] t = new int[N];
            int idx = 0;

            for(int j = 0; j < N; j++){
                if(map[i][j] == 0) continue;

                if(t[idx] == 0) {
                    t[idx] = map[i][j];
                } else if(t[idx] == map[i][j]) {
                    t[idx] *= 2; // 값을 합치고
                    idx++;       // 다음 위치로 이동
                } else {
                    idx++;       // 다음 위치로 이동하고
                    t[idx] = map[i][j]; // 값을 놓음
                }
            }
            map[i] = t;
        }
    }
    private static void goRight(){
        for(int i = 0; i < N; i++){
            int[] t = new int[N];

            int idx = N-1;
            for(int j = N-1; j >= 0; j--){
                if(map[i][j] == 0) continue;

                if(t[idx] == 0){
                    t[idx] = map[i][j];
                }
                else if(t[idx] == map[i][j]){
                    t[idx] *= 2;
                    idx--;
                }else{
                    idx--;
                    t[idx] = map[i][j];
                }
            }

            map[i] = t;
        }

    }
    private static void goUp(){
        for(int j = 0; j < N; j++){
            int[] t = new int[N];
            int idx = 0;

            for(int i = 0; i < N; i++){
                if(map[i][j] == 0) continue;

                if(t[idx] == 0) {
                    t[idx] = map[i][j];
                } else if(t[idx] == map[i][j]) {
                    t[idx] *= 2;
                    idx++;
                } else {
                    idx++;
                    t[idx] = map[i][j];
                }
            }

            for(int i = 0; i < N; i++){
                map[i][j] = t[i];
            }
        }
    }
    private static void goDown(){
        for(int i = 0; i < N; i++){
            int[] t = new int[N];

            int idx = N-1;
            for(int j = N-1; j >= 0; j--){
                if(map[j][i] == 0) continue;

                if(t[idx] == 0){
                    t[idx] = map[j][i];
                }
                else if(t[idx] == map[j][i]){
                    t[idx] *= 2;
                    idx--;
                }else{
                    idx--;
                    t[idx] = map[j][i];
                }
            }

            for(int j = 0; j < N; j++){
                map[j][i] = t[j];
            }
        }
    }

    // 코드 가독성을 위한 메소드 분리
    private static void move(int d){
        switch(d){
            case 0:
                goLeft();
                return;
            case 1:
                goRight();
                return;
            case 2:
                goUp();
                return;
            case 3:
                goDown();
                return;
        }
    }

    private static void dfs(int depth){
        // 깊이가 5가 되면 최대값을 갱신하고 종료
        if(depth == 5) {
            getMax();
            return;
        }

        // 현재 맵을 복사하여 저장
        for(int i = 0; i < N; i++) {
            copyMap[i] = Arrays.copyOfRange(map[i], 0, N);
        }

        for(int d = 0; d < 4; d++){
            // 방향에 따라 이동
            move(d);

            // 이동 후 depth + 1로 dfs 호출
            dfs(depth + 1);

            // 저장했던 맵으로 되돌리기
            for(int i = 0; i < N; i++) {
                map[i] = Arrays.copyOfRange(copyMap[i], 0, N);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        N = Integer.parseInt(br.readLine());
        
        map = new int[N][N];
        copyMap = new int[N][];
        
        StringTokenizer st;
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        dfs(0);

        
        System.out.println(max);
    }
}