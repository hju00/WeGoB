import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

class UserSolution {
	private TreeSet<int[]> land; // 빈 땅
	private Map<Integer, Integer> startToEnd, endToStart; // 빈 공간의 시작점, 끝점을 저장하는 리스트
	private Map<Integer, int[]> buildings; // 건설한 빌딩들
	
	public void init(int N){
		buildings = new HashMap<>();
		startToEnd = new HashMap<>();
		endToStart = new HashMap<>();
		
		land = new TreeSet<>(new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int len1 = o1[1] - o1[0] + 1;
				int len2 = o2[1] - o2[0] + 1;
				
				if(len1 == len2) return Integer.compare(o1[0], o2[0]);
				else return Integer.compare(len2, len1);
			}
		});
		
		// 처음 빈 공간: 전체
		land.add(new int[] {0, N - 1});
		startToEnd.put(0, N - 1);
		endToStart.put(N - 1, 0);
	}

	public int build(int mLength) {
		if(land.isEmpty()) return -1;
		
		// 빈 공간의 길이가 가장 긴 땅에 건설
		int[] longest = findLongest();
	
		if(longest == null) return -1;
		
		int len = longest[1] - longest[0] + 1; // 구간 길이
		if(len < mLength) { // 못 넣으면
			land.add(longest); // 지금 구간 다시 pq에 넣어주고
			return -1; // -1 return
		}
		
		// map에서 가장 긴 구간 삭제(건물 세울 거니까 기존 구간 삭제 필요)
		startToEnd.remove(longest[0]);
		endToStart.remove(longest[1]);
		
		// 빌딩의 시작점, 끝점
		int start = longest[0] + (len - mLength) / 2;
	    int end = start + mLength - 1;
		
		// new 빈공간: longest[0] ~ start, end ~ longest[1] -> 빈 공간이 남는 경우에만 넣어주기
	    if (longest[0] <= start - 1) {
	        land.add(new int[] {longest[0], start - 1});
	        startToEnd.put(longest[0], start - 1);
	        endToStart.put(start-1, longest[0]);
	    }
	    
	    if (longest[1] >= end + 1) {
	        land.add(new int[] {end + 1, longest[1]});
	        startToEnd.put(end + 1, longest[1]);
	        endToStart.put(longest[1], end + 1);
	    }
	    
	    // 지은 건물 추가
	    buildings.put(start, new int[] {start, end});	    
	    
		return start; // 건물 시작점 return
	}

	private int[] findLongest() {
		while(!land.isEmpty()) {
			int[] cur = land.pollFirst(); // 가장 긴 구간(start, end): 살았는지 죽었는지 모름
			Integer end = startToEnd.get(cur[0]); // 살아있는 구간(startAlive, endAlive)
			
			if(end != null && end == cur[1]) return cur; // null이면 합쳐진 구간, endAlive가 cur[1]이랑 달라도 있는 구간 아님
		}
		return null;
	}

	public int demolish(int mAddr) {
		int[] building = buildings.get(mAddr);
		
		// 못 찾았으면 return -1;
		if(building == null) return -1;
		
		// 시작, 끝 확인하고 삭제
		int start = building[0];
		int end = building[1];
		
		buildings.remove(mAddr);
		
		makeClear(start, end);
		
		return end - start + 1; // 부순 길이 return
	}

	private void makeClear(int start, int end) {
		// 새 빈 구간: start ~ end
		// cur[1] == start - 1
		// cur[0] == end + 1
		int left = start;
		int right = end;
		
		Integer before = endToStart.get(start - 1); // 전: end가 start - 1인 부분
		Integer after = startToEnd.get(end + 1); // 후: start가 end + 1인 부분
		
		if(before != null) {
			startToEnd.remove(before);
	        endToStart.remove(start - 1);
	        
	        left = before;
		}
		if(after != null) {
			startToEnd.remove(end + 1);
	        endToStart.remove(after);
	        
	        right = after;
		}
		
		startToEnd.put(left, right);
	    endToStart.put(right, left);
	    
	    // 새로운 합쳐진 구간 추가
	    // 이전 구간은 빌딩 건설할 때 확인하기 때문에 삭제하지 않아도 됨
	    land.add(new int[] {left, right});
	}
}
