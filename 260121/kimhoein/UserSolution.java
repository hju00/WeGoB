import java.util.*;

class UserSolution
{
    static class Node{
        int x;
        int y;
        int mGateID;
        int count=0;
        boolean remove = false;
        
        Node(int x, int y ,int mGateID){
            this.x = x;
            this.y = y;
            this.mGateID = mGateID;
        }
    }
    
    static class Mode implements Comparable<Mode>
    {
        int to;
        int distance;
        
        Mode(int to, int distance){
            this.to = to;
            this.distance = distance;
        }

		@Override
		public int compareTo(Mode o) {
			return this.distance - o.distance;
		}
    }
    
    static int mMaxStamin;
    static int mMap[][];
    static Node gate_Map[][];
    static int N;
    static Node gate[];
    static int mIdCount=0;
    
    static HashMap<Integer, Node> hash;
    static ArrayList<ArrayList<Mode>> list;
    static int delta[][] = {{1,0},{0,1},{-1,0},{0,-1}};
    
    void init(int N, int mMaxStamina, int mMap[][])
    {
        this.N = N;
        this.mMap = mMap;
        this.mMaxStamin = mMaxStamina;
        gate_Map = new Node[N][N];
        
        gate = new Node[200];
        hash = new HashMap<>();
        list = new ArrayList<>();
        
        for(int i=0; i<200; i++) {
            list.add(new ArrayList<Mode>());
        }
        
        return;
    }

    void addGate(int mGateID, int mRow, int mCol)
    {
    	mIdCount = mGateID;
        Node n = new Node(mRow,mCol,mGateID);
        hash.put(mGateID, n);
        return;
    }

    void removeGate(int mGateID)
    {
        if(hash.containsKey(mGateID)) {
            hash.get(mGateID).remove = true;
            return;
        }
        gate[mGateID].remove = true;
        return;
    }

    int getMinTime(int mStartGateID, int mEndGateID)
    {
        if(hash.size() != 0) connect_gate();
        
        Mode m = new Mode(mStartGateID,0);
        
        PriorityQueue<Mode> pq = new PriorityQueue<>();
        pq.add(m);
        int distance[] = new int[mIdCount+1];
        Arrays.fill(distance, Integer.MAX_VALUE/2+1);
        distance[m.to] = 0;
        
        while(!pq.isEmpty()) {
        	Mode cur = pq.poll();
        	
        	if(mEndGateID == cur.to) return distance[cur.to];
        	
        	for(Mode next : list.get(cur.to)) {
        		if(distance[next.to] <= next.distance + distance[cur.to]) continue;
        		if(gate[next.to].remove) continue;
        		
        		distance[next.to] = next.distance + distance[cur.to];
        		//if(cur.to == 5) System.out.println("cur.distance : " + cur.distance);
        		//System.out.println("cur : " + cur.to + " next : " + next.to + " distance[next.to] : " + distance[next.to] + " next.distance : " + next.distance);
        		
        		//if(mEndGateID == next.to) return distance[next.to];
        		
        		pq.add(new Mode(next.to, distance[next.to]));
        	}
        }
        
        return -1;
    }
    
    void connect_gate()
    {
    	//System.out.println("connect_gate");
        for(int n : hash.keySet()) {
            Node node = hash.get(n);
            if(node.remove) continue;
            
            Queue<Node> que = new LinkedList<>();
            gate_Map[node.x][node.y] = node;
            gate[node.mGateID] = node;
            
            que.add(node);
            boolean visit[][] = new boolean[N][N];
            visit[node.x][node.y] = true;
            //System.out.println("connect_gate");
            
            while(!que.isEmpty()) {
                Node cur = que.poll();
                if(cur.count >= mMaxStamin) continue;
                
                for(int i=0; i<4; i++) {
                    int dx = cur.x + delta[i][0];
                    int dy = cur.y + delta[i][1];
                    
                    if(!(dx >=0 && dx < N && dy >= 0 && dy < N)) continue;
                    if(mMap[dx][dy] == 1) continue;
                    if(visit[dx][dy]) continue;
                    
                    visit[dx][dy] = true;
                    if(gate_Map[dx][dy] != null) {
                        list.get(gate_Map[dx][dy].mGateID).add(new Mode(cur.mGateID, cur.count+1));
                        list.get(cur.mGateID).add(new Mode(gate_Map[dx][dy].mGateID, cur.count+1));
                        
                        //if(cur.mGateID == 5) System.out.println("gate_Map[dx][dy].mGateID : " + gate_Map[dx][dy].mGateID + " cur.count " + cur.count);
                    }
                    //if(cur.mGateID == 1) System.out.println("cur1 : " + cur.x + " " + cur.y);
                    //if(cur.mGateID == 2) System.out.println("cur2 : " + cur.x + " " + cur.y + " " + cur.count);
                    
                    //if(cur.mGateID == 5) System.out.println("cur5 : " + cur.x + " " + cur.y + " " + cur.count);
                    
                    //system.out.println("cur : " + cur.x + " " + cur.y);
                    
                    //if(cur.mGateID == 5) System.out.println("cur : " + cur.x + " " + cur.y);
                    
                    Node temp = new Node(dx,dy,cur.mGateID);
                    temp.count = cur.count+1;
                    //gate_Map[dx][dy] = temp;
                    
                    if(cur.count == mMaxStamin) break;
                    que.add(temp);
                    
                }
                
            }
            
        }
        
        hash.clear();
       
        
    }
}