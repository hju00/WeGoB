package study;

import java.util.*;

class UserSolution {
	static char[][][] map;
	static HashMap<String , Monarch> hash = new HashMap<>();
	static int[] parent;
    static int[] size; // 각 집합 크기
    static List<List<Integer>> children; // 각 노드의 자식 리스트
    static int N;
    static int[][] delta = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
    
	class Monarch
	{
		int id;
		int x;
		int y;
		int soldier;
		HashSet<String> set;
		
		public Monarch(int x, int y, int soldier, int id) {
			this.x = x;
			this.y = y;
			this.soldier = soldier;
			this.id = id;
			set = new HashSet<String>();
		}
	}
	
	

 // union 연산
    public static boolean union(Monarch A, Monarch B) {
    	int x = A.id;
    	int y = B.id;
    	
        x = find(x);
        y = find(y);

        if (x == y) return false;
        
        // 작은 집합을 큰 집합 밑으로 합치기
        if (size[x] < size[y]) {
            parent[x] = y;
            size[y] += size[x];
            children.get(y).add(x);
            
            int n = find(y);
            hash.get(new String(map[n/N][n%N])).set.addAll(A.set);
        } else {
            parent[y] = x;
            size[x] += size[y];
            children.get(x).add(y);
            
            int n = find(x);
            hash.get(new String(map[n/N][n%N])).set.addAll(B.set);
        }
        
        A.id = x;
        B.id = y;
        return true;
    }

    // find 연산 (경로 압축)
    public static int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    // 원소 x를 newParent가 속한 집합으로 이동
    public static void move(int x, int newParent) {
        int oldParent = find(x);
        newParent = find(newParent);
        
        if (oldParent == newParent) return; // 이미 같은 집합이면 종료
        
        for(int i : children.get(x))
        {
        	System.out.println("i : " + i + " oldParent : " + oldParent);
        	parent[i] = oldParent;
        }
        children.get(oldParent).addAll(children.get(x));
        children.get(x).clear();
        System.out.println("x " + children.get(x).size());
        children.get(newParent).add(x);
        
        // 원래 집합에서 제거
        size[oldParent]--;
        parent[x] = newParent; // 부모 변경
        size[newParent]++;     // 새 집합 크기 증가
    }
    
	
    
    void init(int N, int mSoldier[][], char mMonarch[][][])
    {
    	this.N = N;
        parent = new int[N*N + 1];
        size = new int[N*N + 1];
        children = new ArrayList<>();
        map = new char[N][N][];
        
        for (int i = 0; i <= N; i++) {
            parent[i] = i;
            size[i] = 1;
            children.add(new ArrayList<>());
        }
        
    	for(int i=0; i<N; i++)
    	{
    		for(int j=0; j<N; j++)
    		{
    			
    			//System.out.println(i + " mMonarch[i][j] : " + mMonarch[i][j].toString());
    			map[i][j] = mMonarch[i][j];
    			String name = new String(mMonarch[i][j]);
    			hash.put(name, new Monarch(i,j,mSoldier[i][j],i*N+j));
    		}
    	}
    }
    void destroy()
    {

    }
    int ally(char mMonarchA[], char mMonarchB[])
    {
    	Monarch A = hash.get(new String(mMonarchA));
    	Monarch B = hash.get(new String(mMonarchB));
    	System.out.println("A : " + A.id + " " + A.soldier + " " + A.x + " " + A.y);
    	Boolean result = union(A,B);
    	
    	if(!A.set.isEmpty() && !B.set.isEmpty())
    	{
    		for(String c : A.set)
    		{
    			if(B.set.contains(c)) return -2;
    		}
    		//if(result) return 1;	// 동맹 채결    		
    	}
    	
    	if(!result) return -1; 	// 동맹 미채결
    	
        return 1;
    }
    
    int attack(char mMonarchA[], char mMonarchB[], char mGeneral[])
    {
    	
    	
    	
    	Monarch b = hash.get(new String(mMonarchB));
    	List<Monarch> list = new LinkedList<UserSolution.Monarch>();
    	int id_a = hash.get(new String(mMonarchA)).id;
    	
    	for(int i=0; i<delta.length; i++)
    	{
    		int dx = b.x + delta[i][0];
    		int dy = b.y + delta[i][1];
    		
    		if(!(dx >=0 && dx<N && dy>=0 && dy<N)) continue;
    		System.out.println("dx : " + dx + " dy : " + dy + " map[dx][dy] : " + new String(map[dx][dy]));
    		Monarch m = hash.get(new String(map[dx][dy]));
    		if(find(id_a) == find(m.id)) list.add(m);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
    	}
    	
    	if(list.size() == 0) return -2;
    	
    	int count =0;
    	
    	for(Monarch m : list)
    	{
    		count += m.soldier/2;
    		m.soldier -= m.soldier/2;
    	}
    	
    	if(count <= b.soldier)
    	{
    		b.soldier -= count;
    		return 0;
    	}
    	else
    	{
    		hash.remove(new String(map[b.x][b.y]));
    		map[b.x][b.y] = mGeneral;
    		Monarch m2 = new Monarch(b.x,b.y,count- b.soldier,b.x*N+b.y);

    		hash.put(new String(mGeneral), m2);
    		
    		size[find(b.id)]-=1;
    		union(hash.get(new String(mMonarchA)), m2);
    		return 1;
    	}
    }
    int recruit(char mMonarch[], int mNum, int mOption)
    {
    	int total=0;
    	if(mOption == 0)
    	{
    		hash.get(new String(mMonarch)).soldier += mNum;
    		total = hash.get(new String(mMonarch)).soldier;
    	}
    	else
    	{
    		//int id = find(hash.get(new String(mMonarch)).id);
    		
    		Queue<Monarch> q = new LinkedList<UserSolution.Monarch>();
    		
    		while(!q.isEmpty())
    		{
    			Monarch m = q.poll();
    			
    			m.soldier += mNum;
    			total += m.soldier;
    			
    			for(int next : children.get(m.id))
    			{
    				q.add(hash.get(new String(map[next/N][next%N])));
    				
    			}
    		}
    		
    		
    	}
        return total;
    }
}