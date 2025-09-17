import java.io.*;
import java.util.*;

class Main {
    private static long[] arr;
    private static long[] tree;

    // 세그먼트 트리 초기화
    private static long init(int start, int end, int node){
        // 
        if(start == end) return tree[node] = arr[start];

        int mid = (start + end) / 2;

        return tree[node] = init(start, mid, node*2) + init(mid+1, end, node*2 + 1);
    }

    private static void update(int start, int end, int node, int index, long dif){
        if(index < start || index > end) return;

        tree[node] += dif;
        if(start == end) return;

        int mid = (start + end) / 2;

        update(start, mid, node * 2, index, dif);
        update(mid+1, end, node*2+1, index, dif);
    }

    private static long sum(int start, int end, int node, int left, int right){
        if(left > end || right < start) return 0;

        if(left <= start && end <= right) return tree[node];

        int mid = (start + end) / 2;

        return sum(start, mid, node*2, left, right) + sum(mid+1, end, node*2+1, left, right);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        StringTokenizer st = new StringTokenizer(br.readLine());

        int N = Integer.parseInt(st.nextToken());
        int M = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());

        arr = new long[N+1];

        int k = (int)Math.ceil(Math.log(N) / Math.log(2)) + 1;
        int size = (int)Math.pow(2, k);
        
        tree = new long[size];

        for(int i = 1; i <= N; i++){
            arr[i] = Long.parseLong(br.readLine());
        }

        init(1, N, 1);

        StringBuilder sb = new StringBuilder();
        while(M > 0 || K > 0){
            st = new StringTokenizer(br.readLine());

            int cmd = Integer.parseInt(st.nextToken());

            if(cmd == 1){
                M--;
                int index = Integer.parseInt(st.nextToken());
                long n = Long.parseLong(st.nextToken());

                long dif = n - arr[index];
                arr[index] = n;
                
                update(1, N, 1, index, dif);
            } else{
                K--;

                int left = Integer.parseInt(st.nextToken());
                int right = Integer.parseInt(st.nextToken());

                sb.append(sum(1, N, 1, left, right) + "\n");
            }
        }

        System.out.println(sb);
    }
}
