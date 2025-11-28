package Pro_25005;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class UserSolution_25005 {
    
	static final int INF = Integer.MAX_VALUE;
	static int v;
	static int[] amountCharge, distVirus;
	static HashMap<Integer, Node> nodes;
	static List<Node>[] graph;
	
	static class Car implements Comparable<Car> {
		int to, time, power;
		
		public Car(int to, int time, int power) {
			this.to = to;
			this.time = time;
			this.power = power;
		}

		@Override
		public int compareTo(Car o) {
			return time - o.time;
		}
	}
	
	static class Virus implements Comparable<Virus> {
		int to, time; 
		
		public Virus(int to, int time) {
			this.to = to;
			this.time = time;
		}
		
		@Override
		public int compareTo(Virus o) {
			return time - o.time;
		}
	}
	
	static class Node {
		int to, time, power;
		boolean removed;
		
		public Node(int to, int time, int power) {
			this.to = to;
			this.time = time;
			this.power = power;
			removed = false;
		}
	}
	
	public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
		v = N;
		amountCharge = mCharge;
		graph = new ArrayList[v];
		distVirus = new int[v];
		for(int i=0; i<v; i++) {
			graph[i] = new ArrayList<>();
		}
		nodes = new HashMap<>();
		
		for(int i=0; i<K; i++) {
			Node node = new Node(eCity[i], mTime[i], mPower[i]);
			nodes.put(mId[i], node);
			graph[sCity[i]].add(node);
		}
	}

	public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
		Node newNode = new Node(eCity, mTime, mPower);
		nodes.put(mId, newNode);
		graph[sCity].add(newNode);
	}

	public void remove(int mId) {
		Node target = nodes.get(mId);
		target.removed = true;
	}

	public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
		dijkVirus(M, mCity, mTime);
		return dijkCar(B, sCity, eCity);
	}
	
	public int dijkCar(int maxCharged, int start, int end) {
		int[][] distCar = new int[v][maxCharged+1];
		for(int i=0; i<v; i++) {
			Arrays.fill(distCar[i], INF);
		}
		
		PriorityQueue<Car> q = new PriorityQueue<>();
		distCar[start][maxCharged] = 0;
		q.add(new Car(start, 0, maxCharged));
		
		while(!q.isEmpty()) {
			Car cur = q.poll();
			if(distCar[cur.to][cur.power] < cur.time || cur.time >= distVirus[cur.to]) continue;
			
			if(cur.to == end) return cur.time;
			
			if(cur.power < maxCharged) {
				int newCharge = Math.min(maxCharged, cur.power + amountCharge[cur.to]);
				int newTime = cur.time + 1;
				if(newTime < distVirus[cur.to] && newTime < distCar[cur.to][newCharge]) {
					distCar[cur.to][newCharge] = newTime;
					q.add(new Car(cur.to, newTime, newCharge));
				}
			}
			
			for(Node next : graph[cur.to]) {
				if(next.removed) continue;
				
				int nextTime = cur.time + next.time;
				if(distVirus[next.to] <= nextTime) continue;
				
				int nextCharge = cur.power - next.power;
				if(nextCharge < 0 || nextTime >= distCar[next.to][nextCharge]) continue;
				
				distCar[next.to][nextCharge] = nextTime;
				q.add(new Car(next.to, nextTime, nextCharge));
			}
		}
		
		return -1;
	}
	
	public void dijkVirus(int m, int city[], int time[]) {
		Arrays.fill(distVirus, INF);
		
		PriorityQueue<Virus> q = new PriorityQueue<>();
		for(int i=0; i<m; i++) {
			distVirus[city[i]] = time[i];
			q.add(new Virus(city[i], time[i]));
		}
		
		while(!q.isEmpty()) {
			Virus cur = q.poll();
			if(distVirus[cur.to] < cur.time) continue;
			
			for(Node next : graph[cur.to]) {
				if(next.removed) continue;
				
				int newTime = cur.time + next.time;
				if(distVirus[next.to] <= newTime) continue;
				distVirus[next.to] = newTime;
				q.add(new Virus(next.to, newTime));
			}
		}
	}
	
}