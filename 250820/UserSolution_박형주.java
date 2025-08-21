package soldier_management;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

class UserSolution
{
	SoldierList[][] teamList;	// 5개의 팀과 5개의 점수로 병사를 관리하는 배열
	Soldier[] soldierDB;		// mID를 인덱스로 사용하여 각 Soldier 객체에 O(1)로 직접 접근하기 위한 배열
	
	// Soldier[] heads : 각 점수(1~5점)별 이중 연결 리스트의 시작 노드(head)를 가리키는 포인터 배열
	// Soldier[] tails : 각 점수별 리스트의 마지막 노드(tail)를 가리키는 포인터 배열
	static class SoldierList	{
		Soldier head;
		Soldier tail;
		
		public SoldierList() {
			head = new Soldier(0, 0, 0);
			tail = new Soldier(0, 0, 0);
			head.next = tail;
			tail.prev = head;
		}
	}
	
	// 직접 구현한 이중 연결 리스트(Doubly Linked List)를 사용해야 하는 이유
	// 노드(Node)에 직접 접근하여 O(1) 시간 복잡도로 삭제/이동시키기 위함
	static class Soldier	{
		int ID;
		int team;
		int score;
		
		// 이중 연결 리스트를 구성하기 위한 포인터
		Soldier prev;
		Soldier next;
		
		public Soldier(int ID, int team, int score) {
			this.ID = ID;
			this.team = team;
			this.score = score;
			this.prev = null;
			this.next = null;
		}
	}
	
	public void init()
	{
		teamList = new SoldierList[6][6];
		soldierDB = new Soldier[100001];
		
		for(int team = 1; team <= 5; team++)
			for(int score = 1; score <= 5; score++)
				teamList[team][score] = new SoldierList();
		
	}
	
	public void hire(int mID, int mTeam, int mScore)
	{
		Soldier s = new Soldier(mID, mTeam, mScore);
		soldierDB[mID] = s;
		
		SoldierList sl = teamList[mTeam][mScore];
		
		// teamList 의 이중 연결 리스트에 pushBack 로직
		s.next = sl.tail;		// 추가하는 병사의 next를 tail로 
		s.prev = sl.tail.prev;	// 추가하는 병사의 prev를 tail 이전 병사의 next로
		sl.tail.prev.next = s;	// tail 이전 병사의 next를 추가하는 병사로
		sl.tail.prev = s;		// tail의 prev를 추가하는 병사로
	}
	
	public void fire(int mID)
	{
		Soldier s = soldierDB[mID];
		soldierDB[mID] = null;
				
		// teamList 의 이중 연결 리스트의 remove 로직
		s.next.prev = s.prev;	// 삭제하는 병사 뒤의 병사의 prev를 삭제하는 병사 앞의 병사로
		s.prev.next = s.next;	// 삭제하는 병사 앞의 병사의 next를 삭제하는 병사 뒤의 병사로
	}

	public void updateSoldier(int mID, int mScore)
	{
		// 1. 기존 위치에서 병사를 제거 (fire 로직과 동일)
	    Soldier s = soldierDB[mID];
	    s.prev.next = s.next;
	    s.next.prev = s.prev;

	    // 2. 병사의 점수 정보를 갱신
	    s.score = mScore;

	    // 3. 새 점수에 맞는 리스트에 병사를 추가 (hire 로직과 동일)
	    SoldierList sl = teamList[s.team][s.score];
	    s.next = sl.tail;
	    s.prev = sl.tail.prev;
	    sl.tail.prev.next = s;
	    sl.tail.prev = s;
	}

	public void updateTeam(int mTeam, int mChangeScore) {
	    if (mChangeScore == 0) return;

	    // 1. 기존 리스트들을 임시 변수에 저장
	    SoldierList[] originalLists = teamList[mTeam];
	    
	    // 2. 결과를 담을 새로운 리스트 배열을 준비 (모두 빈 리스트로 시작)
	    SoldierList[] newLists = new SoldierList[6];
	    for (int i = 1; i <= 5; i++) {
	        newLists[i] = new SoldierList();
	    }

	    // 3. 단일 for문으로 1점부터 5점까지 순회하며 병사들을 새 리스트로 옮김
	    for (int score = 1; score <= 5; score++) {
	        // 원본 리스트가 비어있으면 건너뜀
	        if (originalLists[score].head.next == originalLists[score].tail) {
	            continue;
	        }

	        // 새 점수를 계산
	        int newScore = score + mChangeScore;
	        if (newScore > 5) newScore = 5;
	        if (newScore < 1) newScore = 1;
	        
	        // 기존 s점 리스트의 모든 병사를 newScore점 리스트로 합침
	        // (포인터 조작을 통한 O(1) 리스트 합치기)
	        SoldierList targetList = newLists[newScore];
	        SoldierList sourceList = originalLists[score];

	        // targetList의 끝에 sourceList를 그대로 이어 붙임
	        targetList.tail.prev.next = sourceList.head.next;
	        sourceList.head.next.prev = targetList.tail.prev;
	        targetList.tail = sourceList.tail;
	    }

	    // 4. 작업이 완료된 새로운 리스트 배열을 원래 위치에 할당
	    teamList[mTeam] = newLists;
	}
	
	public int bestSoldier(int mTeam) {
	    int max_mID = 0;
	    
	    // 5점부터 1점까지 순회
	    for (int score = 5; score >= 1; score--) {
	        SoldierList sl = teamList[mTeam][score];
	        // 리스트가 비어있지 않다면
	        if (sl.head.next != sl.tail) {
	            // 리스트의 모든 병사를 순회하며 최대 ID를 찾음
	            Soldier current = sl.head.next;
	            while (current != sl.tail) {
	                max_mID = Math.max(current.ID, max_mID);
	                current = current.next;
	            }
	            // 최고점 그룹을 찾았으므로 더 낮은 점수는 볼 필요 없음
	            return max_mID;
	        }
	    }
	    
	    return 0; // 이 경우는 문제 제약상 없음
	}
}