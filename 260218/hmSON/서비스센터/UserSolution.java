import java.util.*;

class UserSolution {

    /*
    * 수리 요청을 처리하는 로봇 클래스. 로봇 대기열 정렬 기준 적용
    * 로봇의 ID, 단위 시간당 업무 처리량, 할당받은 수리 요청의 ID, 로봇 제거 여부(지연 삭제)
    * 정렬 기준
    * 1. 단위 시간당 업무 처리량 내림차순
    * 2. 로봇 ID 오름차순
    * */
    static class Robot implements Comparable<Robot> {
        int id, throughput, allocated = -1;
        boolean removed = false;

        public Robot(int id, int throughput) {
            this.id = id;
            this.throughput = throughput;
        }
        @Override
        public int compareTo(Robot o) {
            if(this.throughput == o.throughput) return this.id - o.id;
            return o.throughput - this.throughput;
        }
    }

    /*
    * 접수된 수리 요청 리스트
    * 수리 요청의 ID, 총 업무량, 요청 접수 시각, 등급, 업무 처리 완료 시각, 할당된 로봇 ID
    * 클래스 내에서 정렬 기준을 재정의하지 않음 -> 할당 대기열과 진행현황 우선순위 큐의 정렬 기준이 다르기 때문.
    * */
    static class Request {
        int id, workload, received, grade, end = -1, allocated = -1;

        public Request(int id, int workload, int received, int grade) {
            this.id = id;
            this.workload = workload;
            this.received = received;
            this.grade = grade;
        }
    }

    int refTime, gradeCnt; // 지연 기준 시간, 등급 수
    int[] waitingTimes; // 등급별 총 대기시간
    Robot[] robots = new Robot[1_001]; // 로봇 객체 배열. 인덱스는 각 로봇의 ID
    Request[] requests = new Request[50_001]; // 수리 요청 객체 배열. 인덱스는 각 수리 요청의 ID

    // 등급별 수리 요청 대기열과 진행중인 수리 요청 리스트
    // 대기열의 경우 최고등급 + 1 인덱스에 지연 요청 대기열 추가. 내부적으로는 접수 시간 기준 오름차순 정렬.
    // 진행 중인 수리 요청 리스트는 종료 시간 기준 오름차순 정렬.
    PriorityQueue<Request>[] requestQueue;
    PriorityQueue<Request> inProgress = new PriorityQueue<>(Comparator.comparingInt(o -> o.end));

    // 로봇 대기열
    // (1) 처리량 기준 내림차순, (2) ID 기준 오름차순
    PriorityQueue<Robot> robotQueue = new PriorityQueue<>();

    /**
     * 초기화 메서드. 초기 1회 호출
     * 등급 수에 따라 등급별 수리 요청 대기 큐와 등급별 대기시간 배열 생성
     * 진행중인 수리 요청 현황, 로봇 대기열, 수리 요청 및 로봇 객체 배열 초기화
     * @param N 지연 기준 시간(10 <= N <= 100)
     * @param M 수리 요청의 등급 수(3 <= M <= 10)
     */
    void init(int N, int M) {
        refTime = N;
        gradeCnt = M;
        waitingTimes = new int[M];

        // "지연 상태" 수리 요청의 대기열을 최우선으로 처리하기 위해 최고 등급인 gradeCnt+1을 부여
        requestQueue = new PriorityQueue[M+1];
        for(int i=0; i<=M; i++) {
            requestQueue[i] = new PriorityQueue<>(Comparator.comparingInt(o -> o.received));
        }

        // 재활용 가능한 객체는 가급적 객체를 새로 만들지 않는다.
        inProgress.clear();
        robotQueue.clear();
        Arrays.fill(requests, null);
        Arrays.fill(robots, null);
    }

