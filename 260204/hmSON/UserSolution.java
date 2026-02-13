import java.util.*;

class UserSolution {

    /*
    * 다익스트라에 사용할 노드 클래스
    * 현재 방문 상태인 컴퓨터의 번호와 현재까지의 링크 이동 거리 관리
    * 정렬 : 이동 거리 오름차순
    * */
    static class Node implements Comparable<Node> {
        int id, cost;

        public Node(int id, int cost) {
            this.id = id;
            this.cost = cost;
        }

        @Override
        public int compareTo(Node o) {
            return this.cost - o.cost;
        }
    }

    /*
    * 각 컴퓨터별 공유 파일과 다운로드 파일을 분리하여 관리
    * 공유 파일은 온전한 파일이므로 번호만 관리
    * 다운로드 파일은 현재 다운로드 현황을 관리할 수 있는 객체 형태로 관리해야 함
    * */
    static class Computer {
        HashSet<Integer> shareFiles;
        HashMap<Integer, DownloadFile> downloadFiles;

        public Computer() {
            this.shareFiles = new HashSet<>();
            this.downloadFiles = new HashMap<>();
        }
    }

    /*
    * 컴퓨터 내 다운로드 파일 클래스
    * 접근 가능한 공유 파일 수, 최종 갱신 시점, 현재 파일 크기, 다운로드 완료 여부 관리
    * 중복을 방지하기 위해 동일한 번호의 공유 파일을 가진 접근 가능한 컴퓨터의 번호 목록도 관리
    * */
    static class DownloadFile {
        int nearShareFiles, updatedAt, size;
        boolean finished;
        HashSet<Integer> nearComs;

        public DownloadFile(int nearShareFiles, int updatedAt, HashSet<Integer> nearComs) {
            this.nearShareFiles = nearShareFiles;
            this.updatedAt = updatedAt;
            this.size = 0;
            this.finished = false;
            this.nearComs = nearComs;
        }
    }

    int n; // 컴퓨터 수
    HashMap<Integer, Integer> files = new HashMap<>(); // 파일별 용량 크기 관리 해시맵
    Computer[] coms; // 컴퓨터 객체 관리 배열
    List<Node>[] graph; // 컴퓨터 링크 인접 그래프
    PriorityQueue<Node> q = new PriorityQueue<>(); // 다익스트라에 사용할 우선순위 큐
    int[] dist; // 다익스트라에 사용할 최단거리 배열
    HashSet<Integer> nearComs = new HashSet<>(); // 다운로드 요청한 컴퓨터로부터 접근 가능한 컴퓨터 목록

    // 하단의 두 해시맵은 addLink() 메서드에서 특정 경로를 반드시 통과하는 경로를 찾기 위해 사용
    HashMap<Integer, Integer> comsFromA = new HashMap<>(); // 컴퓨터 A로부터 접근 가능한 컴퓨터 목록
    HashMap<Integer, Integer> comsFromB = new HashMap<>(); // 컴퓨터 B로부터 접근 가능한 컴퓨터 목록

    /**
     * 초기화 메서드. 초기 1회 호출
     * 컴퓨터 번호는 1~N까지이나, 해당 풀이에서는 0-based로 변환하여 관리한다.
     * @param N 컴퓨터 수
     * @param mShareFileCnt 각 컴퓨터별 공유 파일 수
     * @param mFileID 각 컴퓨터별 공유 파일 번호
     * @param mFileSize 각 컴퓨터별 공유 파일 용량
     */
    void init(int N, int mShareFileCnt[], int mFileID[][], int mFileSize[][]) {
        n = N;
        files.clear();
        coms = new Computer[N];
        graph = new ArrayList[N];
        dist = new int[N];

        for(int i=0; i<N; i++) {
            coms[i] = new Computer();
            graph[i] = new ArrayList<>();

            for(int j=0; j<mShareFileCnt[i]; j++) {
                int fileID = mFileID[i][j];
                coms[i].shareFiles.add(fileID);
                files.put(fileID, mFileSize[i][j]);
            }
        }
    }

    /**
     * 링크 그래프 초기화 메서드. init() 호출 직후 1회 호출
     * @param K 링크 수
     * @param mComA,mComB 링크를 이루는 두 컴퓨터 번호
     * @param mDis 링크 거리
     */
    void makeNet(int K, int mComA[], int mComB[], int mDis[]) {
        for(int i=0; i<K; i++) {
            int comA = mComA[i] - 1, comB = mComB[i] - 1;
            graph[comA].add(new Node(comB, mDis[i]));
            graph[comB].add(new Node(comA, mDis[i]));
        }
    }

