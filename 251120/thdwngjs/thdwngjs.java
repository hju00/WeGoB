import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

class UserSolution {
    // 구간(건물 또는 빈 공간) 정보를 담는 클래스
    static class Node implements Comparable<Node> {
        int s, e;      // 시작 위치(start), 끝 위치(end)
        int length;    // 구간의 길이

        public Node(int s, int e) {
            this.s = s;
            this.e = e;
            this.length = e - s + 1;
        }

        // 기본 정렬 기준: 시작 위치(s) 오름차순
        // building TreeSet에서 사용됨 (위치 순서대로 정렬하여 이웃 건물을 찾기 위함)
        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.s, o.s);
        }
    }

    int N; // 전체 영역의 크기
    Map<Integer, Node> map; // 건물 시작 위치(mAddr) -> 건물 객체 매핑 (삭제 시 빠른 검색용)
    TreeSet<Node> space;    // 빈 공간들을 관리하는 집합
    TreeSet<Node> building; // 지어진 건물들을 관리하는 집합

    public void init(int N) {
        this.N = N;
        map = new HashMap<>();

        // space TreeSet 정렬 기준 (문제의 요구사항에 맞춘 '최적의 공간' 찾기)
        // 1. 길이가 긴 순서 (내림차순)
        // 2. 길이가 같다면 시작 위치가 빠른 순서 (오름차순)
        space = new TreeSet<>((o1, o2) -> {
            if (o1.length == o2.length) {
                return Integer.compare(o1.s, o2.s);
            }
            return Integer.compare(o2.length, o1.length);
        });

        // building TreeSet은 Node의 기본 compareTo(시작 위치 오름차순)를 사용
        building = new TreeSet<>();

        // 초기 상태: 0부터 N-1까지 전체가 하나의 빈 공간임
        space.add(new Node(0, N - 1));
    }

    // 건물 짓기: 가장 큰(그리고 왼쪽인) 빈 공간을 찾아 중앙에 건물을 배치
    public int build(int mLength) {
        // 빈 공간이 없거나, 가장 큰 빈 공간조차 건물을 짓기에 작다면 실패
        if (space.isEmpty() || space.first().length < mLength) {
            return -1;
        }

        // 가장 적합한 빈 공간(길이가 가장 길고, 시작점이 가장 빠른 공간)을 꺼냄
        Node cur = space.pollFirst();

        // 문제의 조건에 따라 건물을 빈 공간의 중앙에 배치하기 위한 시작점 계산
        // 식: 시작점 + (남은 공간) / 2
        int s = cur.s + (cur.length - mLength) / 2;
        int e = s + mLength - 1;

        // 새 건물 생성 및 등록
        Node newBuilding = new Node(s, e);
        building.add(newBuilding);
        map.put(s, newBuilding);

        // [공간 분할]
        // 건물을 지었으므로, 원래의 큰 빈 공간(cur)은 건물을 기준으로 '왼쪽 잔여 공간'과 '오른쪽 잔여 공간'으로 나뉨
        // 이를 계산하기 위해 새로 지은 건물 기준 바로 왼쪽/오른쪽 건물을 찾음
        Node lBuilding = building.lower(newBuilding);
        Node rBuilding = building.higher(newBuilding);

        // 왼쪽/오른쪽 건물이 없다면 경계는 전체 영역의 끝(-1, N)
        int l = (lBuilding == null) ? -1 : lBuilding.e;
        int r = (rBuilding == null) ? N : rBuilding.s;

        // 왼쪽 잔여 공간 생성 (왼쪽 건물 끝 + 1 ~ 새 건물 시작 - 1)
        Node lSpace = new Node(l + 1, newBuilding.s - 1);
        // 오른쪽 잔여 공간 생성 (새 건물 끝 + 1 ~ 오른쪽 건물 시작 - 1)
        Node rSpace = new Node(newBuilding.e + 1, r - 1);

        // 잔여 공간의 길이가 0보다 크면(유효하면) 빈 공간 목록에 다시 추가
        if (lSpace.length > 0) {
            space.add(lSpace);
        }
        if (rSpace.length > 0) {
            space.add(rSpace);
        }

        return newBuilding.s; // 건물의 시작 위치 반환
    }

    // 건물 철거: 건물을 삭제하고 좌우의 빈 공간을 합침
    public int demolish(int mAddr) {
        // 해당 위치에 건물이 없으면 실패
        if (!map.containsKey(mAddr)) {
            return -1;
        }

        // 삭제할 건물 정보 가져오기
        Node delBuilding = map.get(mAddr);
        
        // 건물 목록과 맵에서 제거
        building.remove(delBuilding);
        map.remove(mAddr);

        // [공간 병합]
        // 건물이 사라지면 [왼쪽 빈 공간] + [삭제된 건물 자리] + [오른쪽 빈 공간]이 합쳐져서 하나의 큰 공간이 됨
        
        // 삭제된 건물 기준으로 좌우에 있는 '건물'을 찾음 (병합될 공간의 전체 경계를 알기 위해)
        Node lBuilding = building.lower(delBuilding);
        Node rBuilding = building.higher(delBuilding);

        // 좌우 건물이 없다면 전체 경계가 됨
        int l = (lBuilding == null) ? -1 : lBuilding.e;
        int r = (rBuilding == null) ? N : rBuilding.s;

        // 삭제 전 존재했던 왼쪽 빈 공간과 오른쪽 빈 공간을 정의
        // (이 공간들은 이제 합쳐질 것이므로 space Set에서 제거해야 함)
        Node lSpace = new Node(l + 1, delBuilding.s - 1);
        Node rSpace = new Node(delBuilding.e + 1, r - 1);
        
        // 기존에 존재하던 작은 빈 공간들을 space 목록에서 제거
        // (TreeSet은 length와 s를 기준으로 비교하므로, 객체를 새로 만들어도 값이 같으면 제거됨)
        if (lSpace.length > 0) space.remove(lSpace);
        if (rSpace.length > 0) space.remove(rSpace);

        // 병합된 거대한 빈 공간 생성 (왼쪽 건물 끝+1 ~ 오른쪽 건물 시작-1)
        Node newSpace = new Node(l + 1, r - 1);
        // 병합된 공간을 space 목록에 추가
        space.add(newSpace);

        return delBuilding.length; // 철거된 건물의 길이 반환
    }
}
