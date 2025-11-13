import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class UserSolution {
    static final int MAX_LORDS = 625;

    static int N;
    static int lordCnt;
    static Lord[] lords;
    static int[][] lordIds; // 각 칸의 군주 ID 저장
    static HashMap<String, Integer> nameToId; // 군주 이름으로 영토 ID 바로 찾기 위해서
    static int[] parents; // 해당 영토가 속한 동맹 ID
    static Set<Integer>[] enemies; // 적대관계(루트 기준)
    static int[] dx = {-1,-1,0,1,1,1,0,-1};
    static int[] dy = {0,1,1,1,0,-1,-1,-1};

    static class Lord {
        String name;
        int row, col;
        int soldierCnt;

        Lord(String name, int row, int col, int soldierCnt) {
            this.name = name;
            this.row = row;
            this.col = col;
            this.soldierCnt = soldierCnt;
        }
    }

    /**
     *
     * @param N: 전체 영토의 크기 (4 ≤ N ≤ 25, 16 ≤ N x N ≤ 625)
     * @param mSoldier: 각 영토의 병사 수 (4 ≤ mSoldier[][] ≤ 200)
     * @param mMonarch: 각 영토의 군주의 이름 (4 ≤ 이름 길이 ≤ 10)
     */
    void init(int N, int mSoldier[][], char mMonarch[][][]) {
        this.N = N;
        this.lordCnt = 0;

        nameToId = new HashMap<>();
        parents = new int[MAX_LORDS];
        enemies = new HashSet[MAX_LORDS];
        lords = new Lord[MAX_LORDS];
        lordIds = new int[N][N];

        // 군주 이름 저장, 영토ID 저장
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // 이름 -> 영토ID: O(1), 영토ID -> 이름: O(1)
                String name = charArrayToString(mMonarch[i][j]);
                nameToId.put(name, lordCnt);
                lords[lordCnt] = new Lord(name, i, j, mSoldier[i][j]);
                lordIds[i][j] = lordCnt;

                lordCnt++;
            }
        }

        // 동맹 관계 초기화
        for (int i = 0; i < MAX_LORDS; i++) {
            parents[i] = i;
        }

        // 적대 관계 초기화
        for (int i = 0; i < MAX_LORDS; i++) {
            enemies[i] = new HashSet<Integer>();
        }
    }

    void destroy() {

    }

    /**
     * A의 동맹과 B의 동맹이 동맹을 맺는다. A==B or 이미 동맹관계면 -1 동맹끼리 적대관계에 있으면 -2 위의 두 경우가 아니면
     * 동맹관계, 1
     *
     * @param mMonarchA: A의 이름 (4 ≤ 길이 ≤ 10)
     * @param mMonarchB: B의 이름 (4 ≤ 길이 ≤ 10)
     * @return 동맹 결과
     */
    int ally(char mMonarchA[], char mMonarchB[]) {
        String monarchA = charArrayToString(mMonarchA);
        String monarchB = charArrayToString(mMonarchB);

        int AId = nameToId.get(monarchA);
        int BId = nameToId.get(monarchB);

        int AGroupId = find(AId);
        int BGroupId = find(BId);

        // 동맹 관계 확인
        if (AId == BId || AGroupId == BGroupId) {
            return -1;
        }

        // 적대 관계 확인
        if (enemies[AGroupId].contains(BGroupId) || enemies[BGroupId].contains(AGroupId)) {
            return -2;
        }

        // 동맹 만들기
        union(AGroupId, BGroupId);

        // 적대 관계 합치기
        int newRoot = find(AGroupId);
        HashSet<Integer> newEnemies = new HashSet<>();
        newEnemies.addAll(enemies[AGroupId]);
        newEnemies.addAll(enemies[BGroupId]);
        enemies[newRoot] = newEnemies;

        // 적들의 적대관계도 업데이트
        for(int enemy : newEnemies) {
            enemies[enemy].remove(AGroupId);
            enemies[enemy].remove(BGroupId);
            enemies[enemy].add(newRoot);
        }

        return 1;
    }

    /**
     * A의 동맹들이 B의 영토를 공격한다. 지휘 장수는 mGeneral / 동맹이면 -1, 동맹의 영토가 인접하지 않으면 -2 (전투 X) 공격
     * 성공 1, 방어 성공 또는 모든 병사 사망 시 0
     *
     * @param mMonarchA: 공격하는 군주 이름
     * @param mMonarchB: 공격받는 군주 이름
     * @param mGeneral: 공격 지휘하는 장수 이름 -> 승리 시 해당 영토의 군주가 됨
     * @return 공격 결과
     */
    int attack(char mMonarchA[], char mMonarchB[], char mGeneral[]) {
        String monarchA = charArrayToString(mMonarchA);
        String monarchB = charArrayToString(mMonarchB);
        String general = charArrayToString(mGeneral);

        // 1. 동맹이면 전투 X, -1
        if(find(nameToId.get(monarchA)) == find(nameToId.get(monarchB))) {
            return -1;
        }

        // 2. 8방을 보며 공격팀, 방어팀 찾기
        int id = nameToId.get(monarchB); // 방어 지점
        Lord lordB = lords[id];
        int x = lordB.row;
        int y = lordB.col;
        List<Lord> attackTeam = new ArrayList<>();
        List<Lord> defenseTeam = new ArrayList<>();
        int attackGroupId = find(nameToId.get(monarchA));
        int defenseGroupId = find(nameToId.get(monarchB));

        for(int dir = 0; dir < 8; dir++) {
            int nx = x + dx[dir];
            int ny = y + dy[dir];

            if(!isIn(nx, ny)) continue;

            int nextId = lordIds[nx][ny];
            if(find(nextId) == attackGroupId) {
                attackTeam.add(lords[nextId]);
            }else if(find(nextId) == defenseGroupId) {
                defenseTeam.add(lords[nextId]);
            }
        }

        // A의 동맹이 B 영토와 인접하지 않은 경우
        if(attackTeam.isEmpty()) {
            return -2;
        }

        // 전투 시작
        // 3. 적대 관계 갱신
        enemies[attackGroupId].add(defenseGroupId);
        enemies[defenseGroupId].add(attackGroupId);

        // 4. 병사 계산
        int attackCnt = 0;
        int defenseCnt = 0;
        for(Lord lord : attackTeam) {
            attackCnt += lord.soldierCnt / 2;
            lord.soldierCnt = lord.soldierCnt - lord.soldierCnt / 2;
        }

        for(Lord lord : defenseTeam) {
            defenseCnt += lord.soldierCnt / 2;
            lord.soldierCnt = lord.soldierCnt - lord.soldierCnt / 2;
        }

        lordB.soldierCnt += defenseCnt;

        // 공격 성공 시
        if(attackCnt > lordB.soldierCnt) {
            int remainCnt = attackCnt - lordB.soldierCnt;

            // B그룹 적대 관계 정리 -> A 동맹에 들어가므로 B동맹 해제해야함.

            // 새 군주 등록
            nameToId.put(general, id);
            lords[id] = new Lord(general, x, y, remainCnt);
            parents[id] = attackGroupId;

            return 1;
        }else {
            // 방어 성공
            lordB.soldierCnt -= attackCnt;

            return 0;
        }
    }


    /**
     * option 0: 병사를 mNum만큼 모집, 병사 수를 return option 1: mMonarch 동맹에 모두 mNum만큼 모집, 모든
     * 병사 수 합산값 반환
     *
     * @param mMonarch: 군주의 이름
     * @param mNum: 병사의 수 (1 ≤ mNum ≤ 200)
     * @param mOption: 모집 조건
     * @return 병사의 수
     */
    int recruit(char mMonarch[], int mNum, int mOption) {
        String monarch = charArrayToString(mMonarch);
        int id = nameToId.get(monarch);
        int root = find(id);

        // option이 0일때
        if(mOption == 0) {
            lords[id].soldierCnt += mNum;

            return lords[id].soldierCnt;
        }else {
            int sum = 0;

            for(int i = 0; i < lordCnt; i++) {
                if(lords[i] != null && find(i) == root) {
                    lords[i].soldierCnt += mNum;
                    sum += lords[i].soldierCnt;
                }
            }

            return sum;
        }
    }



    private boolean isIn(int nx, int ny) {
        return nx >= 0 && nx < N && ny >= 0 && ny < N;
    }

    public int find(int x) {
        if (parents[x] == x) {
            return x;
        }
        return parents[x] = find(parents[x]); // 경로 압축
    }

    public void union(int a, int b) {
        int parentA = find(a);
        int parentB = find(b);

        if(parentA == parentB) return;

        if(parentA < parentB) {
            parents[parentB] = parentA;
        }else {
            parents[parentA] = parentB;
        }
    }

    private String charArrayToString(char[] arr) {
        int len = 0;
        while (len < arr.length && arr[len] != '\0') {
            len++;
        }
        return new String(arr, 0, len);
    }
}