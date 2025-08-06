package javas;

import java.io.*;
import java.util.*;

public class Main {
	private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static StringTokenizer st;
	private static final int NODE = 0;
	private static final int DISTANCE = 1;

	public static void main(String[] args) throws IOException {
		int t = getInt();
		
		StringBuilder answer = new StringBuilder();
		for (int tcase = 0; tcase < t; tcase++) {
			st = input();
			int n = getInt(st), d = getInt(st), c = getInt(st);
			List<int[]>[] computers = new ArrayList[n + 1];
			
			for (int i = 0; i < n + 1; i++) {
				computers[i] = new ArrayList<>(); 
			}
			
			for (int i = 0; i < d; i++) {
				st = input();
				int to = getInt(st), from = getInt(st), time = getInt(st);
				computers[from].add(new int[] {to, time});
			}
			
//			for (int i = 0; i < computers.length; i++) {
//				System.out.print("i: " + i + " -> ");
//				for (int j = 0; j < computers[i].size(); j++) {
//					System.out.print(Arrays.toString(computers[i].get(j)) + " ");
//				}
//				System.out.println();
//			}
			int[] result = daijkstra(computers, n, c);
			System.out.println(result[0] + " " + result[1]);
		}
	}
	
	public static int[] daijkstra(List<int[]>[] list, int n, int start) {
		int computer = 0;
		int distanceResult = 0;
//		Queue<int[]> queue = new PriorityQueue((c1, c2) -> Integer.compare(c1[1], c2[1]));
		Queue<int[]> queue = new PriorityQueue<>((a, b) -> Integer.compare(a[DISTANCE], b[DISTANCE]));
		queue.offer(new int[] {start, 0});
		int[] distances = new int[n + 1];
		
		for (int i = 0; i < n + 1; i++) {
			if (i != start) {
				distances[i] = Integer.MAX_VALUE;
			}
		}
//		System.out.println("distances: " + Arrays.toString(distances));
		
		while (!queue.isEmpty()) {
			int[] data = queue.poll();
			int node = data[NODE], distance = data[DISTANCE];
//			System.out.println("data: " + Arrays.toString(data));
			
			if (distance > distances[node]) {
//				System.out.println("continue");
				continue;
			}
			
			for (int[] com : list[node]) {
				System.out.println("com: " + Arrays.toString(com) + " -> " + (com[DISTANCE] + distance) + ", distance : " + distances[node]);
				if (distances[com[NODE]] > com[DISTANCE] + distance) {
					System.out.println("if");
//					System.out.println("if");
					distances[com[NODE]] = com[DISTANCE] + distance;
					queue.offer(new int[]{com[NODE], distances[NODE]});
//					queue.offer(new int[]{node, distances[NODE]});
				}
			}
		}
		
		for (int i = 0; i < n + 1; i++) {
			if (distances[i] != Integer.MAX_VALUE) {
				computer++;
//				distanceResult += distances[i];
				distanceResult = Math.max(distanceResult, distances[i]);
			}
		}
		
//		System.out.println("start: " + start + ", distances: " + Arrays.toString(distances));
		
		return new int[]{computer, distanceResult};
	}

	public static StringTokenizer input() throws IOException {
		return new StringTokenizer(br.readLine());
	}

	public static int getInt(StringTokenizer s) {
		return Integer.parseInt(s.nextToken());
	}

	public static int getInt() throws IOException {
		return Integer.parseInt(br.readLine());
	}
}
