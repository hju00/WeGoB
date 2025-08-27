import java.util.*;

class UserSolution {
	static Map<Integer, Department> groups; // id, department
	static Map<Integer, Integer> parentsMap; // child, parents
	
	public void init(int N, int mId[], int mNum[]) {
		groups = new HashMap<>();
		parentsMap = new HashMap<>();
		
		for (int i = 1; i <= N; i++) {
			groups.put(i, new Department(mId[i], mNum[i]));
			parentsMap.put(mId[i], -1);
		}
		
		return;
	}
 
	public int add(int mId, int mNum, int mParent) {
		Department top = groups.get(findTopParentId(mParent)); // 최상위 부모 객체(최상위 그룹) 아이디로 객체 찾기
		Department parent = find(top, mParent);  // parentId를 가진 노드 찾
		
		return parent.add(mId, mNum);  // 추가
	}

	public int remove(int mId) {
		// 하위 객체를 다 내려가서 Map에서 삭제해주는게 좋으려나 아니면 그냥 flag 처리만 ? (일단 함) 
		int id = findTopParentId(mId);
		Department top = groups.get(id);
		
		return groups.containsKey(id) ? top.delete() : -1;
	}

	public int distribute(int K) {
		return 0;
	}
	
	public static int findTopParentId(int id) {
		while (parentsMap.get(id) != -1) id = parentsMap.get(id);
		
		return id;
	}
	
	static Department find(Department from, int target) {
    System.out.println(target);
		if (from.id == target) return from;
		
		for (Department d : from.subtrees) {
			if (d == null) continue;
			
			from = find(d, target);
			
			if (from.id == target) break;
		}
		
		return from;
	}
	
	static class Department {
		int id, population;
		int cur, total;
		Department[] subtrees;
		
		public Department(int id, int population) {
			this.id = id;
			this.population = population;
			this.cur = -1;
			this.total = 0;
			this.subtrees = new Department[3];
		}
		
		int add(int id, int num) {
			if (cur > 2) return -1;
			
			subtrees[++cur] = new Department(id, num);
			total += num;
			
			return this.total;
		}
		
		int delete() {
			parentsMap.remove(this.id);
			
			while (cur >= 0) {
				total += subtrees[cur].delete();
				--cur;
			}
			
			return total;
		}
	}
}
