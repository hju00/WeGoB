import java.util.*;

class UserSolution {

    static class Word {
        int id, start, end, len;
        public Word(int id, int start, int len) {
            this.id = id;
            this.start = start;
            this.end = start + len;
            this.len = len;
        }
    }

    static int N, M;
    static ArrayList<Word>[] wordList;      	// 각 행의 단어 리스트
    static HashMap<Integer, Integer> idToRow; 	// 단어 ID -> 행 번호 매핑 (삭제 시 빠른 접근용)
    
    static int[] rowMax;    // 각 행별 최대 빈 공간 크기
    static int[] bucketMax; // 각 버킷별 최대 빈 공간 크기
    static int BUCKET_SIZE; // 버킷 크기 (√N)

    public void init(int N, int M) {
        this.N = N;
        this.M = M;
        
        wordList = new ArrayList[N];
        for (int i = 0; i < N; i++) 
            wordList[i] = new ArrayList<>();
        
        idToRow = new HashMap<>();
        rowMax = new int[N];
        Arrays.fill(rowMax, M);

        // 버킷 초기화
        BUCKET_SIZE = (int) Math.sqrt(N);
        int bucketCount = (N + BUCKET_SIZE - 1) / BUCKET_SIZE;
        bucketMax = new int[bucketCount];
        Arrays.fill(bucketMax, M);
    }

    // 해당 행의 최대 빈 공간을 다시 계산하고, 버킷 정보도 갱신, 최대 연산 약 200
    void updateMaxGap(int row) {
        int maxGap = 0;
        int prevEnd = 0;
        
        // 단어 사이사이의 빈 공간 확인
        // 최대 60회
        for (Word word : wordList[row]) {
            maxGap = Math.max(maxGap, word.start - prevEnd);
            prevEnd = word.end;
        }
        
        // 마지막 단어와 끝 벽 사이의 공간 확인
        maxGap = Math.max(maxGap, M - prevEnd);
        
        // 행의 최대 공백 갱신
        rowMax[row] = maxGap;

        // 해당 행이 속한 버킷의 최대값 갱신
        int bIdx = row / BUCKET_SIZE;
        int bMax = 0;
        int start = bIdx * BUCKET_SIZE;
        int end = Math.min(start + BUCKET_SIZE, N);
        
        // 최대 버킷 크기 141
        for (int i = start; i < end; i++)
            bMax = Math.max(bMax, rowMax[i]);
        bucketMax[bIdx] = bMax;
    }


    // 최대 연산 약 620회 이하
    public int writeWord(int mId, int mLen) {
        int targetRow = -1;

        // 순회 연산 300회 이하
        for (int i = 0; i < bucketMax.length; i++) {
            if (bucketMax[i] >= mLen) {
                // 이 버킷 안에 가능한 행이 있음 -> 행 단위 탐색
                int start = i * BUCKET_SIZE;
                int end = Math.min(start + BUCKET_SIZE, N);
                
                for (int j = start; j < end; j++)
                    if (rowMax[j] >= mLen) {
                        targetRow = j;
                        break;
                    }
                if (targetRow != -1) break;
            }
        }

        if (targetRow == -1) return -1;

        ArrayList<Word> line = wordList[targetRow];
        int prevEnd = 0;
        int insertIdx = 0;
        int insertStart = -1;

        // 들어갈 위치 찾기 (빈 공간 계산)
        // 순회 연산 60회 이하
        for (int i = 0; i < line.size(); i++) {
            Word curr = line.get(i);
            if (curr.start - prevEnd >= mLen) {
                insertStart = prevEnd;
                insertIdx = i;
                break;
            }
            prevEnd = curr.end;
        }
        
        // 중간에 못 넣었으면 맨 뒤에 넣음
        if (insertStart == -1) {
            insertStart = prevEnd;
            insertIdx = line.size();
        }

        // 데이터 저장 및 갱신
        // ArrayList index 삽입 시간복잡도 O(N) -> 60회 이하
        line.add(insertIdx, new Word(mId, insertStart, mLen));
        idToRow.put(mId, targetRow);
        
        // 행 및 버킷 정보 갱신
        updateMaxGap(targetRow);

        return targetRow;
    }

    // 최대 연산 약 320회 이하
    public int eraseWord(int mId) {
        if (!idToRow.containsKey(mId)) return -1;

        int row = idToRow.remove(mId);
        ArrayList<Word> line = wordList[row];

        // 리스트에서 해당 ID를 가진 단어 삭제
        // 순회 연산 60회 이하
        for (int i = 0; i < line.size(); i++) {
            if (line.get(i).id == mId) {
                // ArrayList index 삭제 시간복잡도 O(N) -> 60회 이하
                line.remove(i);
                break;
            }
        }

        // 삭제했으니 빈 공간이 합쳐졌을 수 있음 -> 갱신
        updateMaxGap(row); 

        return row;
    }
}