import java.util.*;

class UserSolution
{
	// 영화 등록 순서 변수
	static int order;
	
	// 사용자 정보 배열
	static User[] userDB;
	
	// Key : 영화 ID, Value : 영화 정보
	static HashMap<Integer, Movie> movieDB;
	
	// 장르별로 (first : 영화 평점 순, second : 영화 등록 순) 정렬된 영화 정보
	static TreeSet<Movie> moviesByGenre[];
	
	// 사용자 정보 클래스
	static class User {
		Deque<Movie> watchedList = new ArrayDeque<>();
	}
	
	// 영화 정보 클래스
	static class Movie implements Comparable<Movie> {
		int id;
		int genre;
		int total;
		// 등록 순서
		int order;
		// 해당 영화를 시청한 uID 추가
		HashSet<Integer> watched = new HashSet<>();
		
		public Movie(int id, int genre, int total, int order) {
			super();
			this.id = id;
			this.genre = genre;
			this.total = total;
			this.order = order;
		}

		@Override
		public int compareTo(Movie o) {
			if(this.total == o.total)
				return Integer.compare(o.order, this.order);
			return Integer.compare(o.total, this.total);
		}
		
	} 
	
    void init(int N)
    {
    	// 초기화
    	order = 0;
    	userDB = new User[N + 1];
    	movieDB = new HashMap<>();
    	moviesByGenre = new TreeSet[6];
    	for(int i = 1; i <= 5; i++)
    		moviesByGenre[i] = new TreeSet<>();
    	
    	// 사용자 등록
    	for(int i = 1; i <= N; i++)
    		userDB[i] = new User();
    	
        return;
    }
    
    int add(int mID, int mGenre, int mTotal)
    {
    	// 같은 ID를 가진 영화가 이미 등록된 경우
    	if(movieDB.containsKey(mID))
    		return 0;
    	
    	Movie newMovie = new Movie(mID, mGenre, mTotal, order++);
    	
    	// movieDB 등록
    	movieDB.put(mID, newMovie);
    	
    	// 장르별 평점 순 movie 자료구조에 등록
    	moviesByGenre[mGenre].add(newMovie);
    	
    	return 1;
    }
    
    int erase(int mID)
    {
    	// mID인 영화가 등록된 경우가 없거나 이미 삭제된 경우
    	if(!movieDB.containsKey(mID))
    		return 0;
    	
    	// 삭제할 영화 정보
    	Movie toRemove = movieDB.get(mID);

    	// 1. genreMovie에서 삭제
    	moviesByGenre[toRemove.genre].remove(toRemove);
    	
    	// 2. movieDB 에서 제거
    	movieDB.remove(toRemove.id);
    	
    	// user class 의 watchedList에서는 지울 수가 없다.
    	// 이유 : 시청한 순서인 wOrder 값을 모르기 때문에 조회할 수 없음
    	
    	return 1;
    }

    int watch(int uID, int mID, int mRating)
    {
    	// mID인 영화가 등록된 경우가 없거나 삭제된 경우
    	if(!movieDB.containsKey(mID))
    		return 0;
    	
    	// 시청하려는 영화 정보
    	Movie toWatch = movieDB.get(mID);
    	
    	// 사용자 uID가 이미 시청한 영화인 경우
    	if(toWatch.watched.contains(uID))
    		return 0;
    	
    	// 1. toWatch 영화 정보가 변경되기 전에 genreMovie에서 삭제
    	moviesByGenre[toWatch.genre].remove(toWatch);
    	
    	// 2. 영화 정보에 시청한 사용자의 id와 평점 추가
    	toWatch.watched.add(uID);
    	toWatch.total += mRating;
    	
    	// 3. 변경된 영화 정보로 genreMovie에 추가
    	moviesByGenre[toWatch.genre].add(toWatch);
    	
    	// 4. 사용자의 시청 목록에 영화 추가
    	userDB[uID].watchedList.addFirst(toWatch);
    	
    	return 1;
    	
    }
    
    Solution.RESULT suggest(int uID)
    {
        Solution.RESULT res = new Solution.RESULT();
        
        res.cnt = 0;
        
    	// 해당 영화가 삭제되었을 수도 있으므로 movieDB를 이용해 확인이 필요하다. (asynchronous)  
        // 실제로 유효한 시청 목록 5개를 생성
        List<Movie> validMovies = new ArrayList<>();
        User toSuggest = userDB[uID];
        
        for(Movie m : toSuggest.watchedList)	{
        	// 존재하는 영화인지 확인
        	if(movieDB.containsKey(m.id))
        		validMovies.add(m);
        	
        	// 5개 되면 중단
        	if(validMovies.size() == 5)
        		break;
        }
        
        // 사용자의 시청 목록에 어떤 영화도 없는 경우
        if(validMovies.isEmpty())	{
        	// 추천 영화 후보 리스트
        	TreeSet<Movie> recommandList = new TreeSet<>();
        	
        	for(int g = 1; g <= 5; g++) {
        		// 1. 어차피 최대 5개의 영화만 추천하므로 각 장르별로 상위 5개의 영화만 후보 리스트에 추가
        		int limit = 0;
        		for(Movie m : moviesByGenre[g])	{
        			// 시청한 영화는 제외
    				if(m.watched.contains(uID))
    					continue;
    				
    				recommandList.add(m);
    				limit++;
    				
    				if(limit == 5)	break;
        		}
        	}
        	
        	// 2. 추천 영화 후보 리스트에서 최대 5개 까지만 최종 추천 목록에 추가
        	for(Movie m : recommandList)	{
        		res.IDs[res.cnt++] = m.id;
        		if(res.cnt == 5)	break;
        	}
        	
        }
        // 시청 목록이 존재하는 경우
        else	{
        	int bestGenre = 0;
        	int bestRating = -1;

        	for(Movie m : validMovies) {
        		if(m.total > bestRating) {
        			bestRating = m.total;
        			bestGenre = m.genre;
        		}
        	}
        	
        	for(Movie m : moviesByGenre[bestGenre])	{
        		// 시청한 영화는 제외
        		if(m.watched.contains(uID))
        			continue;
        		res.IDs[res.cnt++] = m.id;
        		if(res.cnt == 5)	break;
        	}
        	
        }
        
        return res;
    }
}
