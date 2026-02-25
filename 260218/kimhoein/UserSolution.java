package study;

import java.util.*;

class UserSolution {
    
    private static class Edge
    {
        int to;
        int weight;
        boolean active;
        
        Edge(int to, int weight)
        {
            this.to = to;
            this.weight = weight;
            this.active = true;
        }
    }
    
    private static final class State implements Comparable<State>{
        int node;
        int sum;
        int maxVal;
        
        State(int node, int sum, int maxVal){
            this.node = node;
            this.sum = sum;
            this.maxVal = maxVal;
        }

        @Override
        public int compareTo(State o) {
        	if(sum != o.sum) {
        		return Integer.compare(sum, o.sum);
        	}
        	if(maxVal != o.maxVal) {
        		return Integer.compare(maxVal, o.maxVal);
        	}
            return 0;
        }
        
        
    }
    
    private int n;
    private int capital;
    
    private List<List<Edge>> graph;
    private HashMap<Integer, Edge> idToEdge;
    
    private int[] distSum;
    private int[] distMax;
    private Edge[] parentEdge;
    
    private boolean dirty;
    
    private Queue<State> pq;
    
    private static final int INF = Integer.MAX_VALUE/2;
    
    public void init(int N, int mCapital, int K, int mId[], int sCity[], int eCity[], int mDistance[]) {
        n = N;
        capital = mCapital;
        
        graph = new ArrayList<>();
        
        for(int i=0; i<n; i++) {
        	graph.add(new ArrayList<>());
        }
        
        idToEdge = new HashMap<>((K*2)+1);
        
        distSum = new int[n];
        distMax = new int[n];
        parentEdge = new Edge[n];
        
        pq = new PriorityQueue<>();
        
        for(int i=0; i<K; i++) {
        	Edge e = new Edge(eCity[i], mDistance[i]);
        	idToEdge.put(mId[i],e);
        	graph.get(sCity[i]).add(e);
        }
        
        runFullDijkstra();
        
    	return;
    }
    
    public void add(int mId, int sCity, int eCity, int mDistance) {
        Edge e = new Edge(eCity, mDistance);
        idToEdge.put(mId, e);
        graph.get(sCity).add(e);
        
        if(dirty) return;
        if(distSum[sCity] == INF) return;
        
        int candSum = distSum[sCity] + mDistance;
        int candMax = Math.max(distMax[sCity], mDistance);
        
        // 새 간선이 eCity를 개선하는 경우에만 증분 완화를 수행
        if(isBetter(candSum, candMax, eCity)) {
        	distSum[eCity] = candSum;
        	distMax[eCity] = candMax;
        
        	parentEdge[eCity] = e;
        	runIncrementalDijkstra(eCity);
        }

    }

    public void remove(int mId) {
        Edge e = idToEdge.remove(mId);
        e.active = false;
        
        if(parentEdge[e.to] == e) dirty = true;
        
    }

    public int calculate(int mCity) {
        
    	if(dirty) {
    		runFullDijkstra();
    	}
    	
    	if(distSum[mCity] == INF) return -1;
    	
    	return distMax[mCity];
    }
    
    private boolean isBetter(int candSum, int candMax, int city) {
    	int curSum = distSum[city];
    	if(candSum != curSum) {
    		return candSum < curSum;
    	}
    	return candMax < distMax[city];
    }
    
    private void runFullDijkstra() {
    	Arrays.fill(distSum, INF);
    	Arrays.fill(distMax, INF);
    	Arrays.fill(parentEdge, null);
    	
    	distSum[capital] = 0;
    	distMax[capital] = 0;
    	
    	pq.clear();
    	pq.offer(new State(capital, 0 , 0));
    	
    	while(!pq.isEmpty()) {
    		State cur = pq.poll();
    		
    		if(cur.sum != distSum[cur.node] || cur.maxVal != distMax[cur.node]) continue;
    		
    		for(Edge e : graph.get(cur.node)) {
    			if(!e.active) continue;
    			
    			int ns = cur.sum + e.weight;
    			int nm = Math.max(cur.maxVal, e.weight);
    			if(isBetter(ns,nm,e.to)) {
    				distSum[e.to] = ns;
    				distMax[e.to] = nm;
    				parentEdge[e.to] = e;
    				pq.offer(new State(e.to,ns,nm));
    			}
    		}
    	}
    	
    	dirty = false;
    }
    
    private void runIncrementalDijkstra(int startNode) {
    	pq.clear();
    	pq.offer(new State(startNode, distSum[startNode], distMax[startNode]));
    	
    	while(!pq.isEmpty()) {
    		State cur = pq.poll();
    		
    		if(cur.sum != distSum[cur.node] || cur.maxVal != distMax[cur.node]) continue;
    		
    		for(Edge e : graph.get(cur.node)) {
    			if(!e.active) continue;
    			
    			int ns = cur.sum + e.weight;
    			int nm = Math.max(cur.maxVal, e.weight);
    			if(isBetter(ns, nm, e.to)) {
    				distSum[e.to] = ns;
    				distMax[e.to] = nm;
    				parentEdge[e.to] = e;
    				pq.offer(new State(e.to, ns, nm));
    			}
    		}
    	}
    }
    
}