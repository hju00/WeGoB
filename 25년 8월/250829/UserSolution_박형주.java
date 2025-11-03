import java.util.*;

class UserSolution {
    // --- 상수 정의 ---
    static final int MAX_GENRE = 5;
    static final int MAX_MOVIES = 10000;
    static final int MAX_USERS = 1000;

    // --- 데이터 클래스 ---
    static class Movie {
        int mID, mGenre, mTotal;
        boolean removed; // 소프트 삭제 플래그
        HashSet<Integer> watchedUsers;
    }

    // TreeSet에 저장될 객체 (평점, 영화 인덱스)
    static class MovieRef implements Comparable<MovieRef> {
        int total;
        int idx;

        public MovieRef(int total, int idx) {
            this.total = total;
            this.idx = idx;
        }

        @Override
        public int compareTo(MovieRef o) {
            if (this.total != o.total) {
                // 평점 내림차순
                return Integer.compare(o.total, this.total);
            }
            // 인덱스 내림차순
            return Integer.compare(o.idx, this.idx);
        }
    }

    // 사용자 시청 기록 객체 (영화 인덱스, 사용자가 준 평점)
    static class WatchLog {
        int idx;
        int rating;

        public WatchLog(int idx, int rating) {
            this.idx = idx;
            this.rating = rating;
        }
    }

    // --- 자료구조 ---
    static Movie[] movies; // 실제 영화 데이터 저장 (조밀 배열)
    static TreeSet<MovieRef>[] genreToMovies; // 장르별 영화 정렬
    static LinkedList<WatchLog>[] userToMovie; // 사용자별 시청 기록

    // ID <-> 인덱스 변환용 Map
    static HashMap<Integer, Integer> idToIdx;
    
    static int movieIdxCounter;

    // API Functions
    void init(int N) {
        movies = new Movie[MAX_MOVIES];
        genreToMovies = new TreeSet[MAX_GENRE + 1]; // 장르 0: 전체, 1~5: 장르별
        userToMovie = new LinkedList[MAX_USERS + 1];
        
        idToIdx = new HashMap<>();
        movieIdxCounter = 0;

        for (int i = 0; i <= MAX_GENRE; i++) 
            genreToMovies[i] = new TreeSet<>();
        
        for (int i = 0; i <= MAX_USERS; i++) 
            userToMovie[i] = new LinkedList<>();
        
    }

    int add(int mID, int mGenre, int mTotal) {
        if (idToIdx.containsKey(mID)) return 0;

        int curIdx = movieIdxCounter;
        idToIdx.put(mID, curIdx);

        movies[curIdx] = new Movie();
        movies[curIdx].mID = mID;
        movies[curIdx].mGenre = mGenre;
        movies[curIdx].mTotal = mTotal;
        movies[curIdx].removed = false;
        movies[curIdx].watchedUsers = new HashSet<>();

        genreToMovies[mGenre].add(new MovieRef(mTotal, curIdx));
        genreToMovies[0].add(new MovieRef(mTotal, curIdx)); // 장르 0은 전체 영화 목록

        movieIdxCounter++;
        return 1;
    }

    int erase(int mID) {
        if (!idToIdx.containsKey(mID)) return 0;

        int curIdx = idToIdx.get(mID);
        // 이미 삭제된 영화(soft delete)도 실패 처리 가능 (문제에 따라)
        if (movies[curIdx].removed) return 0;

        movies[curIdx].removed = true;
        int curGenre = movies[curIdx].mGenre;
        int curTotal = movies[curIdx].mTotal;

        genreToMovies[curGenre].remove(new MovieRef(curTotal, curIdx));
        genreToMovies[0].remove(new MovieRef(curTotal, curIdx));

        // idToIdx에서는 삭제하지 않아야 soft delete 상태를 확인할 수 있음
        // 만약 idToIdx.remove(mID)를 하면 하드 삭제가 되어 다른 로직에 영향
        return 1;
    }

    int watch(int uID, int mID, int mRating) {
        if (!idToIdx.containsKey(mID)) return 0;

        int curIdx = idToIdx.get(mID);
        if (movies[curIdx].removed || movies[curIdx].watchedUsers.contains(uID)) return 0;

        int curGenre = movies[curIdx].mGenre;
        int curTotal = movies[curIdx].mTotal;

        // TreeSet에서 이전 상태 삭제
        genreToMovies[curGenre].remove(new MovieRef(curTotal, curIdx));
        genreToMovies[0].remove(new MovieRef(curTotal, curIdx));

        // 영화 정보 갱신
        movies[curIdx].mTotal += mRating;
        movies[curIdx].watchedUsers.add(uID);
        
        // TreeSet에 새로운 상태 추가
        genreToMovies[curGenre].add(new MovieRef(movies[curIdx].mTotal, curIdx));
        genreToMovies[0].add(new MovieRef(movies[curIdx].mTotal, curIdx));
        
        // 사용자 시청 기록 추가 (사용자가 준 평점 `mRating`을 기록)
        userToMovie[uID].addFirst(new WatchLog(curIdx, mRating));
        
        return 1;
    }

    Solution.RESULT suggest(int uID) {
        Solution.RESULT res = new Solution.RESULT();
        res.cnt = 0;
        int bestRating = 0;
        int bestGenre = 0;

        // 1. 사용자의 최근 5개 '유효한' 시청 기록을 보며 최고 평점 장르 찾기
        int count = 0;
        // Iterator를 사용해야 안전하게 삭제 가능
        Iterator<WatchLog> iter = userToMovie[uID].iterator();
        while (iter.hasNext()) {
            if (count >= 5) break;
            
            WatchLog log = iter.next();
            int movieIdx = log.idx;

            // soft delete된 영화는 시청 기록에서 삭제하고 건너뜀
            if (movies[movieIdx].removed) {
                iter.remove();
                continue;
            }

            if (bestRating < log.rating) {
                bestRating = log.rating;
                bestGenre = movies[movieIdx].mGenre;
            }
            count++;
        }

        // 2. 추천 목록 생성. 유효 시청 기록이 없으면 장르 0(전체)에서 추천
        TreeSet<MovieRef> source = (bestGenre == 0) ? genreToMovies[0] : genreToMovies[bestGenre];
        
        for (MovieRef ref : source) {
            if (res.cnt >= 5) break;

            int movieIdx = ref.idx;
            // 삭제되었거나 이미 본 영화는 건너뜀
            if (movies[movieIdx].removed || movies[movieIdx].watchedUsers.contains(uID)) {
                continue;
            }
            res.IDs[res.cnt++] = movies[movieIdx].mID;
        }

        return res;
    }
}
