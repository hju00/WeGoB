import java.util.*;

class UserSolution {
	
	
	static int l;	// 차량의 이동에너지 (3 ≤ L ≤ 500)
	static int n;	// 섬의 한 변의 길이 (9 ≤ N ≤ L * 30)
	static int bSize;	// 버킷 크기 (N / L)
	static int idx;		// 배열 인덱스
	
	// 현재 위치를 포함한 8방 탐색
	static int dy[] = {0, 1, 0, -1, 0, 1, 1, -1, -1};
	static int dx[] = {0, 0, 1, 0, -1, 1, -1, 1, -1};
	
	// BaseCamp 정보를 ID 별로 관리
	static HashMap<Integer, Integer> idToIdx;
	
	// BaseCamp 정보 관리
	static BaseCamp bcDB[];
	
	// union 시 조상 정보 관리
	static int parent[];
	
	// union 의 조상 id 관리
	static HashSet<Integer> parentSet;
	
	// 각 유니온의 채굴량 합을 저장
	static int union_sum[];
	
	// L 거리 이하의 BaseCamp를 찾기 위한 버킷
	static ArrayList<BaseCamp> bucket[][];
	
	static class BaseCamp implements Comparable<BaseCamp> {
		int y;
		int x;
		int id;
		int quan;
		
		public BaseCamp(int y, int x, int id, int quan) {
			super();
			this.y = y;
			this.x = x;
			this.id = id;
			this.quan = quan;
		}

		@Override
		public int compareTo(BaseCamp o) {
			if(this.quan == o.quan)	{
				if(this.y == o.y)
					return Integer.compare(this.x, o.x);
				return Integer.compare(this.y, o.y);
			}
			return Integer.compare(this.quan, o.quan);
		}
		
	}
	
	// x는 기존에 존재하는 베이스캠프, y는 새로 추가된 베이스캠프 : 이 형식을 꼭 지켜야함
	static void union(int x, int y) {
		int rootX = find(x);
		int rootY = find(y);
		
		if(rootX == rootY)	return;
		
		// 두 베이스캠프 중 우선순위 높은 베이스캠프를 조상으로 설정하기 위함
		TreeSet<BaseCamp> toComp = new TreeSet<>();
		toComp.add(bcDB[rootX]);
		toComp.add(bcDB[rootY]);
		
		// 기존 베이스캠프가 우선순위가 더 높을 경우
		if(toComp.first().id == bcDB[rootX].id) {
			// 조상을 rootX로 변경
			parent[rootY] = rootX;
			
			// 기존의 유니온 합에 새로 들어온 베이스캠프의 채굴량을 더해줌
			union_sum[rootX] += union_sum[rootY];
			
			// parent id 제거
			parentSet.remove(rootY);
		}			
		// 새로 추가된 베이스캠프가 우선순위가 더 높을 경우
		else {
			// 조상을 rootY로 변경
			parent[rootX] = rootY;
			
			// 기존에 존재하던 조상의 유니온 합을 새로운 조상의 합으로 갱신
			union_sum[rootY] += union_sum[rootX];
			
			// parent id 제거
			parentSet.remove(rootX);
		}
	}
	
	// union-find : find
	static int find(int x) {
		if(x == parent[x]) return x;
		return parent[x] = find(parent[x]);
	}
	
	// 버킷 범위 안인지 확인
	static boolean isIn(int y, int x) {
		return y >= 0 && y < bSize && x >= 0 && x < bSize;
	}
	
	// 멘헤튼 거리 계산
	static int calc(int y1, int x1, int y2, int x2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1);
	}
	
	void init(int L, int N){
		l = L;
		n = N;
		idx = 0;
		// N이 L로 나누어 떨어진다는 조건이 없으므로 + 1
		bSize = N / L + 1;
		
		// addBaseCamp 최대 호출 횟수 20000회
		bcDB = new BaseCamp[20001];
		idToIdx = new HashMap<>();
		union_sum = new int[20001];
		parent = new int[20001];
		parentSet = new HashSet<>();
		
		// 최대 30 X 30
		bucket = new ArrayList[bSize][bSize];
		for(int i = 0; i < bSize; i++)
			for(int j = 0; j < bSize; j++)
				bucket[i][j] = new ArrayList<>();
	}
	
	int addBaseCamp(int mID, int mRow, int mCol, int mQuantity){
		
		BaseCamp bc = new BaseCamp(mRow, mCol, mID, mQuantity);
		
		// id 해싱
		idToIdx.put(mID, idx);
		
		// bcDB 추가
		bcDB[idx] = bc;
		
		// parent 추가 (처음은 자기 자신)
		parent[idx] = idx;
		
		// union_sum 설정 (처음은 자기 자신)
		union_sum[idx] = bc.quan;
		
		// parent id 에 현재 추가하려는 idx 추가
		parentSet.add(idx);
		
		// 버킷 좌표
		int by = mRow / l;
		int bx = mCol / l;
		
		// 주변 버킷 탐색 - 9개의 버킷 X 버킷 안의 평균 베이스캠프 개수 22개 = 198개의 베이스캠프와 유니온?
		for(int d = 0; d < 9; d++)	{
			int nby = by + dy[d];
			int nbx = bx + dx[d];
			
			// 버킷 밖 나가면 패스
			if(!isIn(nby, nbx))		continue;
			
			for(BaseCamp nBC : bucket[nby][nbx])	{
				// 추가된 베이스캠프와의 거리
				int dist = calc(bc.y, bc.x, nBC.y, nBC.x);
				
				// l 보다 클 경우 제외
				if(dist > l)	continue;
				
				// 기존에 존재하던 베이스캠프와 유니온
				union(idToIdx.get(nBC.id), idx);
			}
		}
		
		// 버킷에 추가 
		// 마지막에 추가하는 이유 : 주변 버킷 탐색할 때 자기도 들어갈까봐
		bucket[by][bx].add(bc);
		
		// 마지막은 idx 1증가
		int ret = find(idx);
		idx++;
		
		// 추가한 베이스캠프의 union_sum return
		return union_sum[ret];
	}
	
	// 호출 횟수 500회 라서 union_sum 순회해도 될듯?
	int findBaseCampForDropping(int K){		
		
		// 광물의 최대값이 K 이상인 베이스캠프를 넣을 후보 리스트
		TreeSet<BaseCamp> candidate = new TreeSet<>();
		
		for(int id : parentSet) 
			if(union_sum[id] >= K)
				candidate.add(bcDB[id]);
		
		if(candidate.isEmpty())
			return -1;
		
		return candidate.first().id;
	}

}

