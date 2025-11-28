import java.util.*;

class UserSolution {
	
	// N보다 큰 수 중 가장 작은 2의 제곱수. 세그먼트 트리 구성의 핵심 변수
	static int p;
	// 세그먼트 트리 배열
	static int[] tree;
	// 줄 번호로 관리되는 라인 배열, writeWord에서 주어지는 ID로 관리되는 words 배열
	static Line[] lines;
	static Area[] words;
	
	/*
	 * 각 라인을 관리하는 클래스
	 * 라인 내 단어 또는 빈 칸 구간을 관리하는 이중 연결 리스트 구현
	 * 경계 구간 관리를 위한 head, tail 객체 존재
	 */
	static class Line {
		Area head, tail;
	}
	
	/*
	 * 라인 내 각 구간을 관리하는 클래스
	 * 각 구간의 시작과 끝 범위, 해당 구간이 속해 있는 줄의 번호, 구간 분류 필드
	 * 구간 객체를 찾는 즉시 양방향 구간 객체를 찾기 위해 prev, next 필드를 가짐 -> 이중 연결 리스트
	 */
	static class Area {
		int start, end, lineIdx;
		char state; // 구간 분류 -> w: 단어, b: 빈 칸, d: 더미 데이터
		Area prev, next;
		
		public Area(int start, int end, int lineIdx, char state) {
			this.start = start;
			this.end = end;
			this.lineIdx = lineIdx;
			this.state = state;
		}
	}
	
	public void init(int N, int M)
	{
		// 일단 라인, 단어 세팅부터(단어의 ID가 1-based이므로 배열 words 배열 크기도 일치시켜야 함)
		lines = new Line[N];
		words = new Area[50001];
		for(int i=0; i<N; i++) {
			Line line = new Line();
			// 초기 세팅 : 모든 라인 객체는 항상 라인 전체에 해당하는 빈 공간을 하나씩 가지고 시작.
			// 경계 구간인 head, tail과 라인 전체 구간을 의미하는 초기 구간 객체 생성
			line.head = new Area(-1, -1, i, 'd');
			line.tail = new Area(M, M, i, 'd');
			Area initArea = new Area(0, M-1, i, 'b');
			
			// 연결 리스트를 이용한 경계 세팅
			line.head.next = initArea;
			line.tail.prev = initArea;
			initArea.prev = line.head;
			initArea.next = line.tail;
			
			lines[i] = line;
		}
		
		/*
		 * 세그먼트 트리 초기 세팅. 각 구간별로 최대값이 더 큰 쪽이 위로 올라와야 함.
		 * 초기값은 항상 M이다.
		 * < 피드백 1 >
		 * Arrays.fill은 사용하면 안된다. p+N보다 큰 항에도 숫자가 채워지는 사고를 막아야 하기 때문이다.
		 * 이 점을 생각하지 못해 p+N 이상의 값도 크기 M을 가지게 되었고, 이로 인해 다른 로직에서 NullPointerException을 일으켰다.
		 */
		p = 1; // N보다 큰 수 중 가장 작은 2의 제곱수를 구한다.
		while(p < N) p = p << 1;
		tree = new int[2 * p];
		
		// p ~ p+N-1까지의 모든 항에 M을 입력한다.
		// 해당 범위가 세그먼트 트리의 리프 노드이다.
		for(int i=0; i<N; i++) {
			tree[p+i] = M;
		}
		
		// 이후 상위 노드들을 하나씩 순회하면서 각 구간의 최대값을 갱신한다.
		for(int i=p-1; i>0; i--) {
			tree[i] = Math.max(tree[i << 1], tree[i << 1 | 1]);
		}
	}
	
