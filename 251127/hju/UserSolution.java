import java.util.*;

class UserSolution {

    static class Node {
        int id, start, end, len;
        public Node(int id, int start, int len) {
            this.id = id;
            this.start = start;
            this.end = start + len;
            this.len = len;
        }
    }

    static int N, M;
    static ArrayList<Node>[] lines;      		// 각 행의 단어 리스트
    static HashMap<Integer, Integer> idToRow; 	// 단어 ID -> 행 번호 매핑 (삭제 시 빠른 접근용)
    
    static int[] rowMaxGap;    // 각 행별 최대 빈 공간 크기
    static int[] bucketMaxGap; // 각 버킷별 최대 빈 공간 크기
    static int BUCKET_SIZE;    // 버킷 크기 (√N)

    public void init(int N, int M) {
        this.N = N;
        this.M = M;
        
        lines = new ArrayList[N];
        for (int i = 0; i < N; i++) 
            lines[i] = new ArrayList<>();
        
        idToRow = new HashMap<>();
        rowMaxGap = new int[N];
        Arrays.fill(rowMaxGap, M); // 처음엔 모든 행이 M만큼 비어있음

        // 버킷 초기화
        BUCKET_SIZE = (int) Math.sqrt(N);
        int bucketCount = (N + BUCKET_SIZE - 1) / BUCKET_SIZE;
        bucketMaxGap = new int[bucketCount];
        Arrays.fill(bucketMaxGap, M);
    }

    // 해당 행(row)의 최대 빈 공간을 다시 계산하고, 버킷 정보도 갱신하는 함수
    void updateMaxGap(int row) {
        int maxGap = 0;
        int prevEnd = 0;
        
        // 단어 사이사이의 빈 공간 확인
        for (Node node : lines[row]) {
            maxGap = Math.max(maxGap, node.start - prevEnd);
            prevEnd = node.end;
        }
        
        // 마지막 단어와 끝 벽 사이의 공간 확인
        maxGap = Math.max(maxGap, M - prevEnd);
        
        rowMaxGap[row] = maxGap;

        // 해당 행이 속한 버킷의 최대값 갱신
        int bIdx = row / BUCKET_SIZE;
        int bMax = 0;
        int start = bIdx * BUCKET_SIZE;
        int end = Math.min(start + BUCKET_SIZE, N);
        
        for (int i = start; i < end; i++)
            bMax = Math.max(bMax, rowMaxGap[i]);
        bucketMaxGap[bIdx] = bMax;
    }

    public int writeWord(int mId, int mLen) {
        int targetRow = -1;

        for (int i = 0; i < bucketMaxGap.length; i++) {
            if (bucketMaxGap[i] >= mLen) {
                // 이 버킷 안에 가능한 행이 있음 -> 행 단위 탐색
                int start = i * BUCKET_SIZE;
                int end = Math.min(start + BUCKET_SIZE, N);
                
                for (int j = start; j < end; j++)
                    if (rowMaxGap[j] >= mLen) {
                        targetRow = j;
                        break;
                    }
                if (targetRow != -1) break;
            }
        }

        if (targetRow == -1) return -1;

        // 2. 찾은 행에 단어 삽입
        ArrayList<Node> line = lines[targetRow];
        int prevEnd = 0;
        int insertIdx = 0; // 리스트에 삽입할 인덱스
        int insertStart = -1;

        // 들어갈 위치 찾기 (빈 공간 계산)
        for (int i = 0; i < line.size(); i++) {
            Node curr = line.get(i);
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
        line.add(insertIdx, new Node(mId, insertStart, mLen));
        idToRow.put(mId, targetRow);
        
        updateMaxGap(targetRow); // 행 및 버킷 정보 갱신

        return targetRow;
    }

    public int eraseWord(int mId) {
        if (!idToRow.containsKey(mId)) return -1;

        int row = idToRow.remove(mId);
        ArrayList<Node> line = lines[row];

        // 리스트에서 해당 ID를 가진 단어 삭제
        for (int i = 0; i < line.size(); i++) {
            if (line.get(i).id == mId) {
                line.remove(i);
                break;
            }
        }

        // 삭제했으니 빈 공간이 합쳐졌을 수 있음 -> 갱신
        updateMaxGap(row); 

        return row;
    }
}