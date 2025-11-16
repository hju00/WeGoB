# Logic

1. 군주/영토의 상태 (병사 수, 군주 이름)와 관계 (동맹, 적대)를 분리하여 관리
-> Union-find

2. 동맹과 적대관계
* 동맹 관리: 각 동맹을 Set으로 관리.
* 적대 관계: 동맹(집합) 간의 적대 관계를 별도로 저장해야 함. 동맹이 합쳐질(union) 때, 적대 관계도 합쳐져야 함.

3. 상태 관리: 배열 및 해시맵

* 병력/군주: Monarch[] 같은 1차원 배열을 N*N 크기로 선언하여, 각 영토의 상태(병력, 군주명)를 ID로 직접 접근.

* 군주 이름 조회: 군주 이름(`String`)으로 영토 ID(int)를 빠르게 찾기 위해 `Map<String, Integer>` 사용.

* 상태 동기화: attack으로 군주가 교체될 때, `Monarch[]`의 군주 이름과 Map의 키(key)가 동시에 갱신

* 전투 시뮬레이션: 8방향 탐색 및 상태 갱신

* attack 시, 공격받는 영토 기준 8방향을 탐색하여 공격/방어 동맹을 식별하고 전투력(AP/DP)을 계산

* 숨겨진 규칙 : 전투에 참여(지원)한 모든 영토는 보낸 병력`(soldiers / 2)`만큼 영구적으로 병력이 차감

* 상태 갱신 : 전투 승리 시, 패배한 영토의 군주가 `mGeneral`로 교체

# Data Structure

1. 영토 상태: `Monarch` 클래스(좌표, 병력, 군주명)를 정의하고, `Monarch[] territories` 배열로 N*N개의 영토 정보를 ID로 관리.

2. 군주 이름 조회: String 군주 이름으로 int 영토 ID를 O(1)에 찾기 위해 `Map<String, Integer> nameToId` 사용.

3. 동맹 관계 (Union-Find): int[] parent 배열. parent[i]는 i번 영토가 속한 동맹의 '대표(root)' ID를 저장.

* 동맹원 관리: `Set<Integer>[] allianceMembers`
    parent만으로는 동맹의 모든 구성원을 알기 힘듦. allianceMembers[root]에 해당 동맹의 모든 영토 ID를 저장하여 recruit(option=1) 및 ally 합병 시 사용.

* 적대 관계: `Set<Integer>[] hostileTo`
    hostileTo[root]에 해당 동맹과 적대 관계인 다른 동맹의 대표(root) ID를 저장.

```java
class Monarch {
	int r, c;
	int soldiers;
	String name;
}
Monarch[] territories;
Map<String, Integer> nameToId;
int[] parent;
Set<Integer>[] allianceMembers;
Set<Integer>[] hostileTo;
```

# Func

1. `init()`
```java
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
```

2. `ally(A, B)`
```java
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
```

3. `attack(A, B, General)`
```java
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
```

# 회고

/*
* 건실하게 풀자
*/
