import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class UserSolution {

    static class Word {
        int id;
        int left;
        int right;
        int len;

        public Word(int id, int left, int right, int len) {
            this.id = id;
            this.left = left;
            this.right = right;
            this.len = len;
        }

        @Override
        public String toString() {
            return "Word [id=" + id + ", left=" + left + ", right=" + right + ", len=" + len + "]";
        }
    }

    static class Line {
        int rowIdx;
        int maxBlank;
        ArrayList<Word> words;

        public Line(int rowIdx, int M) {
            this.rowIdx = rowIdx;
            this.maxBlank = M; // 초기값
            this.words = new ArrayList<>();
        }
    }

    static class Bucket {
        int idx;
        int startRow;
        int endRow;
        int maxBlank;

        public Bucket(int idx, int startRow, int endRow) {
            this.idx = idx;
            this.startRow = startRow;
            this.endRow = endRow;
            this.maxBlank = M;
        }
    }

    static int N, M;
    static int bucketSize;
    static Line[] lines;
    static Bucket[] buckets;
    static Map<Integer, Integer> mIdToRowMap;

    public void init(int N, int M) {
        this.N = N;
        this.M = M;
        this.mIdToRowMap = new HashMap<>();

        // Line 초기화
        lines = new Line[N];
        for (int i = 0; i < N; i++) {
            lines[i] = new Line(i, M);
        }

        // Bucket 초기화
        bucketSize = (int) Math.sqrt(N);

        int bucketCnt = (N + bucketSize - 1) / bucketSize; // 그냥 나누면 소수점이 버려져서 버킷 1개가 부족할 수 있다. 나눗셈 올림
        buckets = new Bucket[bucketCnt];

        for (int i = 0; i < bucketCnt; i++) {
            int start = i * bucketSize; // 이번 버킷 시작점
            int end = Math.min(start + bucketSize - 1, N-1); // 끝 버킷의 경우 N-1이 end임.

            buckets[i] = new Bucket(i, start, end);
        }

    }

    public int writeWord(int mId, int mLen) {
        // 1. 버킷 단위 탐색(루트 N만큼 순회함)
        for(Bucket bucket : buckets) {
            // 단어 길이가 더 길면 skip
            if(bucket.maxBlank < mLen) continue;

            // 2. 버킷 내부 탐색
            for(int r = bucket.startRow; r <= bucket.endRow; r++) {
                Line line = lines[r];

                if(line.maxBlank >= mLen) {
                    // 라인에서 넣을 수 있는 곳에 삽입
                    int insertIdx = -1; // words에서 몇번째 idx인지
                    int insertLeft = -1;
                    int prevRight = 0;

                    // 각 라인 순회
                    for(int i = 0; i < line.words.size(); i++) {
                        Word w = line.words.get(i);
                        if(w.left - prevRight >= mLen) {
                            insertLeft = prevRight;
                            insertIdx = i;
                            break;
                        }
                        prevRight = w.right; // 맨 끝쪽에 넣기 위한 prevRight 세팅
                    }

                    // 사이에는 들어갈 공간이 없다. 맨 뒤에 넣기
                    if(insertIdx == -1) {
                        insertLeft = prevRight;
                        insertIdx = line.words.size();
                    }

                    // 성공했으니 Map에 기록, 해당 라인과 버킷의 MaxBlank 길이 갱신!
                    line.words.add(insertIdx, new Word(mId, insertLeft, insertLeft + mLen, mLen));
                    mIdToRowMap.put(mId, r);

                    // 라인 maxBlank 갱신
                    int lMax = 0;
                    int lPrev = 0;
                    for(Word w : line.words) {
                        lMax = Math.max(lMax, w.left - lPrev);
                        lPrev = w.right;
                    }

                    // 남은 빈공간 max도 확인
                    lMax = Math.max(lMax, M - lPrev);
                    line.maxBlank = lMax;

                    // 버킷의 maxBlank 갱신
                    int bMax = 0;
                    for(int i = bucket.startRow; i <= bucket.endRow; i++) {
                        bMax = Math.max(bMax, lines[i].maxBlank);
                    }
                    bucket.maxBlank = bMax;

                    return r;
                }
            }
        }
        return -1;
    }

    public int eraseWord(int mId) {
        // Map에서 지울 Word가 들어있는 line번호 가져오기
        if(!mIdToRowMap.containsKey(mId)) return -1;

        int rowIdx = mIdToRowMap.remove(mId);

        // 해당 라인에서 Word 지우기
        Line line = lines[rowIdx];

        // 한 줄에 최대 60개의 단어
        for(int i = 0; i < line.words.size(); i++) {
            if(line.words.get(i).id == mId) {
                line.words.remove(i);
                break;
            }
        }

        // 3. 상태 갱신

        // 라인 maxBlank 갱신
        int lMax = 0;
        int lPrev = 0;
        for(Word w : line.words) {
            lMax = Math.max(lMax, w.left - lPrev);
            lPrev = w.right;
        }

        // 남은 빈공간 max도 확인
        lMax = Math.max(lMax, M - lPrev);
        line.maxBlank = lMax;

        // 버킷 maxBlank 갱신
        int bucketIdx = rowIdx / bucketSize; // 해당 버킷 idx구하기
        Bucket bucket = buckets[bucketIdx];

        int bMax = 0;
        for(int i = bucket.startRow; i <= bucket.endRow; i++) {
            bMax = Math.max(bMax, lines[i].maxBlank);
        }
        bucket.maxBlank = bMax;

        return rowIdx;
    }

}