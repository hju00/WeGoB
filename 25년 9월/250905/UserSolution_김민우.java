import java.util.*;

class UserSolution {


    static class Camp implements Comparable<Camp> {
        int id;
        int row;
        int col;
        int amount;

        Camp parent; // DSU의 부모 노드
        int groupAmount = 0; // 그룹의 총 채굴량 (대표 캠프에만 유효)

        public Camp(int id, int row, int col, int amount) {
            this.id = id;
            this.row = row;
            this.col = col;
            this.amount = amount;
        }

        @Override
        public int compareTo(Camp o) {
            // 우선순위 1: 채굴량이 적은 순서
            if (this.amount != o.amount) {
                return this.amount - o.amount;
            }
            // 우선순위 2: 행 번호가 작은 순서
            if (this.row != o.row) {
                return this.row - o.row;
            }
            // 우선순위 3: 열 번호가 작은 순서
            return this.col - o.col;
        }
    }

    int L, N;
    int maxLength; // N을 L로 나눈 값, 공간 분할 격자의 최대 인덱스

    List<Camp> groupLeaders; // 각 그룹의 대표(루트) 캠프들만 저장하는 리스트
    List<Camp>[][] camps; // 공간 분할을 위한 2D 격자

    void init(int L, int N) {
        this.L = L;
        this.N = N;
        this.maxLength = (N - 1) / L; // N/L 로 해도 되지만, N-1로 하여 인덱스 범위를 명확히 함

        groupLeaders = new ArrayList<>();
        // N <= L * 30 이므로 maxLength는 최대 29, 배열 크기는 31로 충분
        camps = new ArrayList[31][31]; 
        for (int row = 0; row <= maxLength; ++row) {
            for (int col = 0; col <= maxLength; ++col) {
                camps[row][col] = new ArrayList<>();
            }
        }
    }

    int addBaseCamp(int mID, int mRow, int mCol, int mQuantity) {
        Camp camp = new Camp(mID, mRow, mCol, mQuantity);
        int rowIdx = mRow / L;
        int colIdx = mCol / L;

        HashSet<Camp> prevLeaders = new HashSet<>();
        int groupAmount = mQuantity;
        Camp groupLeader = camp;

        // 공간 분할을 이용해 3x3 주변 셀만 탐색
        for (int row = rowIdx - 1; row <= rowIdx + 1; ++row) {
            for (int col = colIdx - 1; col <= colIdx + 1; ++col) {
                if (row < 0 || row > maxLength || col < 0 || col > maxLength) continue;

                for (Camp near : camps[row][col]) {
                    // 맨해튼 거리가 L 이하인 캠프만 고려
                    int dist = Math.abs(camp.row - near.row) + Math.abs(camp.col - near.col);
                    if (dist > L) continue;

                    // DSU의 find 연산으로 그룹의 대표를 찾음
                    Camp leader = getParent(near);
                    // 이미 처리한 그룹은 건너뜀
                    if (prevLeaders.contains(leader)) continue;

                    prevLeaders.add(leader);
                    groupLeaders.remove(leader); // 병합될 그룹이므로 대표 리스트에서 제거
                    groupAmount += leader.groupAmount;

                    // 새로운 통합 그룹의 대표를 우선순위에 따라 결정
                    if (groupLeader.compareTo(leader) > 0) {
                        groupLeader = leader;
                    }
                }
            }
        }


        prevLeaders.add(camp); // 새 캠프 자신도 포함
        for (Camp leader : prevLeaders) {
            leader.parent = groupLeader;
        }

        camps[rowIdx][colIdx].add(camp); // 공간 분할 격자에 새 캠프 추가
        groupLeader.groupAmount = groupAmount; // 새 대표의 총 채굴량 업데이트
        groupLeaders.add(groupLeader); // 새 대표를 대표 리스트에 추가
        
        return groupLeader.groupAmount;
    }


    int findBaseCampForDropping(int K) {
        // 비교를 위한 초기값. 어떤 유효한 캠프보다도 우선순위가 낮도록 설정
        Camp bestCamp = new Camp(-1, -1, -1, Integer.MAX_VALUE);
        
        // 관리 중인 그룹 대표들만 순회
        for (Camp leader : groupLeaders) {
            // K 이상을 수집할 수 있고, 현재까지 찾은 bestCamp보다 우선순위가 높다면 교체
            if (leader.groupAmount >= K && bestCamp.compareTo(leader) > 0) {
                bestCamp = leader;
            }
        }
        return bestCamp.id;
    }
    
    private Camp getParent(Camp camp) {
        if (camp.parent == null || camp.parent == camp) {
            camp.parent = camp;
            return camp;
        }
        return camp.parent = getParent(camp.parent);
    }
}
