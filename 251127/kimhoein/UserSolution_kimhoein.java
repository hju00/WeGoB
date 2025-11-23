
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
	static class Node implements Comparable<Node>
	{
		int start;
		int distance;
		
		Node(int start, int distance)
		{
			this.start = start;
			this.distance = distance;
		}

		@Override
		public int compareTo(Node o) {
			if(this.distance == o.distance)
			{
				return Integer.compare(this.start,o.start);
			}
			return Integer.compare(o.distance,this.distance);
		}
		
		
	}
	static int N;
	static PriorityQueue<Node> pq = new PriorityQueue<>();
	static HashMap<Integer,Integer> void_start_hash = new HashMap<>();
	static HashMap<Integer,Integer> void_end_hash = new HashMap<>();
	static HashMap<Integer,Integer> build = new HashMap<>();
	
	public void init(int N){
		this.N = N;
		pq.add(new Node(0,N));
		pq.clear();
		void_start_hash.clear();
		void_end_hash.clear();
		build.clear();
		
		// 초기 빈 공간 등록
		void_start_hash.put(0, N);
		void_end_hash.put(N, N);
		pq.add(new Node(0, N));
	}

	public int build(int mLength) {
		//System.out.println("mLength : " + mLength + " " +  pq.peek().distance + " " + pq.peek().start);
		
		while(!pq.isEmpty()) {
			Node node = pq.peek();
			
			// [수정 3] 유효성 검사 (Lazy Deletion)
			// PQ에서 본 정보가 실제 해시맵(현재 상태)과 다르면, 이미 사라진 공간이므로 버림
			if(!void_start_hash.containsKey(node.start) || void_start_hash.get(node.start) != node.distance) {
				pq.poll(); // 유효하지 않은 노드 제거
				continue;
			}

			// 가장 큰 공간도 작으면 실패
			if(node.distance < mLength) {
				return -1; 
			}
			
			// 건설 가능하면 poll
			pq.poll();
			
			// 기존 빈 공간 삭제
			void_start_hash.remove(node.start);
			void_end_hash.remove(node.start + node.distance);
			
			int temp = (node.distance - mLength)/2;
			int buildStart = node.start + temp; // 건물이 지어질 위치
			
			// 앞쪽 남은 공간 처리
			if(temp > 0) {
				void_start_hash.put(node.start, temp);
				void_end_hash.put(node.start + temp, temp);
				pq.add(new Node(node.start, temp));
			}
			
			// 뒤쪽 남은 공간 처리
			// [수정 4] 나머지 계산 로직 명확화 (기존 로직도 동작은 하지만 가독성 개선)
			int remainingBack = node.distance - mLength - temp;
			if(remainingBack > 0) {
				void_start_hash.put(buildStart + mLength, remainingBack);
				void_end_hash.put(buildStart + mLength + remainingBack, remainingBack);
				pq.add(new Node(buildStart + mLength, remainingBack));
			}
			
			// [수정 5] 건물 등록 위치 이동
			// 기존에는 if문 안에 있어서, 남은 공간이 0이면 건물이 등록 안 되는 버그가 있었음
			build.put(buildStart, mLength);
			
			//System.out.println("node.start : " + buildStart + " " + mLength);
			return buildStart;
		}
		
		return -1;
	}

	public int demolish(int mAddr) {
		
		if(build.containsKey(mAddr))
		{
			Node Nbuild = new Node(mAddr,build.get(mAddr));
			int start=Nbuild.start;
			int d=Nbuild.distance;
			int size = build.get(mAddr);
			
			if(void_end_hash.containsKey(mAddr))
			{
				start = mAddr- void_end_hash.get(mAddr);
				d += void_end_hash.get(mAddr);
				void_end_hash.remove(mAddr);
				//pq.remove(mAddr);
			}
			
			if(void_start_hash.containsKey(mAddr + build.get(mAddr)))
			{
				d += void_start_hash.get(mAddr + build.get(mAddr));
				void_start_hash.remove(mAddr + build.get(mAddr));
				//pq.remove(mAddr);
			}
			
			build.remove(mAddr);
			void_start_hash.put(start, d);
			void_end_hash.put(start+d, d);
			
			pq.add(new Node(start,d));
			return size;
		}
		
		return -1;
	}
}
