import java.io.*;
import java.util.*;

public class Main {
	private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static StringTokenizer st;
	private static final int INDEX = 0;
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
				computers[from].add(new int[] { to, time });
			}

			int[] result = daijkstra(computers, n, c);
			System.out.println(result[0] + " " + result[1]);
		}
	}

	public static int[] daijkstra(List<int[]>[] list, int n, int start) {
		int computer = 0;
		int distanceResult = 0;
		Queue<int[]> queue = new PriorityQueue<>((a, b) -> Integer.compare(a[DISTANCE], b[DISTANCE]));
		queue.offer(new int[] { start, 0 });
		int[] distances = new int[n + 1];

		for (int i = 0; i < n + 1; i++) {
			if (i != start) {
				distances[i] = Integer.MAX_VALUE;
			}
		}

		while (!queue.isEmpty()) {
			int[] data = queue.poll();
			int current = data[INDEX], distance = data[DISTANCE];

			if (distance > distances[current]) {
				continue;
			}

			for (int[] nextNode : list[current]) {
				int cost = nextNode[DISTANCE] + distance;
				if (distances[nextNode[INDEX]] > cost) {
					distances[nextNode[INDEX]] = cost;
					queue.offer(new int[]{nextNode[INDEX], cost});
				}
			}
		}

		for (int i = 1; i < n + 1; i++) {
			if (distances[i] != Integer.MAX_VALUE) {
				computer++;
				distanceResult = Math.max(distanceResult, distances[i]);
			}
		}

		return new int[] { computer, distanceResult };
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
