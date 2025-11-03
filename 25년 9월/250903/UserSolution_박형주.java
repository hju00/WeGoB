import java.util.*;

class UserSolution
{
	// init 에서 주어진 변수
	static int n, m, l;
	
	// 자료구조
	static ArrayList<Taxi> bucket[][];
	static Taxi[] taxiDB;
	static TreeSet<Taxi> bestTaxi;
	
	// 9개의 버킷 탐색을 위한 center + 8방 탐색
	static int dy[] = {0, 1, 0, -1, 0, 1, 1, -1, -1};
	static int dx[] = {0, 0, 1, 0, -1, 1, -1, 1, -1};
	
	// 버킷 배열 범위 안인지 확인하는 함수
	static boolean isIn(int y, int x)	{ 
		return y >= 0 && y < 10 && x >= 0 && x < 10;
	}
	
	static class dist implements Comparable<dist> {
		int distance;
		int id;
		public dist(int distance, int id) {
			super();
			this.distance = distance;
			this.id = id;
		}
		@Override
		public int compareTo(dist o) {
			// 가장 가까운 거리인 택시가 여러 대이면 그 택시들 중에서 번호가 가장 작은 택시
			if(this.distance == o.distance)
				return Integer.compare(this.id, o.id);
			// 가장 가까운 거리에 있는 택시
			return Integer.compare(this.distance, o.distance);
		}
		
	}
	
	static class Taxi implements Comparable<Taxi>{
		int x;
		int y;
		int id;
		int moveDist = 0;
		int rideDist = 0;
		
		public Taxi(int x, int y, int id) {
			super();
			this.x = x;
			this.y = y;
			this.id = id;
		}

		@Override
		public int compareTo(Taxi o) {
			// 손님을 태우고 이동한 총 거리가 같은 경우 택시 번호가 낮을수록 우선 순위가 높다
			if(this.rideDist == o.rideDist)
				return Integer.compare(this.id, o.id);
			// 손님을 태우고 이동한 총 거리가 가장 큰 순서
			return Integer.compare(o.rideDist, this.rideDist);
		}

	}
	
	// 맨해튼 거리 계산 함수
	static int calc(int x1, int y1, int x2, int y2)	{
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}
	
	// 100 X 2,000 = 200,000회
	public void init(int N, int M, int L, int[] mXs, int[] mYs)
	{
		n = N;
		m = M;
		l = L;
		bucket = new ArrayList[10][10];
		taxiDB = new Taxi[M + 1];
		bestTaxi = new TreeSet<>();
		
		// 연산 100 회
		for(int i = 0; i < 10; i++)
			for(int j = 0; j < 10; j++)
				bucket[i][j] = new ArrayList<>();
		
		// 연산 M 회 (최대 2000)
		for(int i = 1; i <= M; i++)	{
			int x = mXs[i - 1];
			int y = mYs[i - 1];
			
			Taxi t = new Taxi(x, y, i);
			
			bucket[y / l][x / l].add(t);
			taxiDB[i] = t;
			bestTaxi.add(t);
		}
		
		return;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY)
	{
		int cy = mSY / l;
		int cx = mSX / l;
		
		// 출발점을 포함한 주위 9개의 버킷 안의 택시 들 중에서 가장 가까운 택시 찾기
		TreeSet<dist> candidates = new TreeSet<>();
		for(int d = 0; d < 9; d++) {
			int nby = cy + dy[d];
			int nbx = cx + dx[d];
			
			if(!isIn(nby, nbx))
				continue;
			
			for(Taxi t : bucket[nby][nbx])	{
				int distance = calc(mSX, mSY, t.x, t.y);
				
				// 거리가 l 넘으면 안됨, l 크기 만큼의 버킷이기 때문에 제한 거리 보장 안됨
				if(distance > l)	continue;
				
				candidates.add(new dist(distance, t.id));
			}
		}
		
		// 근처에 l거리 이하의 택시가 없을 경우
		if(candidates.isEmpty())	return -1;
		
		Taxi target = taxiDB[candidates.first().id];
		
		int by = target.y / l;
		int bx = target.x / l;
		
		// 기존 버킷에서 제거
		bucket[by][bx].remove(target);
		
		// 기존 TreeSet에서 제거
		bestTaxi.remove(target);
		
		// 택시의 이동 거리 갱신
		int rideDistance = calc(mSX, mSY, mEX, mEY);
		int moveDistance = calc(target.x, target.y, mSX, mSY) + rideDistance;
		target.rideDist += rideDistance;
		target.moveDist += moveDistance;
		
		// 택시의 좌표를 목적지 좌표로 갱신
		target.y = mEY;	
		target.x = mEX;
		
		// 새로운 버킷에 추가
		by = target.y / l;
		bx = target.x / l;
		bucket[by][bx].add(target);
		
		// TreeSet에 갱신된 택시 추가
		bestTaxi.add(target);
		
		return target.id;
	}

	public Solution.Result reset(int mNo)
	{
		Solution.Result res = new Solution.Result();
		
		Taxi target = taxiDB[mNo];
		
		// TreeSet에서 운행 상태 초기화 전 제거
		bestTaxi.remove(target);
		
		// Result에 초기화 되기 전의 운행 상태 저장
		res.mY = target.y;
		res.mX = target.x;
		res.mMoveDistance = target.moveDist;
		res.mRideDistance = target.rideDist;
		
		// 택시 운행 상태 초기화
		target.moveDist = 0;
		target.rideDist = 0;
		
		// TreeSet에 초기화 된 택시 추가
		bestTaxi.add(target);
		
		return res;
	}

	public void getBest(int[] mNos)
	{
		int mNosCnt = Math.min(bestTaxi.size(), 5);
		int cnt = 0;
		
		Iterator<Taxi> iter = bestTaxi.iterator();
		while(iter.hasNext())	{
			if(cnt == mNosCnt)	break;
			mNos[cnt++] = iter.next().id;
		}

		return;	
	}
}