	/**
	 * writeWord() 메서드에서 호출
	 * 세그먼트 트리 탐색으로 빈 칸의 최대 크기가 len 이상이면서 번호가 제일 낮은 라인을 찾는다.
	 * 루트 노드는 모든 리프 노드 중의 최대값을 가진다. 즉, 루트 노드의 값이 len보다 작으면 단어 등록 불가
	 * 왼쪽 서브 노드부터 탐색한다. 해당 노드의 크기가 len 이상인 경우 왼쪽 서브 노드, 그렇지 않은 경우 오른쪽 서브 노드로 이동
	 * @param len : 작성하려는 단어의 길이
	 * @return 어떤 라인에도 입력할 수 없는가? -1 / len 이상이면서 가장 인덱스가 낮은 라인의 인덱스 반환
	 */
	static int searchIdx(int len) {
		// 루트 노드의 값이 len보다 작다면 어느 쪽에도 단어를 적을 수 없음
		if(tree[1] < len) return -1;
		
		// 왼쪽 노드의 값이 len보다 크거나 같으면 왼쪽으로, 아니면 오른쪽으로
		int node = 1;
		while(node < p) {
			if(tree[node << 1] >= len) node *= 2;
			else node = node * 2 + 1;
		}
		
		// 최대 길이가 len 이상인 행 인덱스 중 제일 작은 값을 반환
		return node - p;
	}
	
	/**
	 * writeWord(), eraseWord() 메서드에서 호출
	 * 단어 작성 후 해당 라인의 빈 칸 최대값을 갱신할 때, 단어를 지운 후 해당 라인의 빈 칸 길이 최대값을 갱신할 때 호출하여 세그먼트 트리 최신화
	 * @param idx : 라인 인덱스. 해당 라인에 변화가 존재함을 의미
	 * @param len : 현재 라인에 존재하는 가장 큰 빈 칸의 길이
	 */
	void updateTree(int idx, int len) {
		// 현재 라인에 해당하는 리프 노드 인덱스
        int node = p + idx;
        tree[node] = len;
        
        // 한 레벨씩 올라가면서 상위 노드 전체의 구간별 최대값 갱신
        while(node > 1) {
            node /= 2;
            tree[node] = Math.max(tree[node * 2], tree[node * 2 + 1]);
        }
    }

	/**
	 * Query 2 : 단어 추가
	 * 단어장에 새 단어를 추가하려고 한다. 조건에 맞는 빈 칸의 위치를 찾아 새 단어를 작성한다.
	 * 만약 어느 곳에도 단어를 입력할 만큼 충분한 길이의 빈 칸이 없다면 단어를 작성하지 않는다.
	 * 호출 횟수는 50_000, 단어장의 라인 수가 최대 20_000이므로 시간복잡도 O(N)으로도 시간 초과가 발생할 수 있다.
	 * 시간 복잡도 O(logN) 수준의 알고리즘을 구현해야 함. 
	 * 
	 * 세그먼트 트리를 이용해 조건에 부합하는 라인 인덱스를 찾고, 구간 객체 리스트 관리를 통해 최대 120회 연산으로 입력할 수 있는 빈 칸을 찾을 수 있다.
	 * 왜 120회 연산인가? -> 문제 하단 제약사항에서 한 행별 단어의 최대 수가 60개 이하임을 보장하였음. 단어 - 빈 칸이 1회씩 반복되더라도 최대 120회 탐색이 보장됨.
	 * 찾아낸 빈 칸은 단어 작성 구간과, 남은 빈 칸 구간으로 분리되어 새로운 연결 관계를 형성한다. 또, 단어 객체는 words 배열에 등록된다.
	 * @param mId : 작성하려는 단어의 ID(최소 1, 최대 50_000)
	 * @param mLen : 작성하려는 단어의 길이(최소 2, 최대 M)
	 * @return searchIdx() 반환값. 탐색 실패시 -1, 탐색 성공시  해당 라인의 인덱스를 요구하므로 그 값 그대로 반환
	 */
	public int writeWord(int mId, int mLen)
	{
		// 세그먼트 트리로부터 조건에 부합하는 가장 작은 행 인덱스를 찾음
		// -1 : 없음 -> -1 반환
		int idx = searchIdx(mLen);
		if(idx == -1) return -1;
		
		// 행 객체 호출, 연결리스트 순회
		Line line = lines[idx];
		Area pt = line.head.next;
		
		// 1. 현재 라인 내에서 가장 큰 빈 칸의 길이 갱신
		// 2. 단어를 작성해야 할 빈 칸 객체 호출
		int maxLen = 0;
		Area target = null;
		while(pt != line.tail) {
			// 'w': 단어, 'd': 더미 객체
			if(pt.state != 'b') {
				pt = pt.next;
				continue;
			}
			// 조건에 부합하는 첫번째 빈 칸을 찾은 경우 : 타겟 객체 등록
			// < 피드백 2 >
			// mLen보다 작은 빈 칸도 그냥 continue로 넘기려고 했었다. 
			// 단어 작성으로 인해 mLen보다 작은 어떤 빈 칸이 새로운 최대값이 될 수 있다는 걸 간과했다.
			if(target == null && pt.end - pt.start + 1 >= mLen) {
				target = pt;
				maxLen = Math.max(pt.end - pt.start + 1 - mLen, maxLen);
			} 
			else maxLen = Math.max(pt.end - pt.start + 1, maxLen);
			pt = pt.next;
		}
		// 3. 현재 라인 내 가장 큰 빈 칸의 길이를 세그먼트 트리에 적용 및 최신화
		updateTree(idx, maxLen);
		
		// 4. 단어 객체 생성 및 단어 등록
		// 단어 길이 == 빈 칸 길이면 빈 칸 객체 분류만 변경 후 등록
		// 그 외에는 단어 객체 / 빈 칸 객체 분리 및 연결 관계 최신화
		if(target.end - target.start + 1 == mLen) {
			target.state = 'w';
			words[mId] = target;
		} else {
			Area word = new Area(target.start, target.start + mLen - 1, idx, 'w');
			target.start = target.start + mLen;
			word.next = target;
			word.prev = target.prev;
			word.prev.next = word;
			target.prev = word;
			words[mId] = word;
		}
		
		return idx;
	}

