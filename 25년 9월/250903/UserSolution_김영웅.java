import java.util.*;

class UserSolution_김영웅 {
	static Map<Integer, Taxi> taxies;
	static Set<Taxi> taxiSet;
	static int N, M, L;
	
	public void init(int N, int M, int L, int[] mXs, int[] mYs) {
		taxiSet = new TreeSet<>();
		taxies = new HashMap<>();
		
		UserSolution.N = N;
		UserSolution.M = M;
		UserSolution.L = L;
		
		for (int i = 0; i < M; i++) {
			Taxi taxi = new Taxi(i + 1, mXs[i], mYs[i], 0, 0);
			taxies.put(i + 1, taxi);
			taxiSet.add(taxi); // 처음부터 getbest 부를 수도 있으니까 ~ 
		}
		
		return;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY) {
	    Taxi bestTaxi = null;
	    int minDistance = Integer.MAX_VALUE;

	    for (int i = 1; i <= M; i++) {
	        Taxi currentTaxi = taxies.get(i);
	        int distToStart = currentTaxi.getTaxiDistance(mSX, mSY);

	        if (distToStart > L) continue;

	        // 더 가까운 택시를 찾은 경우
	        if (distToStart < minDistance) {
	            minDistance = distToStart;
	            bestTaxi = currentTaxi;
	        } 
	        // 거리가 같은 경우, ID가 더 작은 택시를 선택합니다.
	        else if (distToStart == minDistance) {
	            if (bestTaxi == null || currentTaxi.id < bestTaxi.id) {
	                bestTaxi = currentTaxi;
	            }
	        }
	    }

	    if (bestTaxi == null) return -1;

	    taxiSet.remove(bestTaxi);
	    bestTaxi.move(mSX, mSY, mEX, mEY);
	    taxiSet.add(bestTaxi);

	    return bestTaxi.id;
	}

	public Solution.Result reset(int mNo) {
		Taxi taxi = taxies.get(mNo);
		
		taxiSet.remove(taxi);
		Solution.Result result = taxi.reset();
		taxiSet.add(taxi);
		
		return result;
	}

	public void getBest(int[] mNos) {
		int i = 0;
		Iterator<Taxi> iterator = taxiSet.iterator();
		
		while (i < 5 && iterator.hasNext()) mNos[i++] = iterator.next().id;
		
		return;
	}
	
	static class Taxi implements Comparable<Taxi>{
		int id, x, y, moveDistance, rideDistance; // x, y, 총 이동 거리, 운행 거리
		
		public Taxi(int id, int x, int y, int totalDistance, int moveDistance) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.moveDistance = totalDistance;
			this.rideDistance = moveDistance;
		}

		void move(int sx, int sy, int dx, int dy) {
			int fromSourceToDest = getDistance(sx, sy, dx, dy);
			this.moveDistance += getDistance(this.x, this.y, sx, sy) + fromSourceToDest;
			this.rideDistance += fromSourceToDest;
			
			this.x = dx;
			this.y = dy;
		}
		
		int getTaxiDistance(int x, int y) {
			return getDistance(this.x, this.y, x, y);
		}
		
		Solution.Result reset() {
			Solution.Result result = new Solution.Result();
			
			result.mX = this.x;
			result.mY = this.y;
			result.mMoveDistance = this.moveDistance;
			result.mRideDistance = this.rideDistance;
			
			this.rideDistance = this.moveDistance = 0;
			
			return result;
		}

		@Override
		public int compareTo(Taxi o) {
			int res = o.rideDistance - this.rideDistance;
			
			return res != 0 ? res : this.id - o.id;
		}
	}
	
	static int getDistance(int x, int y, int r, int c) {
		return Math.abs(x - r) + Math.abs(y - c);
	}
}