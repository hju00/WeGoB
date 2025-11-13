package swea;

import java.util.HashMap;
import java.util.Map;

class UserSolution {
	// 나중에 unions도 enemy처럼 1차원으로 수정 필요...
	int[][] unions;
	int[] enemy;
	int N;
	int[][] mSoldier;
	int[][] delta = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
	Map<String, Integer> nameToIdx; // 이름 넣으면 어느 칸의 군주인지 알려주는 Map
    void init(int N, int mSoldier[][], char mMonarch[][][])
    {
    	
    	this.N = N;
    	this.mSoldier = mSoldier;
    	nameToIdx = new HashMap<>();
    	
    	// union init
    	unions = new int[N][N];
    	// 어차피 idx로 저장하기 때문에 2차원으로 만들지 않고 길게 저장
    	enemy = new int[N * N];
    	for(int i = 0; i < N; i++) {
    		for(int j = 0; j < N; j++) {
    			int idx = i * N + j;
    			unions[i][j] = idx; // 자기 자신이 parent
    			enemy[idx] = -1; // 적대관계 없음
    			
    			String name = charToString(mMonarch[i][j]);
    			nameToIdx.put(name, idx);
    		}
    	}
    	
    }
    private String charToString(char[] name) {
    	StringBuilder sb = new StringBuilder();
        for (char c : name) {
            if (c == '\0') break;
            sb.append(c);
        }
        return sb.toString();
	}
    
	void destroy()
    {

    }
	
    int ally(char mMonarchA[], char mMonarchB[])
    {	
    	int m1 = nameToIdx.get(charToString(mMonarchA));
    	int m2 = nameToIdx.get(charToString(mMonarchB));
    	int pMon1 = findUnionByIdx(m1);
    	int pMon2 = findUnionByIdx(m2);
    	
    	// 이미 동맹이면 return -1
    	if(pMon1 == pMon2) return -1;
    	
    	// 적대관계(자신 / 동맹원들과 적대관계)이면 return -2
    	pMon1 = findEnemyByIdx(m1);
    	pMon2 = findEnemyByIdx(m2);
    	if(pMon1 == pMon2) return -2;
    	
    	// 아니면 동맹으로 묶고 return 1
    	unions[pMon2 / N][pMon2 % N] = pMon1;
    	return 1;
    }
    

	int attack(char mMonarchA[], char mMonarchB[], char mGeneral[])
    {	
    	int pMon1 = findUnionByIdx(nameToIdx.get(charToString(mMonarchA)));
    	int pMon2 = findUnionByIdx(nameToIdx.get(charToString(mMonarchB)));
    	
    	// 동맹관계이면 전투 일어나지 않음
    	if(pMon1 == pMon2) return -1;
    	
    	// 영토 인접하지 않다면 전투 일어나지 X;
    	if(!isNear(pMon1, pMon2)) return -2;
    	
    	// 1. 인접한 모든 동맹(본인 포함): 보유한 병사의 절반을 보내 공격
    	// 보내는 병사 계산시 소수점은 버림
    	int aroot = findUnionByIdx(pMon1);
    	int ar = pMon1 / N;
    	int ac = pMon1 % N;
    	
    	int attack = mSoldier[ar][ac] / 2;
    	mSoldier[ar][ac] -= attack;
    	
    	for(int d = 0; d < 8; d++) {
    		int nr = ar + delta[d][0];
    		int nc = ac + delta[d][1];
    		
    		if(!isIn(nr, nc)) continue;
    		if(findUnionByIdx(nr, nc) == aroot) {
    			int support = mSoldier[nr][nc] / 2;
    			mSoldier[nr][nc] -= support;
    			attack += support;
    		}
    	}
    	// 2. 보유한 병사 절반을 보내 방어를 도움
    	int droot = findUnionByIdx(pMon2);
    	int dr = pMon2 / N;
    	int dc = pMon2 % N;
    	int defence = mSoldier[dr][dc];
    	for(int d = 0; d < 8; d++) {
    		int nr = dr + delta[d][0];
    		int nc = dc + delta[d][1];
    		
    		if(!isIn(nr, nc)) continue;
    		if(findUnionByIdx(nr, nc) == droot) {
    			int support = mSoldier[nr][nc] / 2;
    			mSoldier[nr][nc] -= support;
    			defence += support;
    		}
    	}
    	
    	// 3. 공격 성공시 1, 공격 실패시 0(모든 병사 사망시에도 0), 장수는 병사 수 포함 X
    	int status = attack > defence? 1 : 0;
    	
    	// 전투 발생시 적대관계(동맹 전체)
    	if(status == 1) { // 공격이 승리!
    		// 군주 mMonarchB 처형
    		nameToIdx.remove(charToString(mMonarchB));
    		// mGeneral이 새로운 군주
    		nameToIdx.put(charToString(mGeneral), pMon2);
    		// mMonarchA의 동맹에 편입, 적대 동일 -> union을 mMonarchA의 root로
    		unions[dr][dc] = findUnionByIdx(pMon1);
    	} 
    	return status;
    }
	
    private boolean isIn(int r, int c) {
		return 0 <= r && r < N && 0 <= c && c < N;
	}
	private boolean isNear(int pMon1, int pMon2) {
    	int r1 = pMon1 / N;
    	int c1 = pMon1 % N;
    	
    	int r2 = pMon2 / N;
    	int c2 = pMon2 % N;
    	
    	for(int d = 0; d < 8; d++) {
    		if(r1 + delta[d][0] == r2 && c1 + delta[d][1] == c2) return true;
    	}
		return false;
	}

	int recruit(char mMonarch[], int mNum, int mOption)
    {
        // if mOp == 0: 군주 mMonarch의 영토에 nNum명의 병사 모집
		// mMonarch의 병사 수 반환
		if(mOption == 0) {
			int idx = nameToIdx.get(charToString(mMonarch));
			int r = idx / N; 
			int c = idx % N;
			
			mSoldier[r][c] += mNum;
			return mSoldier[r][c];
		}
		// else == 1:
		// mMonarch를 포함한 모등 동맹의 영토에 각각 mNum명의 병사를 모집
		// 모든 동맹의 수 합산하여 반환
		else if(mOption == 1) {
			int sum = 0;
			int root = findUnionByIdx(nameToIdx.get(charToString(mMonarch)));
			
			for(int i = 0; i < N; i++) {
				for(int j = 0; j < N; j++) {
					if(root == findUnionByIdx(i, j)) {
						mSoldier[i][j] += mNum;
						sum += mSoldier[i][j];
					}
				}
			}
			
			return sum;
		}
		return -1;
    }
    
    private int findUnionByIdx(int idx) {
        int r = idx / N;
        int c = idx % N;

        if (unions[r][c] == idx) return idx;
        
        int root = findUnionByIdx(unions[r][c]);
        unions[r][c] = root;
        return root;
    }
    
    private int findUnionByIdx(int r, int c) {
    	int idx = r * N + c;
        if (unions[r][c] == idx) return idx;
        
        int root = findUnionByIdx(unions[r][c]);
        unions[r][c] = root;
        return root;
    }
    
    private int findEnemyByIdx(int idx) { // root끼리 연결
        int root = findUnionByIdx(idx);
      
        if (enemy[root] == -1) return -1;
        return findUnionByIdx(enemy[root]);
	}

}