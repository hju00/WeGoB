package week09_02.sjh1108.SWEA_24997;

import java.util.*;

class UserSolution
{
    int N, M, L;

    static class Taxi implements Comparable<Taxi>{
        int id;
        int x, y;
        int mMoveDistance;
        int mRideDistance;

        public Taxi(int id, int x, int y, int mMoveDistance, int mRideDistance) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.mMoveDistance = mMoveDistance;
            this.mRideDistance = mRideDistance;
        }

        @Override
        public int compareTo(Taxi o) {
            if(o.mRideDistance == this.mRideDistance){
                return Integer.compare(this.id, o.id);
            }
            return Integer.compare(o.mRideDistance, this.mRideDistance);
        }
    }

    HashMap<Integer, Taxi> taxiMap;
    List<Taxi>[][] bucket;
    TreeSet<Taxi> heap;

    private int getDistance(int sX, int sY, int eX, int eY){
        return Math.abs(sX - eX) + Math.abs(sY - eY);
    }

    @SuppressWarnings("unchecked")
	public void init(int N, int M, int L, int[] mXs, int[] mYs)
	{
        this.N = N;
        this.M = M;
        this.L = L;
        
        bucket = new List[10][10];

        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                bucket[i][j] = new ArrayList<>();
            }
        }

        taxiMap = new HashMap<>();
        heap = new TreeSet<>();

        for(int i = 0; i < M; i++){
            int x = mXs[i], y = mYs[i];
            Taxi tmp = new Taxi(i+1, x, y, 0, 0);

            bucket[x / L][y / L].add(tmp);

            heap.add(tmp);
            taxiMap.put(i+1, tmp);
        }

		return;
	}

	public int pickup(int mSX, int mSY, int mEX, int mEY) {
        int x = mSX / L;
        int y = mSY / L;

        int min = Integer.MAX_VALUE;
        
        Taxi called = null;
        for(int i = Math.max(0, x-1); i <= Math.min(9, x+1); i++){
            for(int j = Math.max(0, y-1); j <= Math.min(9, y+1); j++){
                for(Taxi t: bucket[i][j]){
                    int dist = getDistance(mSX, mSY, t.x, t.y);

                    if(dist > L){
                        continue;
                    }

                    if(dist < min){
                        called = t;
                        min = dist;
                    }
                    else if(dist == min && (called == null || called.id > t.id)){
                        called = t;
                    }
                }
            }
        }
        
        if(called == null){
            return -1;
        }

        heap.remove(called);

        int oldTaxiBucketX = called.x / L;
        int oldTaxiBucketY = called.y / L;
        bucket[oldTaxiBucketX][oldTaxiBucketY].remove(called);

        int rideDist = getDistance(mSX, mSY, mEX, mEY);
        called.mMoveDistance += min + rideDist;
        called.mRideDistance += rideDist;
        called.x = mEX;
        called.y = mEY;

        int newTaxiBucketX = called.x / L;
        int newTaxiBucketY = called.y / L;
        bucket[newTaxiBucketX][newTaxiBucketY].add(called);
        heap.add(called);

        return called.id;
    }

	public Solution_24997_송주헌.Result reset(int mNo)
	{
		Solution_24997_송주헌.Result res = new Solution_24997_송주헌.Result();

        Taxi t = taxiMap.get(mNo);

        heap.remove(t);

        res.mX = t.x;
        res.mY = t.y;
        res.mMoveDistance = t.mMoveDistance;
        res.mRideDistance = t.mRideDistance;

        t.mMoveDistance = 0;
        t.mRideDistance = 0;

        heap.add(t);

		return res;
	}

	public void getBest(int[] mNos)
	{
        Taxi output = heap.first();
        mNos[0] = output.id;
        for(int i = 1; i < 5; i++){
            output = heap.higher(output);
            mNos[i] = output.id;
        }
		return;
	}
}