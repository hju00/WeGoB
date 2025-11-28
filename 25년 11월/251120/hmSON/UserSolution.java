import java.util.HashMap;
import java.util.PriorityQueue;

public class UserSolution {
	
	/*
	 * [핵심 아이디어]
	 * - N이 매우 크므로 배열 대신 '구간(Area)' 객체를 만들어 관리한다.
	 * - 효율적인 탐색을 위해 두 가지 자료구조를 혼합하여 사용한다.
	 * 1. PriorityQueue : '건설'을 위해 가장 긴 빈 공간을 빠르게 추출 (O(log K)). 크기가 동일하면 제일 왼쪽에 있는 빈 공간을 추출.
	 * 2. HashMap : '철거'를 위해 주소값으로 특정 건물 객체에 즉시 접근 (O(1)).
	 * - 공간의 분할과 병합을 O(1)에 처리하기 위해 각 Area 객체를 이중 연결 리스트(prev, next)로 연결한다.
	 * */
	
	static int n;
	static PriorityQueue<Area> q;
	static HashMap<Integer, Area> buildings;
	// 일관된 코드 양식을 지키기 위해 헤더 노드와 테일 노드를 추가한다.
	static Area head, tail;
	
	static class Area {
		int left, right, len;
		// 'a': 빈 공간, 'b': 빌딩, 'd': 빈 공간도 빌딩도 아닌, 삭제된 객체임을 명시하는 지연 삭제 태그
		char state;
		// 이중 연결 리스트
		Area prev, next;
		
		public Area(int left, int right, char state) {
			this.left = left;
			this.right = right;
			this.state = state;
			len = right - left + 1;
		}
	}

	public void init(int N){
		n = N;
		Area allArea = new Area(0, N-1, 'a');
		head = new Area(-1, -1, 'n');
		tail = new Area(N, N, 'n');
		head.next = allArea;
		tail.prev = allArea;
		allArea.prev = head;
		allArea.next = tail;
		
		q = new PriorityQueue<>((a, b) -> a.len != b.len ? b.len - a.len : a.left - b.left);
		q.offer(allArea);
		
		buildings = new HashMap<>();
	}

	public int build(int mLength) {
		// 1. 일단 공간이 제일 넓은 걸 찾는다.
		// 1-1. 그 공간의 크기가 mLength 이상인가? 이걸 우선순위 큐에서 뺀다.
		// 1-2. 그렇지 않은가? 빌딩을 세울 수 없다. -1 반환.
		// 2. 빌딩을 세울 위치를 계산한다. 빌딩으로부터 왼쪽, 오른쪽의 빈 공간 크기는 동일하거나 오른쪽이 1 커야 한다.
		// 3. 총 3개의 Area 객체가 새로 생성된다. 왼쪽 공터, 오른쪽 공터, 빌딩
		// 3-1. 각 구역의 왼쪽 끝이 주소값이다.
		// 3-2. 우선순위 큐에는 양쪽 공터 객체만 추가한다. 해시맵에는 3개의 공간 객체를 전부 추가한다. 이때, key는 주소값이다.
		while(!q.isEmpty() && q.peek().state == 'd') q.poll();
		if(q.isEmpty() || q.peek().len < mLength) return -1;
		Area max = q.poll();
		
		int len = (max.len - mLength);
		if(len == 0) {
			max.state = 'b';
			buildings.put(max.left, max);
			return max.left;
		}
		int leftEdge = max.left + len/2;
		int rightEdge = max.right - (len/2 + (len%2 == 1 ? 1 : 0));
		Area building = new Area(leftEdge, rightEdge, 'b');
		Area rightArea = new Area(rightEdge + 1, max.right, 'a');
		max.right = leftEdge - 1;
		max.len = leftEdge - max.left;
		
		max.next.prev = rightArea;
		rightArea.next = max.next;
		rightArea.prev = building;
		building.next = rightArea;
		if(len == 1) {
			max.next = null;
			max.prev.next = building;
			building.prev = max.prev;
			max.prev = null;
		} else {
			building.prev = max;
			max.next = building;
			q.add(max);
		}
		
		q.add(rightArea);
		buildings.put(leftEdge, building);
		
		return leftEdge;
	}

	public int demolish(int mAddr) {
		// 1. 해당 주소값을 가지는 빌딩을 찾는다. 
		// 1-1. 주소값이 mAddr인 빌딩이 없는가? -> -1 반환
		// 1-2. 주소값이 mAddr인 빌딩이 존재하는가? -> 호출
		// 2. 먼저 빌딩, 왼쪽 공터, 오른쪽 공터 세 곳을 합친다. 
		// 2-1. 양쪽 공터의 state 속성을 사용하여 공터인 경우에만 추가한다.
		// 3. 빌딩 객체는 해시맵에서 지우고, 기존의 공터 객체는 state를 'd'로 변경하여 제거되었음을 명시한다.
		// 4. 이후 새로 발생한 빈 공터는 우선순위 큐에 등록한다.
		Area target = buildings.getOrDefault(mAddr, null);
		if(target == null) return -1;
		
		buildings.remove(target.left);
		int res = target.len;
		
		if(target.next.state == 'a') {
			target.len += target.next.right - target.right;
			target.right = target.next.right;
			target.next.state = 'd';
			target.next = target.next.next;
			target.next.prev = target;
		}
		
		if(target.prev.state == 'a') {
			target.len += target.left - target.prev.left;
			target.left = target.prev.left;
			target.prev.state = 'd';
			target.prev = target.prev.prev;
			target.prev.next = target;
		}
		
		target.state = 'a';
		q.add(target);
		
		return res;
	}

}
