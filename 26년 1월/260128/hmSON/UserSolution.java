import java.util.*;

class UserSolution {

    static final int MAX_STUDENT = 20_001; // 최대 학생 수는 add 호출 횟수에 따라 최대 20_000명까지 증가(1-based)
    int maxSelected, univCnt; // 대학별 최대 선발 인원 수, 대학 수
    int[][] university; // 대학별 가중치 정보
    int[] univNum = new int[MAX_STUDENT]; // 학생별 현재 입학 가능한 대학 번호
    boolean[] deleted = new boolean[MAX_STUDENT]; // 각 학생별 정보 삭제 여부(지연 삭제의 판단 조건)
    int[] studentCnt; // 대학별 실제 선발 후보에 든 학생 수
    int[][] score = new int[5][MAX_STUDENT]; // 학생별 학력 평가 점수
    PriorityQueue<int[]>[] status, ready; // 대학별 입시 현황. 0: 학생 번호, 1: 가중치가 적용된 총점

    public void init(int N, int M, int[][] mWeights) {
        maxSelected = N;
        univCnt = M;
        studentCnt = new int[M];

        // 각 대학별 가중치 입력
        university = new int[5][M];
        for(int i=0; i<M; i++) {
            for(int j=0; j<5; j++) {
                university[j][i] = mWeights[i][j];
            }
        }

        status = new PriorityQueue[M];
        ready = new PriorityQueue[M];
        for(int i=0; i<M; i++) {
            // 각 대학별 선발 학생 성적 오름차순 정렬(정원 초과시 성적이 낮은 학생을 방출하기 위함)
            status[i] = new PriorityQueue<>((a, b) -> a[1] != b[1] ? Integer.compare(a[1], b[1]) : Integer.compare(b[0], a[0]));
            // 각 대학별 대기 명단인 학생 성적 내림차순 정렬(정원 미달시 성적이 높은 학생을 추가 선발하기 위함)
            ready[i] = new PriorityQueue<>((a, b) -> a[1] != b[1] ? Integer.compare(b[1], a[1]) : Integer.compare(a[0], b[0]));
        }

        Arrays.fill(deleted, false);
        Arrays.fill(univNum, M);
    }

    public int calc(int id, int univ) {
        int totalScore = 0;
        for(int i=0; i<5; i++) {
            totalScore += score[i][id] * university[i][univ];
        }

        return totalScore;
    }

    public void add(int mID, int[] mScores) {
        // 신규 학생의 평가 점수 저장
        for(int i=0; i<5; i++) score[i][mID] = mScores[i];
        deleted[mID] = false;

        int targetID = mID;
        // 1번 대학부터 차례대로 밀어내기 시뮬레이션
        for(int i=0; i<univCnt; i++) {
            int curScore = calc(targetID, i);

            // i번 대학 정원이 남았다면 합격
            if(studentCnt[i] < maxSelected) {
                status[i].add(new int[]{targetID, curScore});
                univNum[targetID] = i;
                studentCnt[i]++;
                return; // 합격했으므로 종료
            }

            // Lazy Deletion: status 상단에 유효하지 않은 학생 제거
            while(!status[i].isEmpty() && (deleted[status[i].peek()[0]] || univNum[status[i].peek()[0]] != i)) {
                status[i].poll();
            }

            // 정원 초과 : 최하위 합격자와 비교
            int[] lowest = status[i].peek();
            if(curScore > lowest[1] || (curScore == lowest[1] && targetID < lowest[0])) {
                // 기존 최하위 합격자를 탈락시키고 새 학생을 합격 명단에 추가
                status[i].poll();

                ready[i].add(new int[]{lowest[0], lowest[1]});
                status[i].add(new int[]{targetID, curScore});
                univNum[targetID] = i;

                // 밀려난 학생은 바로 다음 학교에 지원
                targetID = lowest[0];
                univNum[targetID] = -1; // 임시 탈락 상태
            } else {
                // 이번 대학 지원 실패 : 다음 대학 시도
                ready[i].add(new int[]{targetID, curScore});
            }
        }

        // 모든 대학에서 탈락
        univNum[targetID] = -1;
    }

    public void erase(int mID) {
        deleted[mID] = true;
        int startUniv = univNum[mID];
        // 이미 불합격 상태면 연쇄 반응 없음
        if(startUniv == -1) return;

        // 실제 합격자 수 감소
        studentCnt[startUniv]--;
        univNum[mID] = -1;

        for(int i=startUniv; i<univCnt; i++) {
            // i번 대학의 status에서 삭제된(또는 상위 대학으로 이동한) 학생 제거 (Lazy Deletion)
            while(!status[i].isEmpty() && (deleted[status[i].peek()[0]] || univNum[status[i].peek()[0]] != i)) {
                status[i].poll();
            }

            // 빈자리가 생겼다면 ready에서 충원
            while(studentCnt[i] < maxSelected && !ready[i].isEmpty()) {
                int[] top = ready[i].poll();
                int tid = top[0];

                // 이미 삭제되었거나, 현재 대학보다 더 좋은 대학(번호가 낮은)에 다니는 경우 무시
                if(deleted[tid] || (univNum[tid] != -1 && univNum[tid] < i)) continue;

                int oldUniv = univNum[tid];
                if(oldUniv != -1) studentCnt[oldUniv]--; // 다니던 대학에 빈자리 발생

                status[i].add(new int[]{tid, calc(tid, i)});
                univNum[tid] = i;
                studentCnt[i]++;

                // 옮겨온 학생 때문에 뒷번호 대학에 빈자리가 생겼다면 그곳부터 다시 처리
                if(oldUniv != -1 && oldUniv > i) {
                    i = oldUniv-1; // for문의 i++로 인해 oldUniv부터 다시 검사하게 됨
                }
                break;
            }
        }

        univNum[mID] = -1;
    }

    public int suggest(int mID) {
        // 학생의 선발 예정 대학 번호를 즉시 출력(대학 번호는 0-based이므로 1 더해서 출력)
        // 만약 -1인 경우 경쟁에서 밀려 어떤 대학에도 선발될 수 없는 상태임. 그대로 -1 출력
        int res =  univNum[mID];
        return res == -1 ? -1 : (res + 1);
    }
}