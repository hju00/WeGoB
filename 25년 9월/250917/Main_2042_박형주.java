import java.io.*;
import java.util.*;

class Main {
    // N(1 ≤ N ≤ 1,000,000), M(1 ≤ M ≤ 10,000), K(1 ≤ K ≤ 10,000)
    static int N, M, K;
    static long[] arr, tree;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        arr = new long[N];
        for (int i = 0; i < N; i++)
            arr[i] = Long.parseLong(st.nextToken());

        tree = new long[N * 4];
        init(1, 0, N - 1);

        for (int i = 0; i < M + K; i++) {
            st = new StringTokenizer(br.readLine());

            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            long c = Long.parseLong(st.nextToken());

            switch (a) {
                // b(1 ≤ b ≤ N)번째 수를 c로 바꾸기
                case 1:
                    update(1, 0, N - 1, b - 1, c);
                    break;

                // b(1 ≤ b ≤ N)번째 수부터 c(b ≤ c ≤ N)번째 수까지의 합 구하기
                case 2:
                    System.out.println(query(1, 0, N - 1, b - 1, (int) c - 1));
                    break;
            }
        }
    }

    static long init(int node, int start, int end) {
        if (start == end)
            return tree[node] = arr[start];

        int mid = (start + end) / 2;
        return tree[node] = init(node * 2, start, mid) + init(node * 2 + 1, mid + 1, end);
    }

    static long query(int node, int start, int end, int left, int right) {
        if (left > end || right < start)
            return 0;
        if (left <= start && end <= right)
            return tree[node];

        int mid = (start + end) / 2;
        return query(node * 2, start, mid, left, right) + query(node * 2 + 1, mid + 1, end, left, right);
    }

    static void update(int node, int start, int end, int index, long newValue) {
        if (index < start || index > end)
            return;
        if (start == end) {
            tree[node] = newValue;
            arr[index] = newValue;
            return;
        }
        int mid = (start + end) / 2;
        update(node * 2, start, mid, index, newValue);
        update(node * 2 + 1, mid + 1, end, index, newValue);
        tree[node] = tree[node * 2] + tree[node * 2 + 1];
    }
}
