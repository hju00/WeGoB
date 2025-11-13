import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class UserSolution {

    // 맵의 최대 크기 (25x25)
    static final int MAX_MAP_SIZE = 25 * 25;
    
    // 현재 맵의 한 변의 길이
    int N;

    /**
     * 각 영토(군주)의 정보를 저장하는 클래스
     */
    class Monarch {
        int r, c;       // 영토의 좌표
        int soldiers;   // 현재 병사 수
        String name;    // 현재 군주 이름

        Monarch(int r, int c, int soldiers, String name) {
            this.r = r;
            this.c = c;
            this.soldiers = soldiers;
            this.name = name;
        }
    }

    // 1. N*N 개의 영토 정보를 1차원 배열로 관리
    Monarch[] territories;
    
    // 2. 군주 이름(String)으로 영토의 고유 ID(int)를 빠르게 찾기 위한 맵
    Map<String, Integer> nameToId;
    
    // 3. Union-Find를 위한 parent 배열
    // parent[i] := i번 영토가 속한 동맹의 '대표(root)' ID
    int[] parent;
    
    // 4. 동맹 관리를 위한 Set
    // allianceMembers[i] := i가 '대표'인 동맹에 속한 모든 영토 ID의 Set
    Set<Integer>[] allianceMembers;
    
    // 5. 적대 관계 관리를 위한 Set
    // hostileTo[i] := i가 '대표'인 동맹과 적대 관계인 '다른 동맹의 대표' ID의 Set
    Set<Integer>[] hostileTo;
    
    // 8방향 탐색을 위한 배열 (상, 상우, 우, 하우, 하, 하좌, 좌, 상좌)
    int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
    int[] dc = {0, 1, 1, 1, 0, -1, -1, -1};

    /**
     * (r, c) 2차원 좌표를 1차원 고유 ID로 변환
     */
    int getId(int r, int c) {
        return r * N + c;
    }

    /**
     * char[]를 String으로 변환
     */
    String charToString(char[] cArr) {
        StringBuilder sb = new StringBuilder();
        for (char ch : cArr) {
            if (ch == '\0') break;
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Union-Find의 find 연산 (O(1))
     * 이 구현에서는 '평탄화된' 구조를 사용하므로,
     * parent[id]가 항상 해당 동맹의 '대표' ID를 가리킴.
     */
    int find(int id) {
        return parent[id];
    }

    void init(int N, int mSoldier[][], char mMonarch[][][]) {
        this.N = N;
        int mapSize = N * N;

        // 모든 자료구조 초기화
        territories = new Monarch[mapSize];
        nameToId = new HashMap<>();
        parent = new int[mapSize];
        allianceMembers = new Set[mapSize];
        hostileTo = new Set[mapSize];

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int id = getId(r, c);
                String name = charToString(mMonarch[r][c]);

                // 1. 영토 정보 저장
                territories[id] = new Monarch(r, c, mSoldier[r][c], name);
                
                // 2. 이름 -> ID 맵핑
                nameToId.put(name, id);
                
                // 3. Union-Find 초기화 (모두 자기 자신이 대표)
                parent[id] = id;
                
                // 4. 동맹원 Set 초기화 (자기 자신만 포함)
                allianceMembers[id] = new HashSet<>();
                allianceMembers[id].add(id);
                
                // 5. 적대관계 Set 초기화 (비어 있음)
                hostileTo[id] = new HashSet<>();
            }
        }
    }

    void destroy() {
    }

    int ally(char mMonarchA[], char mMonarchB[]) {
        
        String nameA = charToString(mMonarchA);
        String nameB = charToString(mMonarchB);

        // `get` 호출 (이제 안전함)
        int idA = nameToId.get(nameA);
        int idB = nameToId.get(nameB);

        int rootA = find(idA);
        int rootB = find(idB);

        // Case 1: 이미 같은 동맹
        if (rootA == rootB) {
            return -1;
        }
        
        // Case 2: 서로 적대 관계
        if (hostileTo[rootA].contains(rootB)) {
            return -2;
        }

        // Case 3: 동맹 성공 (Union-by-Size 최적화)
        // 항상 크기가 작은 동맹(rootA)을 큰 동맹(rootB)에 합침
        if (allianceMembers[rootA].size() > allianceMembers[rootB].size()) {
            int tempRoot = rootA;
            rootA = rootB;
            rootB = tempRoot;
        }

        // --- union ---

        // 1. rootA에 속한 모든 영토의 '대표'를 rootB로 변경 (평탄화)
        for (int memberId : allianceMembers[rootA]) {
            parent[memberId] = rootB;
        }
        
        // 2. rootA의 동맹원들을 rootB의 동맹원 목록에 추가
        allianceMembers[rootB].addAll(allianceMembers[rootA]);
        
        // 3. rootA의 적대 목록을 rootB의 적대 목록에 추가
        hostileTo[rootB].addAll(hostileTo[rootA]);
        
        // 4. rootA와 적대적이던 동맹들에게, "이제 너희의 적은 rootB야"라고 알려줌
        for (int enemyRoot : hostileTo[rootA]) {
            hostileTo[enemyRoot].remove(rootA);
            hostileTo[enemyRoot].add(rootB);
        }

        // 5. rootA는 더 이상 대표가 아니므로, 목록을 비움
        allianceMembers[rootA].clear();
        hostileTo[rootA].clear();

        return 1;
    }

    int attack(char mMonarchA[], char mMonarchB[], char mGeneral[]) {
        int idA = nameToId.get(charToString(mMonarchA)); // 공격자
        int idB = nameToId.get(charToString(mMonarchB)); // 방어자 영토
        String generalName = charToString(mGeneral); // 공격 지휘 장수

        int rootA = find(idA);
        int rootB = find(idB);

        // Case 1: 같은 동맹 공격
        if (rootA == rootB) {
            return -1;
        }

        Monarch targetTerritory = territories[idB];
        int attackPower = 0;
        boolean isAdjacent = false;

        Map<Integer, Integer> attackSupporters = new HashMap<>();
        Map<Integer, Integer> defenseSupporters = new HashMap<>();

        // 1. 공격력(AP) 계산 및 공격 지원군 식별
        for (int i = 0; i < 8; i++) {
            int nr = targetTerritory.r + dr[i];
            int nc = targetTerritory.c + dc[i];

            if (nr < 0 || nr >= N || nc < 0 || nc >= N) continue;

            int neighborId = getId(nr, nc);
            if (find(neighborId) == rootA) {
                isAdjacent = true;
                int sentSoldiers = territories[neighborId].soldiers / 2;
                attackPower += sentSoldiers;
                attackSupporters.put(neighborId, sentSoldiers); // 지원군과 보낸 병력 수 저장
            }
        }

        // Case 2: 인접한 동맹군이 없음 (공격 불가)
        if (!isAdjacent) {
            return -2;
        }

        // 전투 발생! -> 즉시 서로 적대 관계가 됨
        hostileTo[rootA].add(rootB);
        hostileTo[rootB].add(rootA);

        // 2. 방어력(DP) 계산 및 방어 지원군 식별
        int defensePower = targetTerritory.soldiers;
        
        for (int i = 0; i < 8; i++) {
            int nr = targetTerritory.r + dr[i];
            int nc = targetTerritory.c + dc[i];

            if (nr < 0 || nr >= N || nc < 0 || nc >= N) continue;

            int neighborId = getId(nr, nc);
            if (neighborId != idB && find(neighborId) == rootB) {
                int sentSoldiers = territories[neighborId].soldiers / 2;
                defensePower += sentSoldiers;
                defenseSupporters.put(neighborId, sentSoldiers); // 지원군과 보낸 병력 수 저장
            }
        }

        for (Map.Entry<Integer, Integer> entry : attackSupporters.entrySet()) {
            territories[entry.getKey()].soldiers -= entry.getValue();
        }
        for (Map.Entry<Integer, Integer> entry : defenseSupporters.entrySet()) {
            territories[entry.getKey()].soldiers -= entry.getValue();
        }

        // --- 4. 전투 결과 판정 ---
        if (attackPower > defensePower) {
            // [공격 성공]
            
            // 4-1. 기존 군주 이름 저장 (맵에서 제거하기 위해)
            String oldMonarchName = targetTerritory.name;

            // 4-2. 새 군주, 새 병력 설정
            targetTerritory.soldiers = attackPower - defensePower; 
            targetTerritory.name = generalName;                   

            // 4-3. nameToId 맵 업데이트 (NPE 핵심 해결)
            nameToId.remove(oldMonarchName); // 이전 군주 이름 제거
            nameToId.put(generalName, idB);      // 새 군주 이름과 ID 추가
            
            // 4-4. 동맹 변경 (영토가 rootB -> rootA로 편입됨)
            allianceMembers[rootB].remove(idB);
            allianceMembers[rootA].add(idB);
            parent[idB] = rootA;

            return 1;
        } else {
            // [공격 실패] (방어 성공 또는 무승부)
            targetTerritory.soldiers = defensePower - attackPower; // 남은 병력
            return 0;
        }
    }

    int recruit(char mMonarch[], int mNum, int mOption) {
        int id = nameToId.get(charToString(mMonarch));

        if (mOption == 0) {
            // Case 0: 해당 영토에만 모집
            territories[id].soldiers += mNum;
            return territories[id].soldiers;
            
        } else {
            // Case 1: 해당 군주의 '모든 동맹' 영토에 모집
            int root = find(id);
            int totalSoldiers = 0;
            
            // 대표(root)의 동맹원 목록(Set)을 순회
            for (int memberId : allianceMembers[root]) {
                territories[memberId].soldiers += mNum;
                totalSoldiers += territories[memberId].soldiers;
            }
            return totalSoldiers;
        }
    }
}