	/**
	 * Query 3 : 단어 제거
	 * 단어장 내 이미 작성된 단어를 지우려고 한다. ID 값을 통해 해당하는 단어를 찾아 지우고 빈 칸을 만들어야 한다.
	 * 만약 이미 제거되었거나, 처음부터 등록되지 않은 ID가 주어지면 단어를 제거할 수 없다.
	 * 호출 횟수는 5_000, ID 값으로 즉시 대상 객체를 찾아낼 수 있다면 그 이후의 연산은 많은 시간을 소모하지 않을 것이다. 
	 * 
	 * words 배열의 ID 인덱스에 해당하는 객체를 호출해두고, 해당 인덱스는 null값을 입력해 비운다.
	 * 이중 연결 리스트이므로 즉시 양 옆의 구간 객체로 방문할 수 있다. 양 옆의 구간 객체도 state == 'b'라면 하나의 빈 칸으로 병합한다.
	 * 새로운 길이의 빈 칸이 만들어졌으므로 세그먼트 트리 또한 갱신해야 한다. 단, 현재 등록된 값보다 큰 경우에만 업데이트 메서드를 호출한다.
	 * @param mId : 삭제하려는 단어의 ID(최소 1, 최대 55_000) -> 입력시의 ID 범위와 다르다. 따라서 50000 이상인 경우 더 볼 것도 없이 -1을 반환한다.
	 * @return 대상 단어 객체에 저장되어있는 lineIdx 값 반환. 대상 단어 객체가 없는 경우 -1 반환
	 */
	public int eraseWord(int mId)
	{
		// 범위 외 ID가 주어지거나 미등록 상태의 ID인 경우 -1 반환
		if(mId > 50000 || words[mId] == null) return -1;
		
		// 제거 대상 단어 객체 호출, 해당 ID는 미등록 처리
		Area word = words[mId];
		int line = word.lineIdx;
		words[mId] = null;
		
		// 1. 연결 리스트의 양쪽 확인. 빈 칸이 존재하면 병합하고 연결 관계를 갱신한다.
		// 이 과정에서 양 옆의 빈 칸 객체는 어떤 방식으로도 접근 불가능한 쓰레기 객체가 된다.
		word.state = 'b';
		if(word.prev.state == 'b') {
			word.start = word.prev.start;
			word.prev.prev.next = word;
			word.prev = word.prev.prev;
		}
		if(word.next.state == 'b') {
			word.end = word.next.end;
			word.next.next.prev = word;
			word.next = word.next.next;
		}
		
		// 2. 이후 병합된 공간 객체의 크기 확인. 세그먼트 트리 갱신
		// 빈 칸 병합 후에도 기존의 최대값보다 작으면 굳이 갱신할 필요 없음.
		int len = word.end - word.start + 1;
		if(len > tree[p+line]) updateTree(line, len);
		
		return line;
	}

}