import java.util.*;

class UserSolution {
	static Map<Integer, Movie> movies;
	static Set<Movie>[] genres;
	static User[] users;

	void init(int N) {
		movies = new HashMap<>();
		genres = new TreeSet[5];
		
		for (int i = 0; i < 5; i++) 
			genres[i] = new TreeSet();
		
		users = new User[N];
		for (int i = 0; i < N; i++) {
			users[i] = new User();
		}

		return;
	}

	int add(int mID, int mGenre, int mTotal) {
		if (movies.containsKey(mID)) return 0;
		
		movies.put(mID, new Movie(mID, mGenre, mTotal));
		genres[mGenre].add(new Movie(mID, mGenre, mTotal));
		
		return 1;
	}

	int erase(int mID) {
		return movies.remove(mID) == null ? 0 : 1;
	}

	int watch(int uID, int mID, int mRating) {
		return users[uID].watch(mID, mRating) ? 1 : 0;
	}

	Solution.RESULT suggest(int uID) {
		Solution.RESULT res = new Solution.RESULT();
		res.cnt = -1;
		
		User user = users[uID];
		int genreId = user.findFirst();
		
		if (genreId == -1) { // 본 게 없으
			// 일단 러프하게 모든 movie 다 돌아 ? -> 그럼 5000만번인데 / 그렇다고 TreeSet으로 관리하기에는 watch 3만번이 곤란할 것 같 
		}
		else {
			for (Movie m : genres[genreId]) {
				if (user.isWatched(m.id)) continue;
				// something
				// TOOD: 뭘 추가하
				res.IDs[++res.cnt] = m.id;
			}			
		}

		return res;
	}

	static class User {
		Set<WatchedMovie> watchedMovies;

		public User() {
			this.watchedMovies = new TreeSet<>();
		}
		
		public boolean watch(int mid, int rate) {
			Movie movie = movies.get(mid);
			
			if (movie == null || watchedMovies.contains(movie)) return false;
			
			movie.total += rate;
			watchedMovies.add(new WatchedMovie(mid, rate));
			return true;
		}
		
		public int findFirst() {
			return watchedMovies.size() > 0 ? watchedMovies.iterator().next().id : -1;
		}
		
		public boolean isWatched(int mid) {
			Iterator<WatchedMovie> iter = this.watchedMovies.iterator();
			
			WatchedMovie m;
			while (iter.hasNext()) {
				m = iter.next();
				
				if (m.id == mid) return false;
			}
			
			return true;
		}
	}

	static class Movie implements Comparable<Movie>{
		int id, genre, total;

		public Movie(int id, int genre, int total) {
			this.id = id;
			this.genre = genre;
			this.total = total;
		}
		
		@Override
		public int compareTo(Movie o) {
			return o.total - this.total;
		}
	}
	
	static class WatchedMovie implements Comparable<WatchedMovie> {
		static int totalOrder = 0;
		int id, rate, order;

		public WatchedMovie(int id, int rate) {
			this.id = id;
			this.rate = rate;
			order = ++totalOrder;
		}

		@Override
		public int compareTo(WatchedMovie o) {
			return this.rate != o.rate ? o.rate - this.rate:
				o.order - this.order;
		}
	}
}
