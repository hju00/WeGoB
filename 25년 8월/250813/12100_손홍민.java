import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main_12100 {

    static int n, maxNum;
    static int[][] board;

    public static void main(String[] args) throws Exception {
        init();
        bt(0, maxNum);
        System.out.println(maxNum);
    }

    public static void init() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        n = Integer.parseInt(br.readLine());
        board = new int[n][n];
        maxNum = 2;
        for(int i=0; i<n; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            for(int j=0; j<n; j++) {
                int x = Integer.parseInt(st.nextToken());
                board[i][j] = x;
                if(x > maxNum) maxNum = x;
            }
        }
    }

    static int[][] copyArr(int[][] src){
        int[][] dst = new int[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(src[i], 0, dst[i], 0, n);
        return dst;
    }

    public static void bt(int cnt, int curMax) {
        int remain = 5 - cnt;
        if (((long)curMax << remain) <= maxNum) return;

        if(cnt == 5) {
            if(curMax > maxNum) maxNum = curMax;
            return;
        }

        int[][] copy = copyArr(board);
        for(int i=0; i<4; i++) {
            int res = 0;
            switch(i) { // 북, 동, 남, 서
                case 0:
                    res = pressUp(curMax);
                    bt(cnt+1, res);
                    break;
                case 1:
                    res = pressRight(curMax);
                    bt(cnt+1, res);
                    break;
                case 2:
                    res = pressDown(curMax);
                    bt(cnt+1, res);
                    break;
                case 3:
                    res = pressLeft(curMax);
                    bt(cnt+1, res);
                    break;
            }
            board = copyArr(copy);
        }
    }

    public static int pressUp(int max) {
        for(int i=0; i<n; i++) {
            int target = 0;
            for(int j=1; j<n; j++) {
                int num = board[j][i];
                if(num == 0) continue;
                if(board[target][i] == 0) {
                    board[target][i] = num;
                    board[j][i] = 0;
                } else if(board[target][i] == num) {
                    int res = num*2;
                    board[target++][i] = res;
                    if(res > max) max = res;
                    board[j][i] = 0;
                } else {
                    target++;
                    board[target][i] = num;
                    if(target != j) board[j][i] = 0;
                }
            }
        }
        return max;
    }

    public static int pressRight(int max) {
        for(int i=0; i<n; i++) {
            int target = n-1;
            for(int j=n-2; j>=0; j--) {
                int num = board[i][j];
                if(num == 0) continue;
                if(board[i][target] == 0) {
                    board[i][target] = num;
                    board[i][j] = 0;
                } else if(board[i][target] == num) {
                    int res = num*2;
                    board[i][target--] = res;
                    if(res > max) max = res;
                    board[i][j] = 0;
                } else {
                    target--;
                    board[i][target] = num;
                    if(target != j) board[i][j] = 0;
                }
            }
        }
        return max;
    }

    public static int pressDown(int max) {
        for(int i=0; i<n; i++) {
            int target = n-1;
            for(int j=n-2; j>=0; j--) {
                int num = board[j][i];
                if(num == 0) continue;
                if(board[target][i] == 0) {
                    board[target][i] = num;
                    board[j][i] = 0;
                } else if(board[target][i] == num) {
                    int res = num*2;
                    board[target--][i] = res;
                    if(res > max) max = res;
                    board[j][i] = 0;
                } else {
                    target--;
                    board[target][i] = num;
                    if(target != j) board[j][i] = 0;
                }
            }
        }
        return max;
    }

    public static int pressLeft(int max) {
        for(int i=0; i<n; i++) {
            int target = 0;
            for(int j=1; j<n; j++) {
                int num = board[i][j];
                if(num == 0) continue;
                if(board[i][target] == 0) {
                    board[i][target] = num;
                    board[i][j] = 0;
                } else if(board[i][target] == num) {
                    int res = num*2;
                    board[i][target++] = res;
                    if(res > max) max = res;
                    board[i][j] = 0;
                } else {
                    target++;
                    board[i][target] = num;
                    if(target != j) board[i][j] = 0;
                }
            }
        }
        return max;
    }

}
