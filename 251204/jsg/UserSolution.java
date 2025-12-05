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
        boolean isDeleted;

        public Train(int mId, int start, int end, int interval) {
            this.mId = mId;
            this.start = start;
            this.end = end;
            this.interval = interval;
        }
    }

    static final int MAX_TRAINS = 200;

    static int N, K;
    Train[] trains;
    List<Integer>[] adjList;
    int trainCnt;
    static Map<Integer, Integer> trainMap;

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
        trains = new Train[MAX_TRAINS];
        adjList = new ArrayList[MAX_TRAINS];
        trainMap = new HashMap<>();
        trainCnt = 0;

        for(int i = 0; i < MAX_TRAINS; i++) {
            adjList[i] = new ArrayList<>();
        }

        // 열차 리스트 초기화
        for (int i = 0; i < K; i++) {
            add(mId[i], sId[i], eId[i], mInterval[i]);
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
        int idx = trainCnt++;

        Train train = new Train(mId, sId, eId, mInterval);
        trains[idx] = train;
        trainMap.put(train.mId, idx);

        for(int i = 0; i < idx; i++) {
            // 이미 삭제됐으면 무시
            if(trains[i].isDeleted) continue;

            if(isConnected(train, trains[i])) {
                adjList[idx].add(i);
                adjList[i].add(idx);
            }
        }

        return;
    }

    /**
     *
     * @param mId: 삭제할 열차 mId
     */
    public void remove(int mId) {
        if(trainMap.containsKey(mId)) {
            int idx = trainMap.get(mId);
            trains[idx].isDeleted = true;
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
        boolean visited[] = new boolean[trainCnt];

        // 출발역 포함하는 열차들 넣기
        Queue<int[]> trainQ = new ArrayDeque<>();
        for(int i = 0; i < trainCnt; i++) {
            // 삭제됐으면 스킵
            if(trains[i].isDeleted) continue;

            // 해당 정거장 지나는지 check
            Train train = trains[i];
            int start = train.start;
            int end = train.end;
            int interval = train.interval;

            if(sId >= start && sId <= end && (sId - start) % interval == 0) {
                visited[i] = true;
                trainQ.add(new int[] {i, 0});
            }
        }

        while(!trainQ.isEmpty()) {
            int[] cur = trainQ.poll();
            int idx = cur[0];
            int cnt = cur[1];

            // 먼저 현재 열차로 종착역 갈 수 있는지 판별 && 갈 수 있다면 cnt return
            Train train = trains[idx];
            if(eId >= train.start && eId <= train.end && (eId - train.start) % train.interval == 0) {
                return cnt;
            }

            // 나머지 중에서 환승 가능하면 환승하기
            for(int i : adjList[idx]) {
                // 이미 삭제됐거나, visited면 스킵
                if(trains[i].isDeleted || visited[i]) continue;

                visited[i] = true;
                trainQ.add(new int[] {i, cnt + 1});
            }
        }

        return -1;
    }

    private boolean isConnected(Train train, Train train2) {
        // 범위 안겹치면 false
        if(train.start > train2.end || train2.start > train.end) {
            return false;
        }

        for(int i = train.start; i <= train.end; i += train.interval) {
            // 더 작으면 더 키울 수 있음. 더 크면 안겹침.
            if(i < train2.start) continue;
            if(i > train2.end) break;

            if((i - train2.start) % train2.interval == 0) {
                return true;
            }
        }

        return false;
    }
}