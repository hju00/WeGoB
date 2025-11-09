import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class UserSolution {

    static int V;
    static int[] charge; // 각 도시에서의 단위 시간당 충전량
    static List<Road>[] graph; // 인접 도시로 가는 도로들
    static HashMap<Integer, Road> edges;

    /**
     *
     * @param to: 도착하는 인접도시
     * @param time: 도로를 이용하는데 걸리는 시간
     * @param power: 이동에 필요한 배터리 비용
     * @param removed: 도로가 삭제되었는지?
     */
    static class Road {
        int to;
        int time;
        int power;
        boolean removed;

        public Road(int to, int time, int power, boolean removed) {
            this.to = to;
            this.time = time;
            this.power = power;
            this.removed = removed;
        }
    }

    /**
     *
     * @param to: 도착하는 도시
     * @param time: 걸리는 시간
     */
    static class VirusState implements Comparable<VirusState>{
        int to;
        int time;

        public VirusState(int to, int time) {
            this.to = to;
            this.time = time;
        }

        @Override
        public int compareTo(VirusState o) {
            return Integer.compare(this.time, o.time);
        }
    }

    /**
     *
     * @param to: 도착하는 도시, 자기 자신이거나 인접 도시
     * @param time: 걸리는 시간
     * @param battery: 남은 배터리량
     */
    static class CarState implements Comparable<CarState>{
        int to;
        int time;
        int battery;

        public CarState(int to, int time, int battery) {
            this.to = to;
            this.time = time;
            this.battery = battery;
        }

        public int compareTo(CarState o) {
            return Integer.compare(this.time, o.time);
        }
    }


    /**
     *
     * @param N: 도시 개수
     * @param mCharge: 각 도시의 단위 시간당 충전량 (1 <= charge[i] <= 100)
     * @param K: 도로의 개수 ( 6 ≤ K ≤ 4,000 )
     * @param mId:
     * @param sCity
     * @param eCity
     * @param mTime: 도로 i의 소요 시간 ( 1 ≤ mTime[i] ≤ 100 )
     * @param mPower: 도로 i의 전력 소모량 ( 1 ≤ mPower[i] ≤ 100 )
     */
    public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
        V = N; // 도시 개수: 6 <= N <= 500
        charge = mCharge; // 도시 i의 단위 시간당 충전량: 1 <= charge[i] <= 100
        graph = new ArrayList[V];
        edges = new HashMap<>();

        // 그래프 초기화
        for(int i = 0; i < V; i++) {
            graph[i] = new ArrayList<>();
        }

        // Edge(도로) 세팅
        for(int i = 0; i < K; i++) {
            // 도로 저장
            Road road = new Road(eCity[i], mTime[i], mPower[i], false);
            graph[sCity[i]].add(road); // 각 도시에 연결된 도로
            edges.put(mId[i], road); // remove하기 위해 map에 edge 저장
        }

        return;
    }

    public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
        Road road = new Road(eCity, mTime, mPower, false);
        graph[sCity].add(road);
        edges.put(mId, road);

        return;
    }

    public void remove(int mId) {
        Road road= edges.get(mId);
        road.removed = true;

        return;
    }

    /**
     *
     * @param B: 최대 충전 용량, 10 <= B <= 300
     * @param sCity
     * @param eCity
     * @param M: 전염병 발생한 도시 개수, 1 <= M <= 5
     * @param mCity: 전염병이 발생한 도시
     * @param mTime: mCity[i]의 전염병 발생 시각, 0 <= mTime[i] <= 20
     * @return 최소 시간
     */
    public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {

        // Virus 다익 시작
        int[] virusDist = new int[V];
        Arrays.fill(virusDist, Integer.MAX_VALUE);
        PriorityQueue<VirusState> virusPQ = new PriorityQueue<>();

        // 전염병 시작점을 모두 PQ에 넣고 퍼뜨리기
        for(int i = 0; i < M; i++) {
            virusDist[mCity[i]] = mTime[i];
            virusPQ.add(new VirusState(mCity[i], mTime[i])); // 여기에서 to는 자기자신을 가리킴.
        }

        while(!virusPQ.isEmpty()) {
            VirusState cur = virusPQ.poll();

            // 이미 더 빠른 시간에 방문했으면 스킵
            if(virusDist[cur.to] < cur.time) continue;

            // 인접 도시에 퍼뜨리기
            for(Road road : graph[cur.to]) {
                int nextTime = cur.time + road.time;

                if(virusDist[road.to] > nextTime) {
                    virusDist[road.to] = nextTime;
                    virusPQ.add(new VirusState(road.to, nextTime));
                }
            }
        }

        // Car 다익 시작
        int[][] carDist = new int[V][B+1];
        for(int i = 0; i < V; i++) {
            Arrays.fill(carDist[i], Integer.MAX_VALUE);
        }
        carDist[sCity][B] = 0;

        PriorityQueue<CarState> carPQ = new PriorityQueue<>();
        carPQ.add(new CarState(sCity, 0, B));

        while(!carPQ.isEmpty()) {
            CarState cur = carPQ.poll();

            // 이미 더 좋은 조건으로 방문했다면 스킵
            if(carDist[cur.to][cur.battery] < cur.time) continue;

            if(cur.to == eCity) return cur.time;

            // 1. 이동해보기
            for(Road road : graph[cur.to]) {
                // 배터리 충분?
                if(cur.battery >= road.power) {
                    int nextTime = cur.time + road.time;

                    // 바이러스 이미 퍼졌다면 못감
                    if(virusDist[road.to] <= nextTime) continue;

                    int nextBattery = cur.battery - road.power; // 이동 후 배터리

                    if(carDist[road.to][nextBattery] > nextTime) {
                        // nextBattery보다 적은양의 배터리들도 최소 nextTime은 보장함.
                        for(int i = nextBattery; i >= 0; i--) {
                            if(carDist[road.to][i] < nextTime) break; // 더 적은시간이면 end
                            carDist[road.to][i] = nextTime;
                        }

                        carPQ.add(new CarState(road.to, nextTime, nextBattery));
                    }
                }
            }

            // 2. 충전해보기
            if(cur.battery < B) {
                int nextTime = cur.time + 1;

                // 충전하는 동안 바이러스 오면 X
                if(virusDist[cur.to] > nextTime) {
                    int nextBattery = Math.min(cur.battery + charge[cur.to], B); // B 넘어서 충전 불가능

                    if(carDist[cur.to][nextBattery] > nextTime) {
                        //
                        for(int i = nextBattery; i >= 0; i--) {
                            if(carDist[cur.to][i] < nextTime) break;
                            carDist[cur.to][i] = nextTime;
                        }

                        carPQ.add(new CarState(cur.to, nextTime, nextBattery));
                    }
                }
            }
        }

        return -1;
    }
}