import java.util.*;

class UserSolution {
	
	static int n;
	static HashMap<String, Integer> monarchDB;
	static int[][] soldier;
	static int[] parent;		// 동맹 관계 저장
	static boolean[][] hostile; // 적대 관계 저장
	static HashSet<Integer>[] allyList;
	static int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
	static int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
	
	static boolean isIn(int y, int x) {
		return y >= 0 && y < n && x >= 0 && x < n;
	}
	
	static int find(int x) { 
		if(x == parent[x])	return x;
		return parent[x] = find(parent[x]);
	}
	
    void init(int N, int mSoldier[][], char mMonarch[][][])
    {
    	n = N;
    	monarchDB = new HashMap<>();
    	soldier = mSoldier;
    	parent = new int[N * N];
    	hostile = new boolean[N * N][N * N];
    	allyList = new HashSet[N * N];
    	
    	for(int i = 0; i < N * N; i++) {
    		parent[i] = i;
    		allyList[i] = new HashSet<>();
    		allyList[i].add(i);
    	}
    	
    	for(int i = 0; i < N; i++) {
    		for(int j = 0; j < N; j++) {
    			String key = new String(mMonarch[i][j]);
    			monarchDB.put(key, i * N + j);
    		}
    	}
    	
    }
    
    void destroy()
    {

    }
    
    int ally(char mMonarchA[], char mMonarchB[])
    {
    	String a = new String(mMonarchA);
    	String b = new String(mMonarchB);
    	
    	int idxA = monarchDB.get(a);
    	int idxB = monarchDB.get(b);
    	
    	int rootA = find(idxA);
    	int rootB = find(idxB);
    	
		// 1. 이미 동맹 관계일 경우 -1 반환
        if(rootA == rootB) return -1;
        
        // 2. 적대 관계일 경우 -2 반환
        if(hostile[rootA][rootB] || hostile[rootB][rootA])	return -2;
        
        // 3. 동맹 체결
        parent[rootB] = rootA;
        
        // 4. B의 동맹 목록을 A의 동맹 목록으로 이전
        allyList[rootA].addAll(allyList[rootB]);
        allyList[rootB].clear();
        
        // 4. 적대 관계 병합
        for(int i = 0; i < n * n; i++) {
        	if(hostile[rootB][i]) {
        		hostile[rootA][i] = true;
        		hostile[i][rootA] = true;
        		hostile[rootB][i] = false;
                hostile[i][rootB] = false;
            }
        }
        
        return 1;
    }
    
    int attack(char mMonarchA[], char mMonarchB[], char mGeneral[])
    {
    	String a = new String(mMonarchA);
    	String b = new String(mMonarchB);
    	String g = new String(mGeneral);
    	
    	int aIdx = monarchDB.get(a);
    	int bIdx = monarchDB.get(b);
    	
    	int rootA = find(aIdx);
    	int rootB = find(bIdx);
    	
    	// 1. 군주 mMonarchA 와 군주 mMonarchB 가 동맹관계 이면 -1을 반환하고, 전투는 일어나지 않는다.
    	if(rootA == rootB)	return -1;
    	
    	// 2. 군주 mMonarchA 의 영토 또는 동맹 영토가 군주 mMonarchB 의 영토와 인접하지 않다면 -2을 반환하고, 전투는 일어나지 않는다.
    	int by = bIdx / n;
    	int bx = bIdx % n;
    	
    	int attackNum = 0;
    	int defenseNum = soldier[by][bx];
    	boolean isAdjacent = false;
    	
    	for(int d = 0; d < 8; d++) {
    		int ny = by + dy[d];
    		int nx = bx + dx[d];
    		
    		if(!isIn(ny, nx))	continue;
    		
    		int nIdx = ny * n + nx;
    		int nRoot = find(nIdx);
    		
    		if(nRoot == rootA) {
    			isAdjacent = true;
    			attackNum += soldier[ny][nx];
    		}
    		else if(nRoot == rootB) 
    			defenseNum += soldier[ny][nx];
    	}
    	
    	// 인접하지 않으면 공격 불가
    	if(!isAdjacent)	return -2;
    	
    	// 공격 성공
    	if(attackNum > defenseNum) {
    		
    		return 1;
    	}
    	// 공격 실패
    	else {
    		
    		return 0;
    	}
    }
    
    int recruit(char mMonarch[], int mNum, int mOption) {
        String tar = new String(mMonarch);
        int tarIdx = monarchDB.get(tar);
        
        // 해당 영토만
        if(mOption == 0) {
            int y = tarIdx / n;
            int x = tarIdx % n;
            soldier[y][x] += mNum;
            return soldier[y][x];
        }
        
        // 동맹 영토 전체
        int rootT = find(tarIdx);
        int totalSoldiers = 0;
        
        // 모든 영토를 순회하며
        for(int nIdx : allyList[rootT]) {
            int y = nIdx / n;
            int x = nIdx % n;
            soldier[y][x] += mNum;
            totalSoldiers += soldier[y][x];
        }
        
        return totalSoldiers;
    }
}