    /**
     * (핵심) 수리 진행 메서드.
     * init()을 제외한 모든 메서드가 호출되는 즉시 호출되므로 최대 61_500회 호출.
     * 쿼리 호출시 업무 처리 현황을 최산화하는 지연 갱신 메서드임.
     * 서비스 센터의 수리 과정은 다음 과정을 거친다.
     * 1. 접수된 수리 요청 중 지연 기준 시간 이상 경과한 수리 요청을 "지연 상태"로 전환
     * 2. 업무 처리 진행중인 수리 요청 중 "현 시점에 완료되는 수리 요청들"을 처리 완료 상태로 전환
     * 3. 할당된 수리 요청이 없는 로봇 중 우선순위가 높은 로봇부터 처리 우선순위가 높은 수리 요청을 재할당함.
     * ★★★ 진행중인 수리 요청 각각의 종료 시간이 다름에 유의해야 한다. ★★★
     * @param time 현재 시각
     */
    void repair(int time) {
        // 현재 가장 빠른 업무 처리 완료 시각을 저장할 변수
        int finishedTime = -1;
        while(finishedTime < time) {
            // 0. 현재 가장 빠른 업무 처리 완료 시각 확인
            // 수리 요청에 대한 처리 완료 시점마다 지연 업데이트 과정을 거쳐야 함
            // 처리가 완료되는 수리 요청이 없더라도 현재 시각을 기준으로 한 번 지연 갱신을 적용해야 함
            if(inProgress.isEmpty() || inProgress.peek().end > time) finishedTime = time;
            else finishedTime = inProgress.peek().end;

            // 1. "지연 상태" 전환
            // 각 등급별로 지연시간 기준을 경과한 요청을 "지연 상태"로 전환
            // 더 높은 우선순위를 부여하기 위해 "지연 상태"를 gradeCnt+1 등급으로 처리
            for(int i=gradeCnt-1; i>=0; i--) {
                while(!requestQueue[i].isEmpty()) {
                    int received = requestQueue[i].peek().received;
                    if(finishedTime < received + refTime) break;

                    requestQueue[gradeCnt].add(requestQueue[i].poll());
                }
            }

            // 2. 업무 처리 완료된 수리 요청 분류
            // 현재 업무 처리 진행중인 수리 요청(inProgress) 중,
            // 처리 완료 시각이 finishedTime인 모든 수리 요청을 수리 완료 상태로 전환
            // 완료된 수리 요청의 등급에 대한 총 대기시간 갱신
            // 해당 수리 요청을 처리했던 로봇의 할당을 해제하고 대기열에 추가
            boolean flag = false;
            while(!inProgress.isEmpty() && inProgress.peek().end == finishedTime) {
                Request finished = inProgress.poll();
                if(finished.allocated == -1) continue;
                waitingTimes[finished.grade] += finished.end - finished.received;

                Robot robot = robots[finished.allocated];
                robot.allocated = -1;
                robotQueue.add(robot);
                flag = true;
            }
            if(!flag) continue;

            // 3. 대기 중인 로봇을 새 수리 요청에 할당
            while(!robotQueue.isEmpty()) {
                // 삭제된 로봇에 대한 지연 삭제 적용
                if(robotQueue.peek().removed) {
                    robotQueue.poll();
                    continue;
                }

                // 수리 요청 할당 여부를 확인하는 플래그
                // 촤고 등급인 gradeCnt+1, 즉 "지연 상태"인 대기열부터 확인
                flag = false;
                for(int i=gradeCnt; i>=0; i--) {
                    if(requestQueue[i].isEmpty()) continue;
                    Request target = requestQueue[i].poll();
                    Robot robot = robotQueue.poll();
                    allocate(target, robot, finishedTime);
                    // 다음 로봇도 업무를 할당하기 위해 플래그 변경
                    flag = true;
                    break;
                }

                // 대기열에 남아있는 수리 요청이 없음
                if(!flag) break;
            }
        }
    }

    /**
     * 수리 요청 할당 메서드
     * 수리 요청의 총 업무량과 로봇의 단위 사간당 업무 처리량을 이용하여 업무 완료 시각을 미리 계산
     * @param request 처리 우선순위가 제일 높은 수리 요청
     * @param robot 할당 우선순위가 제일 높은 로봇
     * @param time 할당 시각(현재 시각이 아닐 수 있음)
     */
    void allocate(Request request, Robot robot, int time) {
        robot.allocated = request.id;
        request.allocated = robot.id;
        request.end = time
                + (request.workload / robot.throughput)
                + (request.workload % robot.throughput == 0 ? 0 : 1);
        inProgress.add(request);
    }

    /**
     * 수리 요청 접수 메서드. 최대 호출 50_000회
     * 수리 요청을 등록한다. 수리 요청의 ID는 1부터 시작하여 메서드 호출시마다 1씩 증가한다.
     * 할당 대기 중인 로봇이 없으면 등급별 대기열에, 있으면 우선순위가 제일 높은 로봇에게 업무를 할당한다.
     * @param mTime 현재 시각(1 <= mTime <= 1_000_000)
     * @param mId 수리 요청의 ID(1 <= mId <+ 50_000)
     * @param mWorkload 수리 요청의 총 업무량(10 <= mWorkload <= 1_000)
     * @param mGrade 수리 요청의 등급(1 <= mGrade <= M)
     */
    void receive(int mTime, int mId, int mWorkload, int mGrade) {
        // 현재 시각까지의 수리 현황 갱신
        repair(mTime);

        // 신규 수리 요청 객체 생성 및 등록
        Request newRequest = new Request(mId, mWorkload, mTime, mGrade-1);
        requests[mId] = newRequest;

        // 제거된 로봇에 대한 지연 삭제 처리
        while(!robotQueue.isEmpty() && robotQueue.peek().removed) robotQueue.poll();
        // 대기 중인 로봇이 없으면 등급병 대기열에 추가
        // 대기 중인 로봇이 있으면 최고 우선순위 로봇에게 요청 할당
        if(robotQueue.isEmpty()) requestQueue[mGrade-1].add(newRequest);
        else {
            Robot robot = robotQueue.poll();
            allocate(newRequest, robot, mTime);
        }
    }

