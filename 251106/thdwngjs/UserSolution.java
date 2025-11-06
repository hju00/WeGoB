import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

class UserSolution {
    static class Road implements Comparable<Road>{
        int sCity, eCity, mTime, mPower;
        public Road(int sCity, int eCity, int mTime, int mPower){
            this.sCity = sCity;
            this.eCity = eCity;
            this.mTime = mTime;
            this.mPower = mPower;
        }

        @Override
        public int compareTo(Road o) {
            if(this.mTime == o.mTime){
                return o.mPower - this.mPower;
            }
            return this.mTime - o.mTime;
        }
    }

    ArrayList<HashSet<Integer>> roadsFromCities;
    HashMap<Integer, Road> roadMap;
    int[] mCharge;
    int N;

    public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
        this.N = N;
        this.mCharge = new int[N];
        System.arraycopy(mCharge, 0, this.mCharge, 0, N);

        roadsFromCities = new ArrayList<>();
        for(int i = 0; i < N; i++){
            roadsFromCities.add(new HashSet<>());
        }
        roadMap = new HashMap<>();

        for(int i = 0; i < K; i++){
            add(mId[i], sCity[i], eCity[i], mTime[i], mPower[i]);
        }
        return;
    }

    public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
        Road road = new Road(sCity, eCity, mTime, mPower);
        roadMap.put(mId, road);
        roadsFromCities.get(sCity).add(mId);
        return;
    }

    public void remove(int mId) {
        Road road = roadMap.get(mId);
        roadsFromCities.get(road.sCity).remove(mId);
        roadMap.remove(mId);
        return;
    }

    public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
        int[] virus = new int[N];
        Arrays.fill(virus, Integer.MAX_VALUE);

        Queue<int[]> virusQueue = new PriorityQueue<>(Comparator.comparingInt(o -> o[1]));

        for(int i = 0; i < M; i++){
            virusQueue.add(new int[]{mCity[i], mTime[i]});
            virus[mCity[i]] = mTime[i];
        }

        while (!virusQueue.isEmpty()) {
            int[] curr = virusQueue.poll();
            int u = curr[0];
            int time = curr[1];

            if (virus[u] < time) continue;

            for (int mId : roadsFromCities.get(u)) {
                Road road = roadMap.get(mId);
                int v = road.eCity;
                int nextTime = time + road.mTime;

                if (virus[v] > nextTime) {
                    virus[v] = nextTime;
                    virusQueue.offer(new int[]{v, nextTime});
                }
            }
        }

        int[][] dist = new int[N][B + 1];
        for (int[] row : dist) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        Queue<int[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));

        dist[sCity][B] = 0;
        pq.offer(new int[]{sCity, 0, B});

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int to = cur[0];
            int time = cur[1];
            int battery = cur[2];

            if (dist[to][battery] < time) continue;
            if (to == eCity) return time;

            for (int mId : roadsFromCities.get(to)) {
                Road road = roadMap.get(mId);
                
                if (battery >= road.mPower) {
                    int nextTo = road.eCity;
                    int nextTime = time + road.mTime;

                    if (virus[nextTo] <= nextTime) continue;

                    int nextBattery = battery - road.mPower;
                    if (dist[nextTo][nextBattery] > nextTime) {
                        for (int i = nextBattery; i >= 0; --i) {
                            if (dist[nextTo][i] < nextTime) break;
                            dist[nextTo][i] = nextTime;
                        }
                        pq.offer(new int[]{nextTo, nextTime, nextBattery});
                    }
                }
            }

            /*
             * 이 부분을 위에 for문 안에 넣으려고 고민하다가 오래걸림
             * 넣으면 안되는걸 넣고자 하니 안되는거였음
             */
            if (battery < B) {
                int nextTime = time + 1;

                if (virus[to] > nextTime) {
                    int nextBattery = battery + mCharge[to];
                    if (nextBattery > B) nextBattery = B;

                    if (dist[to][nextBattery] > nextTime) {
                        for (int i = nextBattery; i >= 0; --i) {
                            if (dist[to][i] < nextTime) break;
                            dist[to][i] = nextTime;
                        }
                        pq.offer(new int[]{to, nextTime, nextBattery});
                    }
                }
            }
        }
        
        return -1;
    }
}