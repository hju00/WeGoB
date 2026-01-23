import java.util.*;

class UserSolution
{
    // 주요 자료구조 및 알고리즘 : 지연 갱신, 버킷, 큐(배열 구현), 우선순위 큐

    // 주문 정보 관리 클래스
    // 각 주문별 진행 현황을 클래스 내부에서 관리하고자 한다.
    // 따라서 Order 클래스 내부에 현재 배치된 음료 수, 전체 음료 수, 주문 순서, 배치된 음료 목록, 완료 여부를 관리한다.
    static class Order {
        // 주문 번호, 현재 배치된 음료 수, 요청받은 전체 음료 수, 주문 순서
        int id, cnt, total, orderNumber;
        // 현재까지 배치된 음료 번호
        int[] beverages;
        // 주문에 대한 완료 처리 여부
        boolean finished;

        public Order(int id, int total, int orderNumber) {
            this.id = id;
            this.cnt = 0;
            this.total = total;
            this.orderNumber = orderNumber;
            this.beverages = new int[total];
            this.finished = false;
        }
    }

    // 가장 급한 주문 목록 출력을 위한 우선순위 큐 노드 클래스
    // 지연 갱신을 시도할 것이다. 따라서 cnt에는 주문 생성 또는 갱신 시점의 남은 음료 수를 입력한다.
    // 가장 급한 주문의 정렬 기준
    // 정렬 기준 1 : 처리해야 할 음료 수가 많은 주문부터
    // 정렬 기준 2 : 가장 먼저 들어온 주문부터
    static class Node implements Comparable<Node> {
        // 주문 번호, 배치해야 할 음료 수, 주문 순서
        int id, cnt, orderNumber;

        public Node(int id, int cnt, int orderNumber) {
            this.id = id;
            this.cnt = cnt;
            this.orderNumber = orderNumber;
        }

        @Override
        public int compareTo(Node o) {
            if(this.cnt == o.cnt) return this.orderNumber - o.orderNumber;
            return o.cnt - this.cnt;
        }
    }

    // 배열형 연결 리스트의 최대 크기, 버킷 리스트 최대 크기
    // 각 버킷의 배열 크기 선정 이유 : 동일한 번호의 음료가 계속 들어올 수 있음을 가정
    static final int MAX_BEVERAGE_SIZE = 215000, MAX_N = 11;
    // 음료 종류 개수, 현재 활성화 상태인 주문 수, 전체 주문 수(삭제, 완료된 주문도 포함)
    int n, activeOrderCnt, totalOrderCnt;
    // 주문 번호를 key값으로 하는 주문 목록
    HashMap<Integer, Order> orders = new HashMap<>();
    // 하단 버킷리스트 내 음료 큐의 출구 위치, 입구 위치
    int[] queueFront, queueBack;
    // 음료 종류별 배치해야 할 주문 번호를 다루는 배열큐 버킷리스트
    int[][] beverageQueue;
    // 가장 급한 주문 5개를 출력하기 위한 우선순위 큐
    PriorityQueue<Node> q = new PriorityQueue<>();

    public void init(int N)
    {
        n = N;
        activeOrderCnt = 0;
        totalOrderCnt = 0;
        orders.clear();
        q.clear();

        if(beverageQueue == null) {
            beverageQueue = new int[MAX_N][MAX_BEVERAGE_SIZE];
            queueFront = new int[MAX_N];
            queueBack = new int[MAX_N];
        }

        for (int i = 1; i < MAX_N; i++) {
            queueFront[i] = 0;
            queueBack[i] = 0;
        }
    }

    // 음료 주문 생성 함수
    // 호출 횟수 최대 20_000회
    public int order(int mID, int M, int[] mBeverages)
    {
        // 주문 객체 생성 및 해시맵에 등록
        Order newOrder = new Order(mID, M, totalOrderCnt);
        orders.put(mID, newOrder);

        // 주어지는 음료 번호를 받아 각 음료 큐에 주문 번호 추가
        // 같은 음료 종류가 여러 번 나올 수 있음
        for(int i=0; i<M; i++) {
            int bID = mBeverages[i];
            beverageQueue[bID][queueBack[bID]++] = mID;
        }

        // 가장 급한 주문을 처리하는 우선순위 큐에 주문 정보 추가
        // 추가로 전체 주문 카운트와, 활성 상태인 주문 카운트 증가
        q.add(new Node(mID, M, totalOrderCnt++));
        return ++activeOrderCnt;
    }

