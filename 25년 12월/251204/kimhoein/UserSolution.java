package study;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

class UserSolution {
	static class Tomas
	{
		int id;
		int start;
		int end;
		int distance;
		int transfer;
		Tomas(int id, int start, int end, int distance)
		{
			this.id = id;
			this.start = start;
			this.end = end;
			this.distance = distance;
		}
	}
	
	static int N;
	static int K;
	
	HashMap<Integer, Tomas> hash = new HashMap<>();
	
	public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
		this.N = N;
		this.K = K;
		hash = new HashMap<>();
		
		for(int i=0; i<K; i++) {
			Tomas tomas = new Tomas(mId[i], sId[i], eId[i], mInterval[i]);
			hash.put(tomas.id, tomas);
		}
		return;
	}

	public void add(int mId, int sId, int eId, int mInterval) {
		Tomas tomas = new Tomas(mId, sId, eId, mInterval);
		hash.put(tomas.id, tomas);
		
		return;
	}

	public void remove(int mId) {
		hash.remove(mId);
		return;
	}

	public int calculate(int sId, int eId) {
		HashSet<Integer> set = new HashSet<>(); // 방문 처리
		Queue<Tomas> q = new LinkedList<>();
		
		for(int i: hash.keySet()) {
			
			Tomas t = hash.get(i);
			
			if(sId > t.end || sId < t.start) continue;
			
			if(sId >= t.start && sId <= t.end && (sId - t.start) % t.distance == 0) {
				if(eId >= t.start && eId <= t.end && (eId - t.start) % t.distance == 0) {
					return 0;
				}

				set.add(i);
				t.transfer =0;
				q.add(t);
			}
		}
		
		while(!q.isEmpty()) {
			Tomas tomas = q.poll();

			for(int i : hash.keySet())
			{
				if(set.contains(i)) continue;
				
				Tomas t = hash.get(i);
				
				if(!canTransfer(tomas,t)) continue;	// 만날 수 있는지 체크
					
				//만약 환승이 가능한데 그 열차가 목적지에 도착 한다면?
				if(eId >= t.start && eId <= t.end && (eId - t.start) % t.distance == 0) return tomas.transfer + 1;
				
				set.add(i);
				t.transfer = tomas.transfer + 1;
				q.add(t);
				
			}
		}
		
		return -1;
	}
	
	static boolean canTransfer(Tomas a, Tomas b) {
		if(a.end < b.start  || a.start > b.end ) return false;
		
		int countA = (a.end - a.start) / a.distance;
		int countB = (b.end - b.start) / b.distance;
		
		if(countA > countB) {
			Tomas temp = a;
			a = b;
			b = temp;
		}
		
		for(int i=a.start; i<= a.end; i += a.distance) {
			if(i < b.start) continue;
			if(i > b.end) continue;
			
			if((i-b.start) % b.distance == 0) return true;
		}
		
		return false;
	}

}