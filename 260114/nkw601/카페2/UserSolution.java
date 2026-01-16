import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
    private int N, orderIdx, orderCnt;
    private ArrayList<Order> orders;
    private HashMap<Integer, Order> orderMap;
    private PriorityQueue<CurOrder> hurry;

    public class Order {
        int mID;
        int remaining;
        int idx;
        ArrayList<Integer> beverages;
        ArrayList<Integer> madeBevs;

        boolean isCanceled;
        boolean isCompleted;

        public Order(int mID, int remaining, int[] mbeverages) {
            this.mID = mID;
            this.remaining = remaining;

            this.beverages = new ArrayList<>();
            for (int b : mbeverages)
                this.beverages.add(b);

            this.madeBevs = new ArrayList<>();
            this.idx = ++orderIdx;

            isCanceled = false;
            isCompleted = false;
        }
    }

    private class CurOrder {
        int mID;
        int remaining;
        int idx;

        CurOrder(int mID, int remaining, int idx) {
            this.mID = mID;
            this.remaining = remaining;
            this.idx = idx;
        }
    }

    public void init(int N) {
        // 초기화
        this.N = N;
        orderIdx = 0;
        orders = new ArrayList<>();
        orderCnt = 0;
        orderMap = new HashMap<>();

        // 1. 남은 음료 수, 2. 주문 순으로 정렬
        hurry = new PriorityQueue<>((o1, o2) -> {
            if (o1.remaining != o2.remaining)
                return Integer.compare(o2.remaining, o1.remaining);
            return Integer.compare(o1.idx, o2.idx);
        });
    }

    public int order(int mID, int M, int mBeverages[]) {
        Order order = new Order(mID, M, mBeverages);
        orders.add(order);
        orderMap.put(mID, order);
        hurry.offer(new CurOrder(order.mID, order.remaining, order.idx)); // 처리했으니까 pq에 넣어주기

        orderCnt++;
        return orderCnt;
    }

    public int supply(int mBeverage) {
        for (Order order : orders) {
            // 있으면: 주문 리스트에서 제거하고, 만든 리스트에 더하기
            if (order.isCanceled || order.isCompleted)
                continue;

            if (order.beverages.contains(mBeverage)) {
                order.beverages.remove((Integer) mBeverage);
                order.madeBevs.add(mBeverage);

                if (--order.remaining == 0) {
                    order.isCompleted = true;
                    orderCnt--;
                }

                hurry.offer(new CurOrder(order.mID, order.remaining, order.idx)); // 처리했으니까 pq에 넣어주기
                return order.mID;
            }
        }
        // 끝까지 못 찾으면 -1
        return -1;
    }

    public int cancel(int mID) {
        Order order = orderMap.get(mID);

        if (order.isCompleted)
            return 0;
        if (order.isCanceled)
            return -1;

        // 취소처리
        order.isCanceled = true;
        orderCnt--;

        // 재배치
        for (int bev : order.madeBevs) {
            supply(bev);
        }

        return order.remaining;
    }

    public int getStatus(int mID) {
        Order o = orderMap.get(mID);
        if (o == null)
            return -1;
        if (o.isCanceled)
            return -1;
        if (o.isCompleted)
            return 0;
        return o.remaining;
    }

    Solution.RESULT hurry() {
        Solution.RESULT res = new Solution.RESULT();
        ArrayList<CurOrder> temp = new ArrayList<>();
        res.cnt = 0;
        while (res.cnt < 5 && !hurry.isEmpty()) {
            CurOrder order = hurry.poll();

            // 남은 음료 수 다르면 유효한 주문 아님
            if (!isValid(order))
                continue;

            temp.add(order);
            res.IDs[res.cnt++] = order.mID;
        }

        for (CurOrder t : temp) {
            hurry.offer(t);
        }

        return res;
    }

    boolean isValid(CurOrder order) {
        Order curOrder = orderMap.get(order.mID);
        if (curOrder == null || curOrder.isCanceled || curOrder.isCompleted)
            return false;

        return curOrder.remaining == order.remaining;
    }
}