import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

class UserSolution {

    static class Train {
        int mId;
        int start;
        int end; // 노선이 갈 수 없는 역은 제어하는데 필요
        int interval;

        public Train(int mId, int start, int end, int interval) {
            this.mId = mId;
            this.start = start;
            this.end = end;
            this.interval = interval;
        }
    }

    static int N, K;
    static List<Train> trains;
    static Map<Integer, Train> trainMap;

    /**
     *
     * @param N:         열차 역의 개수 ( 20 ≤ N ≤ 100,000 ) => 최대 33,000개
     * @param K:         열차의 개수 ( 3 ≤ K ≤ 50 )
     * @param mId:       열차 ID ( 1 ≤ mId[i] ≤ 1,000,000,000 )
     * @param sId
     * @param eId
     * @param mInterval: 정차 역의 간격 ( 3 ≤ mInterval[i] ≤ 50 )
     */
    public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
        this.N = N;
        this.K = K;
        trains = new ArrayList<Train>();
        trainMap = new HashMap<>();

        // 열차 리스트 초기화
        for (int i = 0; i < K; i++) {
            Train train = new Train(mId[i], sId[i], eId[i], mInterval[i]);
            trainMap.put(mId[i], train);
            trains.add(train);
        }

        return;
    }

    /**
     *
     * @param mId:       열차 ID
     * @param sId:       열차의 시작역
     * @param eId:       열차의 종착역
     * @param mInterval: 정차 간격
     */
    public void add(int mId, int sId, int eId, int mInterval) {
        Train train = new Train(mId, sId, eId, mInterval);
        trains.add(train);
        trainMap.put(train.mId, train);

        return;
    }

    /**
     *
     * @param mId: 삭제할 열차 mId
     */
    public void remove(int mId) {
        for (int i = 0; i < trains.size(); i++) {
            if(trains.get(i).mId == mId) {
                trains.remove(i);
            }
        }

        return;
    }

    /**
     *
     * @param sId: 출발 역
     * @param eId: 도착 역
     * @return 최소 환승 횟수
     */
    public int calculate(int sId, int eId) {
        boolean visited[] = new boolean[trains.size()];

        // 출발역 포함하는 열차들 리스트
        Queue<int[]> trainQ = new ArrayDeque<>();
        for(int i = 0; i < trains.size(); i++) {
            // 해당 정거장 지나는지 check
            Train train = trains.get(i);
            int start = train.start;
            int interval = train.interval;

            if(sId >= start && (sId - start) % interval == 0) {
                visited[i] = true;
                trainQ.add(new int[] {train.mId, 0});
            }
        }

        while(!trainQ.isEmpty()) {
            int[] cur = trainQ.poll();
            Train train = trainMap.get(cur[0]);
            int cnt = cur[1];

            // 먼저 현재 열차로 종착역 갈 수 있는지 판별 && 갈 수 있다면 cnt return
            if((eId - train.start) % train.interval == 0) {
                return cnt;
            }

            // 나머지 중에서 환승 가능하면 환승하기
            for(int i = 0; i < trains.size(); i++) {

            }
        }



        return -1;
    }













}