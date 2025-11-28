import java.util.HashMap;
import java.util.TreeSet;

class UserSolution {

    class Vacant implements Comparable<Vacant> {
        int r;
        int c;
        int length;

        @Override
        public int compareTo(Vacant o) {
            if (this.r != o.r) return Integer.compare(this.r, o.r);
            return Integer.compare(this.c, o.c);
        }

        Vacant(int r, int c, int length) {
            this.r = r;
            this.c = c;
            this.length = length;
        }
    }

    // 빈 칸 정보 전체
    private TreeSet<Vacant> wordBook;

    // 단어 위치 정보
    private HashMap<Integer, Vacant> words;

    // 전체 행/열 크기
    private int N, M;

    private int bucketSize;   // 버킷 하나에 포함되는 행 수
    private int bucketCnt;    // 버킷 개수
    private int[] bucketMax;  // 각 버킷에서 가장 긴 길이

    public void init(int N, int M) {
        wordBook = new TreeSet<>();
        words = new HashMap<>();
        this.N = N;
        this.M = M;


        for (int i = 0; i < N; i++) {
            wordBook.add(new Vacant(i, 0, M));
        }

        bucketSize = (int) Math.sqrt(N);
        if (bucketSize <= 0) bucketSize = 1;

        bucketCnt = (N + bucketSize - 1) / bucketSize;
        bucketMax = new int[bucketCnt];

        for (int b = 0; b < bucketCnt; b++) {
            bucketMax[b] = M;
        }
    }

    // r행의 버킷 번호
    private int bucketIndex(int r) {
        return r / bucketSize;
    }

    // b번 버킷의 가장 긴 빈칸 길이 계산
    private void recomputeBucket(int b) {
        int startRow = b * bucketSize;
        int endRow = Math.min(N, startRow + bucketSize);

        int maxLen = 0;

        // startRow 이상인 첫 Vacant부터 시작
        Vacant key = new Vacant(startRow, -1, 0);
        Vacant cur = wordBook.ceiling(key);

        while (cur != null && cur.r < endRow) {
            if (cur.length > maxLen) maxLen = cur.length;
            cur = wordBook.higher(cur);
        }

        bucketMax[b] = maxLen;
    }

    // 길이 mLen 이상을 넣을 수 있는 Vacant 하나 찾아서 꺼내기
    private Vacant canWrite(int mLen) {
        
    	// 1) 버킷 단위로 먼저 거르기
        for (int b = 0; b < bucketCnt; b++) {
            if (bucketMax[b] < mLen) continue; // mLen보다 가장 긴 칸이 짧다면, 넣을 수 없음.

            int startRow = b * bucketSize;
            int endRow = Math.min(N, startRow + bucketSize);

            // 2) 이 버킷에 속하는 행들의 Vacant 중에
            Vacant key = new Vacant(startRow, -1, 0);
            Vacant cur = wordBook.ceiling(key);

            while (cur != null && cur.r < endRow) {
                if (cur.length >= mLen) {
                    // 쓸 수 있는 자리 발견 → wordBook에서 삭제하고 반환
                    wordBook.remove(cur);
                    return cur;
                }
                cur = wordBook.higher(cur);
            }            
        }

        return null;
    }

    // 단어 작성
    public int writeWord(int mId, int mLen) {
        // ID: mId, mLen: 단어의 길이
        if (mLen > M) return -1; // M보다 길면 못 넣음

        Vacant vac = canWrite(mLen); // 쓸 수 있는지 확인
        if (vac == null) return -1;

        int r = vac.r;
        int c = vac.c;
        int len = vac.length; // 빈 공간의 크기
        int remain = len - mLen;

        // 오른쪽에 남는 빈 공간 추가
        if (remain > 0) {
            wordBook.add(new Vacant(r, c + mLen, remain));
        }

        // 단어장에 추가
        words.put(mId, new Vacant(r, c, mLen));

        // 이 행 r이 속한 버킷만 다시 최대 길이 계산
        int b = bucketIndex(r);
        recomputeBucket(b);

        return r;
    }

    // 단어 삭제
    public int eraseWord(int mId) { // 지우기 전 위치의 행 번호 반환
        Vacant word = words.remove(mId); // 지우기
        if (word == null) return -1; // 없으면 -1

        int r = word.r;
        int c = word.c;
        int len = word.length;
        
        // 빈 칸 정리
        // 새로운 빈 칸: idx[r][c] ~ idx[r][c + len]
        // 앞 칸 합치기
        Vacant lower = wordBook.lower(word);
        if (lower != null && lower.r == r && lower.c + lower.length == c) {
            wordBook.remove(lower);
            c = lower.c;
            len += lower.length;
            word = new Vacant(r, c, len);
        }
        
        // 뒷 칸 합치기
        Vacant higher = wordBook.higher(word);
        if (higher != null && higher.r == r && c + len == higher.c) {
            wordBook.remove(higher);
            len += higher.length;
            word = new Vacant(r, c, len);
        }
        
        // 합쳐진 빈칸 다시 추가
        wordBook.add(word);
        
        // 이 행 r이 속한 버킷만 다시 최대 길이 계산
        int b = bucketIndex(r);
        recomputeBucket(b);

        return r;
    }
}
