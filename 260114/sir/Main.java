import java.util.*;

public class Main {
    public static void main(String[] args) {

        // ===== 테스트 드라이버(Main) =====
        UserSolution us = new UserSolution();

        // [Table 1] 예제 그대로
        int N = 6, K = 6;
        int[] s = {0, 2, 1, 1, 5, 4};
        int[] e = {1, 0, 2, 3, 3, 5};
        int[] d = {80, 30, 10, 10, 60, 20};

        us.init(N, K, s, e, d);

        us.add(3, 4, 30);
        int r1 = us.calculate(1, new int[]{0}, 1, new int[]{5}, 50);
        System.out.println("r1 = " + r1 + " (expected 100)");

        us.add(1, 4, 30);
        int r2 = us.calculate(1, new int[]{0}, 1, new int[]{5}, 50);
        System.out.println("r2 = " + r2 + " (expected 90)");

        int r3 = us.calculate(2, new int[]{0, 4}, 2, new int[]{5, 2}, 50);
        System.out.println("r3 = " + r3 + " (expected 40)");

        // 간단한 통과/실패 표시
        boolean ok = (r1 == 100) && (r2 == 90) && (r3 == 40);
        System.out.println(ok ? "✅ PASS" : "❌ FAIL");
    }
}