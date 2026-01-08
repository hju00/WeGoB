import java.util.*;

class UserSolution {
    class Train {
        int mId;
        int start, end, interval;
        boolean isAlive;

        Train(int mId, int start, int end, int interval){
            this.mId = mId;
            this.start = start;
            this.end = end;
            this.interval = interval;

            isAlive = true;
        }

        boolean willStop(int stationId){ // 특정 역에 멈출 지 확인
            if(!isAlive) return false;
            if (stationId < start || stationId > end) return false;
            return (stationId - start) % interval == 0; // 멈추면 간격으로 나눴을 때 나머지가 0
        }
    }

    int N, trainCnt;
    Train[] trains;
    Map<Integer, Integer> id2idx; // mId 넣으면 idx 알려주는 map
    boolean[][] connected; // 열차 간 환승 가능 여부
    
    public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
        this.N = N;
        trainCnt = 0; // 지금 열차 개수

        trains = new Train[201]; // 열차 최대 50개 + 150번 호출
        connected = new boolean[201][201];
        id2idx = new HashMap<>();

        for(int i = 0; i < K; i++){
            trains[trainCnt] = new Train(mId[i], sId[i], eId[i], mInterval[i]);
            id2idx.put(mId[i], trainCnt);
            trainCnt++;
        }

        for (int i = 0; i < trainCnt; i++) {
            for (int j = i + 1; j < trainCnt; j++) {
                if (canTransfer(trains[i], trains[j])) {
                    connected[i][j] = connected[j][i] = true;
                }
            }
        }

        return;
    }

    private boolean canTransfer(Train t1, Train t2) { // 같은 역에 멈추는지 확인
        if(!t1.isAlive || !t2.isAlive) return false;

        int s1 = t1.start, e1 = t1.end, d1 = t1.interval;
        int s2 = t2.start, e2 = t2.end, d2 = t2.interval;

        int start = Math.max(s1, s2); // 둘 모두 시작한 최소 시간
        int end = Math.min(e1, e2); // 둘 모두 운행하는 최대 시간
        if ( start > end) return false; // 겹치는 구간 없으면 false

        if ((s2 - s1) % gcd(d1, d2) != 0) return false; // 0이 아니면 안 만남

        // 더 자주 도착하는 기차를 small
        Train small = t1;
        Train big = t2;
        if (t1.interval > t2.interval) {
            small = t2;
            big = t1;
        }
        
        // 자주 도착하는 기차(small)이 L, R 사이에서 정차하는 최초의 역
        int first = small.start;
        int ds = small.interval;

        while (first < start) { // 열차 시작 시간보다 둘 모두 시작한 최소 시간이 빠르면
            first += ds; // 다음 열차로 보내기
        }
        
        if (first > end) return false; // 공통 구간에 small 안섬

        int lcm = lcm(d1, d2);
        int lim = Math.min(end, first + lcm); // 반복 돌 마지막 구간

        for (int x = first; x <= lim; x += ds) { // x: small의 정차 시각
            if (x < big.start || x > big.end) continue; // 정차 시각이 범위 밖에 있으면 out
            // x = big.start + big.interval * m -> x - big.start = big.interval * m
            if ((x - big.start) % big.interval == 0) return true; // 동시에 멈추면 return true
        }
        return false;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    private int lcm(int a, int b) {
        return a / gcd(a, b) * b;
    }

    public void add(int mId, int sId, int eId, int mInterval) {
        Train t = new Train(mId, sId, eId, mInterval);
        int idx = trainCnt;
        trains[idx] = t;
        id2idx.put(mId, idx);
        trainCnt++;
        
        // 연결 업데이트
        for(int i = 0; i < idx; i++){
            if(!trains[i].isAlive) continue;
            if(canTransfer(trains[i], t)) {
                connected[i][idx] = connected[idx][i] = true;
            }
        }
    }

    public void remove(int mId) {
        int idx = id2idx.get(mId);
        trains[idx].isAlive = false; // 없애기
    }

    public int calculate(int sId, int eId) {
        // sId: 출발역, eId: 도착역
        HashSet<Integer> starts = new HashSet<>();
        HashSet<Integer> arrives = new HashSet<>();
        boolean[] visited = new boolean[trainCnt];

        for(int i = 0; i < trainCnt; i++){
            Train t = trains[i];
            boolean willStart = false, willArrive = false;

            if(!t.isAlive) continue;

            if(t.willStop(sId)) {
                starts.add(i);
                willStart = true;
            }
            if(t.willStop(eId)) {
                arrives.add(i);
                willArrive = true;
            }

            if (willStart && willArrive) return 0; // 한 열차로 한번에 쭉 갈 수 있음
        }

        Queue<int[]> q = new ArrayDeque<>();
        for(int sIdx : starts ){
            visited[sIdx] = true;
            q.offer(new int[] {sIdx, 0}); // 처음 환승 횟수 : 0
        }
        
        while(!q.isEmpty()){
            int[] cur = q.poll();
            int trainIdx = cur[0];
            int cnt = cur[1];
            
            if(!trains[trainIdx].isAlive) continue; // 삭제한 열차면 넘어가기
            if(arrives.contains(trainIdx)) return cnt;

            for(int i = 0; i < trainCnt; i++){
                if(!connected[i][trainIdx]) continue;
                if(!trains[i].isAlive) continue;
                if(visited[i]) continue;

                visited[i] = true;
                q.offer(new int[] {i, cnt + 1});
            }
            
        }
        
        return -1;
    }
}