    /**
     * 로봇 추가 메서드. 최대 1_000회 호출
     * 새 로봇을 등록한다. 로봇의 ID는 1부터 시작하여 메서드 호출시마다 1씩 증가한다.
     * 대기 중인 수리 요청이 없으면 로봇 대기열에 해당 로봇을 추가한다.
     * 대기 중인 수리 요청이 있으면 우선순위가 제일 높은 수리 요청을 해당 로봇에게 할당한다.
     * @param mTime 현재 시각
     * @param rId 로봇 ID(1 <= rId <= 1_000)
     * @param mThroughput 단위 시간당 업무 처리량(5 <= mThroughput <= 20)
     */
    void add(int mTime, int rId, int mThroughput) {
        // 현재 시각까지의 수리 현황 갱신
        repair(mTime);

        // 신규 로봇 객체 생성 및 등록
        Robot robot = new Robot(rId, mThroughput);
        robots[rId] = robot;
        // 할당 가능한 수리 요청 찾기. 높은 등급("지연 상태" 포함)부터 대기 중인 수리 요청을 탐색한다.
        for(int i=gradeCnt; i>=0; i--) {
            if(requestQueue[i].isEmpty()) continue;

            // 최고 우선순위 수리 요청을 즉시 할당
            Request target = requestQueue[i].poll();
            allocate(target, robot, mTime);
            return;
        }

        // 대기 중인 수리 요청 없음. 로봇 대기열에 추가
        robotQueue.add(robot);
    }

    /**
     * 로봇 제거 메서드. 최대 500회 호출
     * 지정된 로봇을 제거한다. rId는 현재 고용중인 로봇임이 보장된다.
     * 제거된 로봇이 진행중이던 수리 요청이 있다면 진행 현황 초기화 및 할당 해제한다.
     * 대기 중인 다른 로봇이 존재하면 즉시 할당 해제된 수리 요청을 우선순위가 제일 높은 로봇에 할당한다.
     * @param mTime 현재 시각
     * @param rId 로봇 ID
     * @return 제거된 로봇이 진행중이었던 수리 요청의 ID 반환. 없으면 -1 반환.
     */
    int remove(int mTime, int rId) {
        // 현재 시각까지의 수리 현황 갱신
        repair(mTime);

        // 지정된 로봇을 제거 상태로 전환
        // 할당된 수리 요청이 없는 상태라면 -1 반환
        Robot target = robots[rId];
        target.removed = true;
        if(target.allocated == -1) return -1;

        // 진행중이던 업무의 할당 해제
        // deallocated.end는 정렬의 기준이므로 당장 건들지 않을 것임.
        Request deallocated = requests[target.allocated];
        deallocated.allocated = -1;

        // deallocated 객체는 아직 inProgress 큐에 존재하는 상태이므로 새 객체 생성 및 등록
        Request newRequest = new Request(
                deallocated.id,
                deallocated.workload,
                deallocated.received,
                deallocated.grade
        );
        requests[deallocated.id] = newRequest;

        // 할당 해제된 신규 요청을 받을 수 있는 로봇이 있다면 즉시 할당
        // 없다면 수리 요청 대기 큐에 등록. 지연 상태인지 판단해야 함.
        while(!robotQueue.isEmpty() && robotQueue.peek().removed) robotQueue.poll();
        if(robotQueue.isEmpty()) {
            // 지연 상태인지 판단
            int grade = newRequest.received + refTime <= mTime ? gradeCnt : newRequest.grade;
            requestQueue[grade].add(newRequest);
        } else {
            Robot newRobot = robotQueue.poll();
            allocate(newRequest, newRobot, mTime);
        }

        // 제거된 로봇이 진행중이던 수리 요청의 ID 반환
        return newRequest.id;
    }

    /**
     * 완료된 요청에 대한 평가 메서드. 최대 10_000회 호출
     * 완료된 수리 요청들 중 주어진 등급에 해당하는 모든 수리 요청의 대기 시간(접수 시각 ~ 처리 완료 시각) 합계 반환
     * @param mTime 현재 시각
     * @param mGrade 평가하려는 등급
     * @return 지정 등급에 속하는 모든 완료된 수리 요청의 대기 시간 합
     */
    int evaluate(int mTime, int mGrade) {
        // 현재 시각까지의 수리 현황 갱신
        repair(mTime);

        // 지정된 등급의 총 대기시간 반환
        return waitingTimes[mGrade-1];
    }
}