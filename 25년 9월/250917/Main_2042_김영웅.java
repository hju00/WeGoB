import java.io.*;
import java.util.*;

public class Main {
	static BufferedReader br;
	static int UPDATE = 1;
	static int N, M, K;
	static long[] numbers;
	static long[] segtree;
	static StringBuilder sb;
	
	public static void main(String[] args) throws IOException {
		br = new BufferedReader(new InputStreamReader(System.in));
		init();
		System.out.println(sb);			
	}
	
	static void init() throws IOException {		
		sb = new StringBuilder();
		
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());
		
		numbers = new long[N + 1];  // index를 1부터 시작하기 위해 N + 1만큼 초기화
		for (int i = 1; i <= N; i++) numbers[i] = Long.parseLong(br.readLine());
		
		segtree = new long[N * 4];  // seg tree 크기 초기화
		segInit(1, 1, N);  // seg tree init
		
		for (int limit = 0; limit < M + K; limit++) {
			st = new StringTokenizer(br.readLine());
			int command = Integer.parseInt(st.nextToken()), op1 = Integer.parseInt(st.nextToken()); 
			long op2 = Long.parseLong(st.nextToken());
			
			if (command == UPDATE) {
				long diff = op2 - numbers[op1];  // 기존의 값에서 변경 값의 차를 구함 -> 다른 세그 트리에 반영해주기 위해서
                
				numbers[op1] = op2;  // 값 변경
				update(1, 1, N, op1, diff);
			} else sb.append(sum(1, 1, N, op1, (int) op2)).append("\n");
		}
	}
	
	static void segInit(int node, int start, int end) {  // 양쪽 자식으로 계속 파고 들어가면서 segtree 초기화
		if (start == end) {
			segtree[node] = numbers[start];
			return;
		}
		
		int mid = (start + end) / 2, leftChild = node * 2, rightChild = node * 2 + 1; 
		segInit(leftChild, start, mid);  // 왼쪽 자식으로 더 들어감
		segInit(rightChild, mid + 1, end);  // 오른쪽 자식으로 더 들어감
	
		segtree[node] = segtree[leftChild] + segtree[rightChild];  // 양쪽 자식에 합을 본인의 값으로 지정
	}
	
	static long sum(int node, int start, int end, int left, int right) {  // node == 현재 노드, start | end == numbers의 전체 범위
        // left ~ right 까지의 구간합을 구함
		if (right < start || left > end) return 0;  // left나 right가 찾을 수 없는 범위면 return 0
		if (left <= start && end <= right) return segtree[node];  // left가 start에 포함되고(left <= start) end가 right에 포함되면(end <= right) == left와 right가 찾을 수 있는 범위면
		
		int mid = (start + end) / 2, leftChild = node * 2, rightChild = node * 2 + 1;
		return sum(leftChild, start, mid, left, right) + sum(rightChild, mid + 1, end, left, right);
	}

	static void update(int node, int start, int end, int index, long diff) {  // node == 현재 노드 인덱스, start | end == numbers의 전체 범위
		if (index < start || index > end) return;
		
		segtree[node] += diff;  // 다른 세그트리들에 변경 값 반영
		
		if (start == end) return;
		
		int mid = (start + end) / 2;
		update(node * 2, start, mid, index, diff);
		update(node * 2 + 1, mid + 1, end, index, diff);
	}
}