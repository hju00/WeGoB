import java.util.*;

class UserSolution {
	
	static int n;
	static HashMap<Integer, Building> buildingDB;
	static TreeSet<Building> buildings;
	static TreeSet<Space> spaces;
	
	static class Building implements Comparable<Building> {
		int s, e;
		int length;
		
		public Building(int s, int length) {
			super();
			this.s = s;
			this.e = s + length - 1;
			this.length =  length;
		}

		@Override
		public int compareTo(Building o) {
			return Integer.compare(this.s, o.s);
		}
	}
	
	static class Space implements Comparable<Space> {
		int s, e;
		int length;
		
		public Space(int s, int e) {
			super();
			this.s = s;
			this.e = e;
			this.length = e - s + 1;
		}

		@Override
		public int compareTo(Space o) {
			if(this.length == o.length)
				return Integer.compare(this.s, o.s);
			return Integer.compare(o.length, this.length);
		}
	}

	public void init(int N){
		n = N;
		
		buildingDB = new HashMap<>();
		buildings = new TreeSet<>();
		spaces = new TreeSet<>();
		
		Space sp = new Space(0, n - 1);
		spaces.add(sp);
	}

	public int build(int mLength) {
		
		// �� ������ ���� ���
		if(spaces.isEmpty())
			return -1;
		
		// ���� �� �� ������ ���̰� ������ ���̺��� ���� ���
		if(spaces.first().length < mLength)
			return -1;
		
		Space cur = spaces.pollFirst();
		
		int diffLength = cur.length - mLength;
		int s = cur.s + diffLength / 2;
		
		Building newB = new Building(s, mLength);
		buildingDB.put(s, newB);
		buildings.add(newB);
		
		Building left = buildings.lower(newB);
		Building right = buildings.higher(newB);
		
		int left_end = (left == null) ? -1 : left.e;
		int right_start = (right == null) ? n : right.s;
		
		Space newLeftSpace = new Space(left_end + 1, newB.s - 1);
		Space newRightSpace = new Space(newB.e + 1, right_start - 1);
		
		// �� ������ ���̰� 0�̸� �� ������ �ƴ�
		if(newLeftSpace.length > 0) 	spaces.add(newLeftSpace);
		if(newRightSpace.length > 0) 	spaces.add(newRightSpace);
		
		return newB.s;
	}

	public int demolish(int mAddr) {
		
		// �־����� �ּҰ� ������ �ּҰ� �ƴ� ���
		if(!buildingDB.containsKey(mAddr))
			return -1;
		
		Building toRemove = buildingDB.get(mAddr);
		
		buildingDB.remove(mAddr);
		buildings.remove(toRemove);
		
		Building left = buildings.lower(toRemove);
		Building right = buildings.higher(toRemove);
		
		int left_end = (left == null) ? -1 : left.e;
		int right_start = (right == null) ? n : right.s;
		
		Space toRemoveLeft = new Space(left_end + 1, toRemove.s - 1);
		Space toRemoveRight = new Space(toRemove.e + 1, right_start - 1);
		Space newSpace = new Space(left_end + 1, right_start - 1);
		
		if(toRemoveLeft.length > 0)	spaces.remove(toRemoveLeft);
		if(toRemoveRight.length > 0) spaces.remove(toRemoveRight);
		spaces.add(newSpace);
		
		return toRemove.length;
	}
}
