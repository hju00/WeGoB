import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {

    static class Space implements Comparable<Space> {
        int left;
        int right;
        int len;
        boolean isRemoved;

        public Space(int left, int right, int len) {
            this.left = left;
            this.right = right;
            this.len = len;
        }

        public int compareTo(Space o) {
            if (this.len == o.len) {
                return Integer.compare(this.left, o.left);
            }
            return Integer.compare(o.len, this.len);
        }

        @Override
        public String toString() {
            return "Space " + left + " " + right + " " + len;
        }
    }

    static class Building {
        int left;
        int right;
        int len;

        public Building(int left, int right, int len) {
            this.left = left;
            this.right = right;
            this.len = len;
        }

        @Override
        public String toString() {
            return "Building " + left + " " + right + " " + len;
        }
    }

    static int N;
    static HashMap<Integer, Space> spaces;
    static HashMap<Integer, Building> buildings;
    static PriorityQueue<Space> PQ;

    /**
     * 초기화, 시작 빈공간 추가
     *
     * @param N: 토지의 길이 (30 <= N <= 100_000_000
     */
    public void init(int N) {
        this.N = N;

        spaces = new HashMap<>();
        buildings = new HashMap<>();
        PQ = new PriorityQueue<>();

        int left = 0;
        int right = N - 1;
        int len = N;
        Space space = new Space(left, right, len);
        PQ.add(space);
        spaces.put(left, space);
        spaces.put(right, space);
    }

    /**
     * 가장 긴 빈공간을 꺼내 가능하다면 빌딩을 건설하고 빈공간을 쪼갠다.
     *
     * @param mLength: 건설할 빌딩의 길이 (1 ≤ mLength ≤ N)
     * @return 건설된 빌딩의 주소(OR -1)
     */
    public int build(int mLength) {
        // Lazy 처리
        while (!PQ.isEmpty() && PQ.peek().isRemoved) {
            PQ.poll();
        }

        if (!PQ.isEmpty()) {
            if (mLength <= PQ.peek().len) {
                Space space = PQ.poll();
                int left = space.left;
                int right = space.right;

                spaces.remove(left);
                spaces.remove(right);

                // 새로운 빌딩 건설
                int diff = space.len - mLength;
                int leftSpaceLen = diff / 2;

                int BuildingLeft = space.left + leftSpaceLen; // 시작 위치
                int BuildingRight = BuildingLeft + mLength - 1;
                Building building = new Building(BuildingLeft, BuildingRight, mLength);
//                System.out.println(building);
                buildings.put(BuildingLeft, building);

                // 0 ~ 2개 사이에 생긴 space 추가
                if (left < BuildingLeft) {
                    Space space1 = new Space(left, BuildingLeft - 1, BuildingLeft - left);
                    spaces.put(left, space1);
                    spaces.put(BuildingLeft - 1, space1);
//                    System.out.println(space1);
                    PQ.add(space1);
                }

                if (right > BuildingRight) {
                    Space space2 = new Space(BuildingRight + 1, right, right - BuildingRight);
                    spaces.put(BuildingRight + 1, space2);
                    spaces.put(right, space2);
//                    System.out.println(space2);
                    PQ.add(space2);
                }

                return BuildingLeft;
            }
        }

        return -1;
    }

    /**
     * 주소로 Map에서 빌딩 지우기, 양옆에 있는 빈공간을 불러와서 removed로 바꾸고 새로운 빈공간을 추가해주기
     *
     * @param mAddr: 제거할 빌딩의 주소 (0 ≤ mAddr ≤ N-1)
     * @return 제거된 빌딩 길이(OR -1)
     */
    public int demolish(int mAddr) {
        if(!buildings.containsKey(mAddr)) return -1;

        // 빌딩 삭제
        Building cur = buildings.get(mAddr);
        buildings.remove(mAddr);

        // 생성할 새로운 빈공간 값들, 최소 빌딩 크기만큼 빈공간
        int newLeft = cur.left;
        int newRight = cur.right;
        int newLen = cur.len;

        // 왼쪽 빈공간 있다면?
        if (spaces.containsKey(cur.left - 1)) {
            Space space1 = spaces.get(cur.left - 1);

            newLeft = space1.left;
            newLen += space1.len;

            // isRemoved
            space1.isRemoved = true;

            spaces.remove(space1.left);
            spaces.remove(space1.right);
        }

        // 오른쪽 빈공간 있다면?
        if (spaces.containsKey(cur.right + 1)) {
            Space space2 = spaces.get(cur.right + 1);

            newRight = space2.right;
            newLen += space2.len;

            // isRemoved
            space2.isRemoved = true;

            spaces.remove(space2.left);
            spaces.remove(space2.right);
        }

        // 새로운 빈공간 생성, PQ, spaces에 모두 추가
        Space newSpace = new Space(newLeft, newRight, newLen);
        PQ.add(newSpace);

        spaces.put(newLeft, newSpace);
        spaces.put(newRight, newSpace);

        // 빌딩 지웠다면 제거된 빌딩 길이 반환
        return cur.len;
    }
}