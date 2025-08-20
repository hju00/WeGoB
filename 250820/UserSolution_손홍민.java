public class UserSolution { 
	
	Team[] teams;
	Soldier[] soldiers;
	
	static class Team {
		// 1~5점을 가진 병사들의 배열을 연결리스트로 표현
		Soldier[] heads = new Soldier[6];
		Soldier[] tails = new Soldier[6];
	}
	
	// 병사 클래스
	static class Soldier {
		int id;
		int team;
		Soldier prev;
		Soldier next;
		
		public Soldier(int id, int team) {
			this.id = id;
			this.team = team;
		}
	}
	
	void init() {
		// 이 시점에 팀 배열 객체 생성
		// team[]
		// 그리고 각 팀별로 병사들을 연결리스트로 관리할 것
		// 다만, 평판 업데이트의 편의를 위해 평판 점수에 따른 버킷 배열을 추가 생성
		teams = new Team[6];
		for(int i=1; i<=5; i++) {
			teams[i] = new Team();
			for(int j=1; j<=5; j++) {
				teams[i].heads[j] = new Soldier(-1, i);
				teams[i].tails[j] = new Soldier(-1, i);
				teams[i].heads[j].next = teams[i].tails[j];
				teams[i].tails[j].prev = teams[i].heads[j];
			}
		}
		
		// 병사 해고 및 병사 개인의 평점 업데이트를 빠르게 하기 위한 인덱스 추적용 배열
		soldiers = new Soldier[100001];
	}
	
	void hire(int mID, int mTeam, int mScore) {
		// 병사 정보를 받아서, mTeam 내 mScore쪽에 병사 추가
		// 최대 1 * 100_000 = 100_000
		Team team = teams[mTeam];
		Soldier soldier = new Soldier(mID, mTeam);
		
		team.tails[mScore].prev.next = soldier;
		soldier.prev = team.tails[mScore].prev;
		team.tails[mScore].prev = soldier;
		soldier.next = team.tails[mScore];
		soldiers[mID] = soldier;
		
	}
	
	void fire(int mID) {
		// id만으로 병사를 즉시 찾아서 제거
		// 최대 1 * 100_000 = 100_000
		Soldier target = soldiers[mID];
		target.prev.next = target.next;
		target.next.prev = target.prev;
		target.prev = null;
		target.next = null;
		soldiers[mID] = null;
	}
	
	void updateSoldier(int mID, int mScore) {
		// 대상 병사를 찾아 스코어를 먼저 바꿔주고,
		Soldier target = soldiers[mID];
		target.prev.next = target.next;
		target.next.prev = target.prev;
		
		// 그 팀의 score에 해당하는 리스트를 찾아서 해당 병사를 이전시킨다
		Team team = teams[target.team];
		team.tails[mScore].prev.next = target;
		target.prev = team.tails[mScore].prev;
		team.tails[mScore].prev = target;
		target.next = team.tails[mScore];
		
		// 최대 1 * 100_000 = 100_000
	}
	
	void updateTeam(int mTeam, int mChangeScore) {
		if(mChangeScore == 0) return;
		
		// 연결리스트를 이용해 동점수대의 병사 평점을 한꺼번에 업데이트
		// 최대 4 * 100_000 = 400_000
		Team team = teams[mTeam];
		if(mChangeScore > 0) {
			// 추가할 평점이 양수일 때
			for(int i=4; i>=1; i--) {
				// 아무것도 없는 리스트는 무시
				if(team.heads[i].next == team.tails[i]) continue;
				// 평점 변화 후 점수 계산
				int finalScore = i + mChangeScore;
				if(finalScore > 5) finalScore = 5;
				// 리스트 전체를 목적 버킷의 헤더와 첫 병사 사이로 연결
				team.heads[i].next.prev = team.heads[finalScore];
				team.tails[i].prev.next = team.heads[finalScore].next;
				team.heads[finalScore].next.prev = team.tails[i].prev;
				team.tails[i].prev = team.heads[i];
				team.heads[finalScore].next = team.heads[i].next;
				team.heads[i].next = team.tails[i];
			}
		} else {
			// 추가할 평점이 음수일 때
			for(int i=2; i<=5; i++) {
				// 아무것도 없는 리스트는 무시
				if(team.heads[i].next == team.tails[i]) continue;
				// 평점 변화 후 점수 계산
				int finalScore = i + mChangeScore;
				if(finalScore < 1) finalScore = 1;
				// 리스트 전체를 목적 버킷의 헤더와 첫 병사 사이로 연결
				team.heads[i].next.prev = team.heads[finalScore];
				team.tails[i].prev.next = team.heads[finalScore].next;
				team.heads[finalScore].next.prev = team.tails[i].prev;
				team.tails[i].prev = team.heads[i];
				team.heads[finalScore].next = team.heads[i].next;
				team.heads[i].next = team.tails[i];
			}
		}
	}
	
	int bestSoldier(int mTeam) {
		// 5번에 있는 병사들 리스트
		// 없으면 4번
		// 또 없으면 3번
		// 높은 평점에 대한 연결리스트에 병사가 하나라도 존재하면 그 리스트를 순차탐색
		// 최대 100_000 * 100 = 10_000_000
		Team team = teams[mTeam];
		
		int maxId = -1;
		for(int i=5; i>=1; i--) {
			Soldier target = team.heads[i].next;
			if(target == team.tails[i]) continue;
			while(target != team.tails[i]) {
				if(target.id > maxId) maxId = target.id;
				target = target.next;
			}
			break;
		}
		
		return maxId;
	}
	
}
