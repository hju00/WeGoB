package coupon_distribution;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

class UserSolution {
	
	// 부서 ID를 통해 부서 정보를 조회
	// Key : 부서 ID, Value : 부서 정보
	static HashMap<Integer, Department> departmentDB;
	
	// Key : 부서 ID , Value : 최상위 부서 ID
	static HashMap<Integer, Integer> parent;
	
	// Key : 부서 ID , Value : 자신 부서와 하위 모든 부서들의 총 인원 수 합
	static HashMap<Integer, Integer> subTree_sum;
	
	// Key : 최상위 부서 ID, Value : 최상위 부서 그룹의 총 인원 수
	static HashMap<Integer, Integer> ancestor_sum;
	
	// 현재 존재하는 그룹 중 가장 많은 인원을 가진 그룹의 인원 수
	static int max_people;
	
	// 부서 정보 class
	static class Department	{
		int id;
		int people;
		int parent;
		ArrayList<Integer> childId;

		public Department(int mId, int people, int parent) {
			super();
			this.id = mId;
			this.people = people;
			this.parent = parent;
			childId = new ArrayList<>();
		}
	}
	
	public void updateParentSum(int x, int people)	{

		Department c = departmentDB.get(x);
	    
	    // 현재 노드의 subTree_sum 갱신
	    subTree_sum.put(x, subTree_sum.get(x) + people);
		
		// 부모가 없을 때 까지 재귀
		if(c.parent != c.id)	
			updateParentSum(c.parent, people);
	}
	
	// 유니온 - 파인드
	public void union(int x, int y)	{
		int rootX = find(x);
		int rootY = find(y);
		
		if(rootX == rootY)	return;
		
		parent.replace(rootY, rootX);
	}
	
	// 유니온 - 파인드 Map을 사용하기 때문에 살짝 변형
	public int find(int x) {
		if(parent.get(x) == x)	return x;
		
		int rootX = find(parent.get(x));
		
		parent.put(x, rootX);
		
		return rootX;
	}
	
	public void init(int N, int mId[], int mNum[]) {
		
		// static 초기화
		departmentDB = new HashMap<>();
		parent = new HashMap<>();
		ancestor_sum = new HashMap<>();
		subTree_sum = new HashMap<>();
		max_people = 0;
		
		// 최상위 부서 추가
		for(int i = 0; i < N; i++)	{
			// 최상위 부서의 부모는 자기 자신
			Department addDepartment = new Department(mId[i], mNum[i], mId[i]);
			departmentDB.put(mId[i], addDepartment);
			parent.put(mId[i], mId[i]);
			subTree_sum.put(mId[i], mNum[i]);
			ancestor_sum.put(mId[i], mNum[i]);
		}
		
		return;
	}

	public int add(int mId, int mNum, int mParent) {
		
		// mParent 부서에 이미 3개의 하위 부서가 존재한다면, 추가에 실패하고 -1을 반환
		Department parentDepartment = departmentDB.get(mParent);
		if(parentDepartment.childId.size() == 3)
			return -1;
		
		// 추가할 부서
		Department addDepartment = new Department(mId, mNum, mParent);		
		departmentDB.put(mId, addDepartment);
		parent.put(mId, mId);
		subTree_sum.put(mId, mNum);
		
		// 부서 추가 로직
		// 부모의 childId에 mId 추가
		parentDepartment.childId.add(mId);
		
		// subtree 인원 추가
		updateParentSum(mParent, mNum);
		
		// 추가하는 부서를 부모의 최상위 부서와 그룹화
		union(mParent, mId);
		
		// 최상위 부모 그룹에 인원 추가
		int ancestorId = find(mParent);
		int new_value = ancestor_sum.get(ancestorId) + mNum;
		ancestor_sum.replace(ancestorId, new_value);
		
		// 최대 인원, 최소 인원 갱신
		max_people = Math.max(max_people, new_value);
		
		// 추가하는 부서가 속한 그룹의 총 인원 수 return
		return subTree_sum.get(mParent);
	}

	public int remove(int mId) {
		
		// mId 부서가 존재하지 않을 경우, -1을 반환
		if(!departmentDB.containsKey(mId))
			return -1;
		
		// 삭제하려는 부서의 하위 부서까지의 인원 합
		int ret = subTree_sum.get(mId);
		
		// 상위 부서에서 삭제하려는 서브 트리의 합을 빼줌
		Department toRemove = departmentDB.get(mId);
		updateParentSum(toRemove.parent, -ret);
		
		// 최상위 부서의 인원 합에서도 빼줌
		int ancestor = find(mId);
		int old_value = ancestor_sum.get(ancestor);
		int new_value = ancestor_sum.get(ancestor) - ret;
		ancestor_sum.put(ancestor, new_value);
		
		// 부모 부서의 자식 부서 목록에서 제외, mId가 int여서 index로 생각하기 때문에 Object로 변경해줌
		departmentDB.get(toRemove.parent).childId.remove(Integer.valueOf(mId));
		
		// 하위 부서까지 모두 지우기
		// 최상위 부서의 ID가 주어지는 경우는 없음
		Queue<Integer> q = new ArrayDeque<>();
		q.offer(mId);
		
		while(!q.isEmpty())	{
			int cId = q.poll();
			Department cDepartment = departmentDB.get(cId);
			
			for(int nId : cDepartment.childId)	
				q.offer(nId);
			
			departmentDB.remove(cId);
			parent.remove(cId);
			subTree_sum.remove(cId);
		}
		
		// 삭제한 부서가 인원 수가 가장 많은 그룹에 속했을 경우 max_people 값 갱신
		if(old_value == max_people)	{
			max_people = 0;
			for(int p : ancestor_sum.values())
				max_people = Math.max(max_people, p);
		}
		
		return ret;
	}

	public int distribute(int K) {
		
		int total_people = 0;
		for(int p : ancestor_sum.values())
			total_people += p;
		
		// 총 인원수가 K 이하인 경우, 각 그룹의 인원 수대로 상품권을 나누어 줌
		if(K >= total_people)	
			return max_people;
		
		// 총 인원수가 K보다 큰 경우, 상한 개수 L을 정한다. 그룹의 인원 수가 L 이하인 경우에는 그룹의 인원 수대로 상품권을 주고,
		// 그룹의 인원 수가 L을 초과하는 경우에는 L개의 상품권을 준다.
		ArrayList<Integer> a = new ArrayList<>(ancestor_sum.values());
		
		// 이분 탐색
		int low = 0;
		int high = max_people;
		int ans = 0;
		
		while(low <= high)	{
			int limit = low + (high - low) / 2;
			int total = 0;
			
			for(int p : a)	{
				if(p <= limit)	total += p;
				else			total += limit;
			}
			
			if(total <= K)	{
				ans = limit;
				low = limit + 1;
			}
			else	high = limit - 1;
		}
		
		return ans;
	}
}