    /**
     * 파일 다운로드 지연 갱신 메서드. 다운로드 정보 갱신시마다 호출
     * @param time 현재 시각
     * @param target 다운로드 요청한 파일 객체
     * @param fileID 다운로드 요청한 파일 번호
     */
    void update(int time, DownloadFile target, int fileID) {
        // 이미 다운로드가 완료된 경우 무시
        if(target.finished) return;

        // 경과 시간 = 현재 시각 - 최종 갱신 시각
        int elapsedTime = time - target.updatedAt;
        // 다운로드 용량 변화 = 9 * 접근 가능한 공유 파일 수 * 경과 시간
        target.size += 9 * target.nearShareFiles * elapsedTime;
        // 최종 갱신 시각을 현재 시각으로 갱신
        target.updatedAt = time;

        // 현재 파일 크기가 파일별 용량 이상인 경우 최대값으로 보정하고 다운로드 완료 처리
        int totalSize = files.get(fileID);
        if(target.size >= totalSize) {
            target.size = totalSize;
            target.finished = true;
        }
    }

    /**
     * 링크 추가 메서드. 최대 750회.
     * 이로 인해 주변 컴퓨터들에서 새 공유 파일에 접근 가능하게 되면 즉시 다운로드에 적용한다.
     * @param mTime 현재 시각
     * @param mComA,mComB 링크를 이루는 두 컴퓨터 번호
     * @param mDis 링크 거리
     */
    void addLink(int mTime, int mComA, int mComB, int mDis) {
        // 그래프에 양방향 링크 추가
        int comNumA = mComA - 1, comNumB = mComB - 1;
        graph[comNumA].add(new Node(comNumB, mDis));
        graph[comNumB].add(new Node(comNumA, mDis));

        // du + mDis + dv 방식으로 전개
        // 두 컴퓨터를 기준으로 거리가 5000 - mDis인 컴퓨터들을 찾은 뒤
        // 각각의 경우마다 다운로드 요청 중인 파일이 있는지 확인
        comsFromA.clear();
        comsFromA.put(comNumA, 0);
        q.clear();
        q.add(new Node(comNumA, 0));
        Arrays.fill(dist, 5001 - mDis);
        dist[comNumA] = 0;
        dist[comNumB] = mDis;

        while(!q.isEmpty()) {
            Node cur = q.poll();
            if(cur.cost > dist[cur.id]) continue;

            for(Node next : graph[cur.id]) {
                int newCost = cur.cost + next.cost;
                if(newCost >= dist[next.id]) continue;

                comsFromA.put(next.id, newCost);
                dist[next.id] = newCost;
                q.add(new Node(next.id, newCost));
            }
        }

        comsFromB.clear();
        comsFromB.put(comNumB, 0);
        q.clear();
        q.add(new Node(comNumB, 0));
        Arrays.fill(dist, 5001 - mDis);
        dist[comNumB] = 0;
        dist[comNumA] = mDis;

        while(!q.isEmpty()) {
            Node cur = q.poll();
            if(cur.cost > dist[cur.id]) continue;

            for(Node next : graph[cur.id]) {
                int newCost = cur.cost + next.cost;
                if(newCost >= dist[next.id]) continue;

                comsFromB.put(next.id, newCost);
                dist[next.id] = newCost;
                q.add(new Node(next.id, newCost));
            }
        }

        // du + mDis + dv <= 5000인지 확인
        // 위 조건을 달성한 경우, 각각 다운로드 요청 상태인 파일이 상대 컴퓨터 공유 파일 목록에 존재하는 지 확인
        for(int leftIdx : comsFromA.keySet()) {
            for(int rightIdx : comsFromB.keySet()) {
                // 사이클 무시
                if (leftIdx == rightIdx) continue;

                // 새 링크를 반드시 통과하는 두 컴퓨터간 경로가 거리 5000 이하인지 확인. 초과하는 경우 무시
                int leftDist = comsFromA.get(leftIdx);
                int rightDist = comsFromB.get(rightIdx);
                if (leftDist + mDis + rightDist > 5000) continue;

                // 양쪽 컴퓨터 모두 다운로드 중인 파일 목록을 순환하며 상대 컴퓨터에 동일한 번호의 공유 파일이 존재하는 지 확인
                // 존재하면 접근 가능한 컴퓨터 목록에 등록
                // 다운로드에 활용되는 공유 파일 수가 변한 상태이므로 다운로드 현황을 현재 시점으로 갱신
                for(int leftFileID : coms[leftIdx].downloadFiles.keySet()) {
                    DownloadFile leftFile = coms[leftIdx].downloadFiles.get(leftFileID);
                    if(leftFile.finished || leftFile.nearComs.contains(rightIdx)) continue;

                    if(coms[rightIdx].shareFiles.contains(leftFileID)) {
                        update(mTime, leftFile, leftFileID);
                        leftFile.nearShareFiles++;
                        leftFile.nearComs.add(rightIdx);
                    }
                }

                for(int rightFileID : coms[rightIdx].downloadFiles.keySet()) {
                    DownloadFile rightFile = coms[rightIdx].downloadFiles.get(rightFileID);
                    if(rightFile.finished || rightFile.nearComs.contains(leftIdx)) continue;

                    if(coms[leftIdx].shareFiles.contains(rightFileID)) {
                        update(mTime, rightFile, rightFileID);
                        rightFile.nearShareFiles++;
                        rightFile.nearComs.add(leftIdx);
                    }
                }
            }
        }
    }

