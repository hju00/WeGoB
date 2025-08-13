import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

class Main {
    static int N; // (1 ≤ N ≤ 20)
    static int[][] map;
    static int max_ans = 0;

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        N = Integer.parseInt(br.readLine());
        map = new int[N][N];

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++)
                map[i][j] = Integer.parseInt(st.nextToken());
        }

        dfs(0);

        System.out.println(max_ans);
    }

    static void dfs(int index) {

        if (index == 5) {
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    max_ans = Math.max(max_ans, map[i][j]);
            return;
        }

        int[][] original_map = copy(map);

        moveUp();
        dfs(index + 1);
        map = copy(original_map);

        moveDown();
        dfs(index + 1);
        map = copy(original_map);

        moveLeft();
        dfs(index + 1);
        map = copy(original_map);

        moveRight();
        dfs(index + 1);
        map = copy(original_map);
    }

    // 위
    static void moveUp() {
        // 1) 압축
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                int k = i;
                while (k < N && map[k][j] == 0)
                    k++;
                if (k == N) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[k][j];
                    if (i != k)
                        map[k][j] = 0;
                }
            }
        }
        // 2) 병합
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) {
                if (map[i][j] != 0 && map[i][j] == map[i - 1][j]) {
                    map[i - 1][j] <<= 1;
                    map[i][j] = 0;
                }
            }
        }
        // 3) 병합 후 재압축
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                int k = i;
                while (k < N && map[k][j] == 0)
                    k++;
                if (k == N) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[k][j];
                    if (i != k)
                        map[k][j] = 0;
                }
            }
        }
    }

    // 아래
    static void moveDown() {
        // 1) 압축
        for (int j = 0; j < N; j++) {
            for (int i = N - 1; i >= 0; i--) {
                int k = i;
                while (k >= 0 && map[k][j] == 0)
                    k--;
                if (k < 0) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[k][j];
                    if (i != k)
                        map[k][j] = 0;
                }
            }
        }
        // 2) 병합
        for (int j = 0; j < N; j++) {
            for (int i = N - 2; i >= 0; i--) {
                if (map[i][j] != 0 && map[i][j] == map[i + 1][j]) {
                    map[i + 1][j] <<= 1;
                    map[i][j] = 0;
                }
            }
        }
        // 3) 재압축
        for (int j = 0; j < N; j++) {
            for (int i = N - 1; i >= 0; i--) {
                int k = i;
                while (k >= 0 && map[k][j] == 0)
                    k--;
                if (k < 0) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[k][j];
                    if (i != k)
                        map[k][j] = 0;
                }
            }
        }
    }

    // 왼쪽
    static void moveLeft() {
        // 1) 압축
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int k = j;
                while (k < N && map[i][k] == 0)
                    k++;
                if (k == N) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[i][k];
                    if (j != k)
                        map[i][k] = 0;
                }
            }
        }
        // 2) 병합
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (map[i][j] != 0 && map[i][j] == map[i][j - 1]) {
                    map[i][j - 1] <<= 1;
                    map[i][j] = 0;
                }
            }
        }
        // 3) 재압축
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int k = j;
                while (k < N && map[i][k] == 0)
                    k++;
                if (k == N) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[i][k];
                    if (j != k)
                        map[i][k] = 0;
                }
            }
        }
    }

    // 오른쪽
    static void moveRight() {
        // 1) 압축
        for (int i = 0; i < N; i++) {
            for (int j = N - 1; j >= 0; j--) {
                int k = j;
                while (k >= 0 && map[i][k] == 0)
                    k--;
                if (k < 0) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[i][k];
                    if (j != k)
                        map[i][k] = 0;
                }
            }
        }
        // 2) 병합
        for (int i = 0; i < N; i++) {
            for (int j = N - 2; j >= 0; j--) {
                if (map[i][j] != 0 && map[i][j] == map[i][j + 1]) {
                    map[i][j + 1] <<= 1;
                    map[i][j] = 0;
                }
            }
        }
        // 3) 재압축
        for (int i = 0; i < N; i++) {
            for (int j = N - 1; j >= 0; j--) {
                int k = j;
                while (k >= 0 && map[i][k] == 0)
                    k--;
                if (k < 0) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = map[i][k];
                    if (j != k)
                        map[i][k] = 0;
                }
            }
        }
    }

    static int[][] copy(int[][] src) {
        int[][] ret = new int[N][N];
        for (int i = 0; i < N; i++)
            System.arraycopy(src[i], 0, ret[i], 0, N);
        return ret;
    }
}