    // 음료 생성 및 배치 함수
    // 호출 횟수 : 최대 50_000회
    public int supply(int mBeverage)
    {
        // 음료 종류에 해당하는 큐를 즉시 찾아서 순차 탐색
        // front와 back의 값이 동일하면 음료를 배치할 주문이 없다는 의미 -> 음료는 즉시 폐기
        // 즉시 첫번째 값을 꺼내지 않는 이유는 이미 삭제된 주문의 번호일 수 있기 때문
        while(queueFront[mBeverage] != queueBack[mBeverage]) {
            int targetIdx = beverageQueue[mBeverage][queueFront[mBeverage]++];
            Order target = orders.get(targetIdx);

            // 해당 주문이 이미 삭제된 경우(해시맵에 없음) -> 다음 주문 번호 확인
            if (target == null) continue;

            // 해당 주문에 음료 배치
            // 이번 배치로 target 주문에 모든 음료가 배치된 상태면 finished 태그 활성화 및 활성 상태 주문 카운트 감소
            target.beverages[target.cnt++] = mBeverage;
            if(target.cnt == target.total) {
                target.finished = true;
                activeOrderCnt--;
            }

            return targetIdx;
        }
        return -1;
    }

    // 주문 취소 함수
    // 호출 횟수 : 최대 1_000회
    public int cancel(int mID)
    {
        // 지정된 주문의 진행 현황 확인
        // 이미 완료된 주문이거나, 취소된 주문의 경우 즉시 종료
        int res = getStatus(mID);
        if(res < 1) return res;

        // 취소된 주문은 해시맵에서 제거
        Order target = orders.remove(mID);

        // 지금까지 배치된 음료가 있으면 다른 주문에 재배치하기 위해 supply 함수 호출(최대 9회 호출)
        for(int i=0; i<target.cnt; i++) {
            supply(target.beverages[i]);
        }

        // 활성 상태 주문 카운트 감소
        activeOrderCnt--;

        return res;
    }

    // 지정 주문의 진행 현황 확인 함수
    // 호출 횟수 : 최대 5_000회
    public int getStatus(int mID)
    {
        // 현재 주문의 상태 반환
        // 해시맵에 없는 경우 -> 이미 삭제된 주문
        // 주문의 finished 태그 활성화 -> 이미 모든 음료가 배치된 주문
        // 그 외 : 아직 배치해야 할 음료가 남은 주문
        Order target = orders.get(mID);
        if(target == null) return -1;
        if(target.finished) return 0;

        return target.total - target.cnt;
    }

    // 가장 급한 주문 최대 5개 반환 함수
    // 호출 횟수 : 최대 10_000회
    Solution.RESULT hurry()
    {
        // RESULT 객체에 반환하려는 주문의 수 및 각 주문의 번호를 담아서 반환
        Solution.RESULT res = new Solution.RESULT();

        // 가장 급한 주문 최대 5개를 순서대로 저장하기 위한 임시 큐 생성
        Queue<Node> temp = new ArrayDeque<>();

        // 정렬 기준 1 : 처리해야 할 음료 수가 많은 주문부터
        // 정렬 기준 2 : 가장 먼저 들어온 주문부터

        // 우선순위 큐에서 주문 정보를 하나씩 반환받아서 확인
        // 위 정렬 기준에 맞는 주문 최대 5개의 번호를 RESULT 객체에 담아 반환
        res.cnt = 0;
        while(!q.isEmpty() && res.cnt < 5) {
            Node cur = q.poll();

            // 만약 해당 주문이 이미 완료되었거나 취소된 경우 무시
            if(!orders.containsKey(cur.id)) continue;
            Order target = orders.get(cur.id);
            if(target.finished) continue;

            // 만약 Node 객체의 cnt와 실제 Order 객체의 남은 음료 수가 다르면 최신 버전 정보가 아닌 것임
            // 따라서 해당 Node 객체의 cnt를 최신 값으로 갱신해서 다시 우선순위 큐에 삽입
            if(target.total - target.cnt < cur.cnt) {
                cur.cnt = target.total - target.cnt;
                q.add(cur);
                continue;
            }

            temp.add(cur);
            res.IDs[res.cnt++] = cur.id;
        }

        // 우선순위 큐에서 빼낸 가장 급한 주문 5개는 여전히 음료를 받아야 하므로, 다시 우선순위 큐에 삽입
        while(!temp.isEmpty()) {
            q.add(temp.poll());
        }

        return res;
    }
}
