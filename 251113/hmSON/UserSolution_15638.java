import java.util.*;
class UserSolution_15638 {
	
	static int n; 
	static int[][] soldiers;
	static Monarch[][] board;
	static HashMap<String, Monarch> monarchs;
	static int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1}, dx = {0, 1, 1, 1, 0, -1, -1, -1};
	
	static class Monarch {
		int y, x;
		Monarch head;
		HashSet<Monarch> alliance;
		HashSet<Monarch> enemies;
		boolean executed;
		
		public Monarch(int y, int x) {
			this.y = y;
			this.x = x;
			this.head = this;
			this.alliance = new HashSet<>();
			this.alliance.add(this);
			this.enemies = new HashSet<>();
			this.executed = false;
		}
	}
	
    void init(int N, int mSoldier[][], char mMonarch[][][])
    {
    	n = N;
    	
    	soldiers = new int[N][N];
    	board = new Monarch[N][N];
    	monarchs = new HashMap<>();
    	for(int i=0; i<N; i++) {
    		for(int j=0; j<N; j++) {
    			Monarch monarch = new Monarch(i, j);
    			soldiers[i][j] = mSoldier[i][j];
    			board[i][j] = monarch;
    			monarchs.put(String.valueOf(mMonarch[i][j]), monarch);
    		}
    	}
    }
    
    void destroy() {}
    
    Monarch find(Monarch mon) {
    	if(mon.head == mon) return mon;
    	return mon.head = find(mon.head);
    }
    
    void union(Monarch ma, Monarch mb) {
    	// 두 군주의 동맹 정보를 관리하는 맹주를 찾음
    	Monarch ha = find(ma);
    	Monarch hb = find(mb);
    	
    	// 가급적 동맹의 크기가 더 큰 쪽으로 병합하고자 함
    	if (ha.alliance.size() < hb.alliance.size()) {
            Monarch tmp = ha;
            ha = hb;
            hb = tmp;
        }
    	hb.head = ha;
    	
    	// 맹주가 된 군주 A가 다른 군주 B의 동맹 관계를 통합하여 관리
    	// 그에 따라 군주 B는 관리 의무를 제거
    	ha.alliance.addAll(hb.alliance);
    	hb.alliance = null;
    	
    	// 군주 B의 적대관계가 군주 A의 적대관계에 추가됨
    	// 그에 따라 적국 또한 군주 A를 적대관계로 등록
    	// HashSet이므로 중복은 무시됨
    	for(Monarch enemy : hb.enemies) {
    		ha.enemies.add(enemy);
    		enemy.enemies.remove(hb);
    		enemy.enemies.add(ha);
    	}
    	hb.enemies.clear();
    }
    
    int ally(char mMonarchA[], char mMonarchB[])
    {
    	// 두 군주의 동맹 체결 여부를 반환
    	// 1 : 동맹 체결
    	// -1 : 이미 동맹인 경우
    	// -2 : 각 동맹 사이에 적대 관계가 존재하는 경우
    	Monarch ma = monarchs.get(String.valueOf(mMonarchA));
    	Monarch mb = monarchs.get(String.valueOf(mMonarchB));
    	
    	// 두 동맹의 맹주가 동맹 관계인지 확인
    	// 동맹인 경우 -> -1 반환
    	Monarch ha = find(ma);
    	Monarch hb = find(mb);
    	if(ha == hb) return -1;
    	
    	// 두 맹주가 서로 적대관계인 경우 -> -2 반환
    	if(ha.enemies.contains(hb) || hb.enemies.contains(ha)) return -2;
    	
    	// 동맹 체결 -> 1 반환
    	union(ha, hb);
    	return 1;
    }
    
    int attack(char mMonarchA[], char mMonarchB[], char mGeneral[])
    {
    	// 1. 동맹 관계 확인
    	// 두 군주가 동맹인 경우 전투 X: -1 출력
    	Monarch ma = monarchs.get(String.valueOf(mMonarchA));
    	Monarch mb = monarchs.get(String.valueOf(mMonarchB));
    	
    	Monarch ha = find(ma);
    	Monarch hb = find(mb);
    	if(ha == hb) return -1;
    	
    	// 2. 전투 준비
    	// 군주 B의 지역의 인접 지역 중 군주 A의 동맹국이 존재하는 지 확인
    	int y = mb.y, x = mb.x;
    	boolean nearFlag = false;
    	for(int i=0; i<8; i++) {
    		int ny = y + dy[i];
    		int nx = x + dx[i];
    		if(ny < 0 || nx < 0 || ny >= n || nx >= n) continue;
    		
    		Monarch near = find(board[ny][nx]);
    		if(near == ha) {
    			nearFlag = true;
    			break;
    		}
    	}
    	
    	// 군주 A 측에서 참여한 병사가 없음 -> 군주 B의 지역과 인접한 동맹 구역이 없음
    	// 공격측 참전 병사가 없으므로 전투 X: -2 출력
    	if(!nearFlag) return -2;
    	
    	// 전투에 참여하지 않고 인접 지역에 대기하는 병사 수를 업데이트
    	// 피공격 지역에 인접한 지역 병사들만 전쟁에 참여할 수 있음
    	// 공격 또는 방어 지역과 동맹이 아닌 제 3국은 참여할 수 없음
    	// 방어 지역은 모든 병사가 전쟁에 참여, 그 외 지역은 병사의 절반이 참여(소수점은 버림 -> 현재 병사 15명인 경우 8명 대기, 7명 참전)
    	int cntA = 0, cntB = soldiers[y][x];
    	for(int i=0; i<8; i++) {
    		int ny = y + dy[i];
    		int nx = x + dx[i];
    		if(ny < 0 || nx < 0 || ny >= n || nx >= n) continue;
    		
    		Monarch near = find(board[ny][nx]);
    		if(near.equals(ha)) {
    			cntA += soldiers[ny][nx] / 2;
    			soldiers[ny][nx] -= (soldiers[ny][nx] / 2);
    		} else if(near.equals(hb)) {
    			cntB += soldiers[ny][nx] / 2;
    			soldiers[ny][nx] -= (soldiers[ny][nx] / 2);
    		}
    	}
    	
    	// 3. 전투 시작
    	// 두 군주 및 동맹국은 적대 관계가 됨
    	addEnemy(ha, hb);
    	
    	// 4. 전투 종료
    	// 공격측 승리
    	// 군주 B는 처형, 군주 A의 장수가 새 군주가 되며 군주 A의 동맹 및 적대 관계를 그대로 계승. 병사 수는 남은 병사의 수
    	if(cntA > cntB) {
    		soldiers[y][x] = cntA - cntB;
    		String general = String.valueOf(mGeneral);
    		Monarch newMon = new Monarch(y, x);
    		
    		newMon.head = ha;
    		ha.alliance.add(newMon);
    		monarchs.put(general, newMon);
    		board[y][x] = newMon;
    		
    		// 군주 B에 대한 정보를 지연 업데이트로라도 처리해야 할 필요가 있는가?
    		// 병사 모집시 동맹국 군주 중 사망한 경우가 존재할 것으로 판단됨
    		// 따라서 군주 객체에 플래그를 추가하여 처형 여부를 판단
    		mb.executed = true;
    		
    		return 1;
    	}
    	
    	// 방어측 승리 또는 전멸
    	// 현 군주 유지. 병사 수는 남은 병사의 수
    	else {
    		soldiers[y][x] = cntB - cntA; 
            return 0;
    	}
    }
    
    void addEnemy(Monarch ma, Monarch mb) {
    	Monarch ha = find(ma);
    	Monarch hb = find(mb);
    	if(ha == hb) return;
    	
    	ha.enemies.add(hb);
    	hb.enemies.add(ha);
    }
    
    int recruit(char mMonarch[], int mNum, int mOption)
    {
    	Monarch m = monarchs.get(String.valueOf(mMonarch));
    	// 옵션 0 : 해당 군주의 지역 병사 수만 추가, mNum 반환
    	if(mOption == 0) {
    		soldiers[m.y][m.x] += mNum;
    		return soldiers[m.y][m.x];
    	}
    	
    	// 옵션 1 : 해당 군주의 동맹국 전체가 각각 병사 수 추가, mNum * 동맹국 수 반환
    	Monarch head = find(m);
    	int total = 0;
    	for(Monarch ally : head.alliance) {
    		if(ally.executed) continue;
    		soldiers[ally.y][ally.x] += mNum;
    		total += soldiers[ally.y][ally.x];
    	}
        return total;
    }
}