    /**
     * 공유 파일 추가 메서드. 최대 1500회
     * 해당 공유 파일과 동일한 번호의 파일을 다운로드 중인 접근 가능한 컴퓨터는 즉시 해당 공유 파일을 목록에 추가한다.
     * @param mTime 현재 시각
     * @param mComA 공유 파일을 추가하려는 컴퓨터 번호
     * @param mFileID 공유 파일 번호
     * @param mSize 귱유 파일 크기
     */
    void addShareFile(int mTime, int mComA, int mFileID, int mSize) {
        int comNum = mComA - 1;
        Computer com = coms[comNum];
        // 현재 컴퓨터에 공유 파일 추가
        com.shareFiles.add(mFileID);
        files.put(mFileID, mSize);

        // 접근 가능한 컴퓨터들 중 mFileID 파일을 다운로드 받는 중인 컴퓨터는 해당 파일도 함께 다운로드 받음.
        q.clear();
        q.add(new Node(comNum, 0));
        Arrays.fill(dist, 5001);
        dist[comNum] = 0;

        while(!q.isEmpty()) {
            Node cur = q.poll();
            if(cur.cost > dist[cur.id]) continue;

            for(Node next : graph[cur.id]) {
                int newCost = cur.cost + next.cost;
                if(newCost >= dist[next.id]) continue;

                // 해당 컴퓨터가 mFileID번 파일 다운로드를 요청한 상태라면 해당 파일을 함께 다운로드
                if(coms[next.id].downloadFiles.containsKey(mFileID)) {
                    DownloadFile file = coms[next.id].downloadFiles.get(mFileID);
                    update(mTime, file, mFileID);
                    file.nearShareFiles++;
                    file.nearComs.add(comNum);
                }

                dist[next.id] = newCost;
                q.add(new Node(next.id, newCost));
            }
        }
    }

    /**
     * 파일 다운로드 메서드. 최대 1500회.
     * 접근 가능한 모든 컴퓨터를 탐색하여 번호가 mFileID인 공유 파일이 몇 개인지 찾고 다운로드를 요청한다.
     * 다운로드 가능한 공유 파일이 없더라도 다운로드를 요청한 상태로 두어야 한다.
     * 해당 방식으로 추가된 파일은 다운로드가 완료되더라도 공유 파일 목록에 등록되지 않는다.
     * @param mTime 현재 시각
     * @param mComA 다운로드를 요청한 컴퓨터 번호
     * @param mFileID 다운로드하려는 파일 번호
     * @return 접근 가능한 공유 파일 수. 없으면 0 반환
     */
    int downloadFile(int mTime, int mComA, int mFileID) {
        int comNum = mComA - 1;
        Computer com = coms[comNum];

        // 다익스트라로 접근 가능한 모든 컴퓨터 탐색
        q.clear();
        q.add(new Node(comNum, 0));
        Arrays.fill(dist, 5001);
        dist[comNum] = 0;

        // 접근 가능한 공유 파일 수
        int shareCnt = 0;
        // 접근 가능한 공유 파일을 가진 컴퓨터 목록
        nearComs = new HashSet<>();
        while(!q.isEmpty()) {
            Node cur = q.poll();
            if(cur.cost > dist[cur.id]) continue;

            // 번호가 mFileID인 공유 파일이 존재하면 파일 개수 카운트
            if(coms[cur.id].shareFiles.contains(mFileID)) {
                shareCnt++;
                nearComs.add(cur.id);
            }

            for(Node next : graph[cur.id]) {
                int newCost = cur.cost + next.cost;
                if(newCost >= dist[next.id]) continue;

                dist[next.id] = newCost;
                q.add(new Node(next.id, newCost));
            }
        }

        com.downloadFiles.put(mFileID, new DownloadFile(shareCnt, mTime, nearComs));
        return shareCnt;
    }

    /**
     * 파일 크기 조회 메서드. 최대 1500회
     * 해당 파일이 공유 파일인지, 다운로드 파일인지는 직접 확인해야 한다.
     * 공유 파일이면 즉시 files에서 파일 크기 반환.
     * 다운로드 파일이면 현재까지의 다운로드를 진행한 뒤 현재 파일 크기 반환.
     * @param mTime 현재 시각
     * @param mComA 다운로드를 요청한 컴퓨터 번호
     * @param mFileID 파일 번호
     * @return 파일 크기. 파일 다운로드를 요청하지 않았거나, 다운로드 용량이 0이면 0 반환
     */
    int getFileSize(int mTime, int mComA, int mFileID) {
        Computer com = coms[mComA - 1];
        if(com.shareFiles.contains(mFileID)) return files.get(mFileID);

        DownloadFile target = com.downloadFiles.get(mFileID);
        // 파일 다운로드 요청 기록이 없으면 0 반환
        if(target == null) return 0;
        // 파일 다운로드가 이미 완료된 상태면 파일 크기 그대로 반환
        if(target.finished) return target.size;

        // 현재 시점까지의 다운로드 정보 갱신
        update(mTime, target, mFileID);

        // 현재까지의 다운로드 총량 반환. 아직 0이면 그대로 0 반환
        return target.size;
    }
}