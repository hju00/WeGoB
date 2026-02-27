import java.util.*;

class UserSolution {
    private static final int INF = 1_000_000_000;
    
    private int n;
    private int capital;

    private List<List<Edge>> graph;
    private HashMap<Integer, Edge> idToEdge;

    private int[] distSum;
    private int[] distMax;
    private Edge[] parentEdge;

    // true이면 삭제로 인해 현재 최단경로 캐시가 오래된 상태임을 의미한다.
    private boolean dirty;

    private Queue<State> pq;

    private static class Edge{
        int to;
        int weight;
        boolean active;

        Edge(int to, int weight){
            this.to = to;
            this.weight = weight;
            this.active = true;
        }
    }

    private static final class State implements Comparable<State> {
        int node;
        int sum;
        int maxVal;

        State(int node, int sum, int maxVal) {
            this.node = node;
            this.sum = sum;
            this.maxVal = maxVal;
        }

        @Override
        public int compareTo(State other) {
            if (sum != other.sum) {
                return Integer.compare(sum, other.sum);
            }
            if (maxVal != other.maxVal) {
                return Integer.compare(maxVal, other.maxVal);
            }
            return 0;
        }
    }


	public void init(int N, int mCapital, int K, int mId[], int sCity[], int eCity[], int mDistance[]) {
        // 그래프를 구성하고 고정 시작점(수도) 기준 최단경로 캐시를 초기화한다.
        n = N;
        capital = mCapital;

        graph = new ArrayList<>();
        for(int i = 0; i < n; i++){
            graph.add(new ArrayList<>());
        }

        idToEdge = new HashMap<>((K*2) + 1);

        distSum = new int[n];
        distMax = new int[n];
        parentEdge = new Edge[n];

        pq = new PriorityQueue<>();

        for(int i = 0; i < K; i++){
            Edge e = new Edge(eCity[i], mDistance[i]);
            idToEdge.put(mId[i], e);
            graph.get(sCity[i]).add(e);
        }

        runFullDijkstra();
	}

	public void add(int mId, int sCity, int eCity, int mDistance) {
        // 간선을 먼저 추가하고, 캐시가 dirty면 재계산은 calculate() 시점으로 미룬다.
        Edge e = new Edge(eCity, mDistance);
        idToEdge.put(mId, e);
        graph.get(sCity).add(e);

        if(dirty) return;
        if(distSum[sCity] == INF) return;

        int candSum = distSum[sCity] + mDistance;
        int candMax = Math.max(distMax[sCity], mDistance);
        
        // 새 간선이 eCity를 개선하는 경우에만 증분 완화를 수행한다.
        if(isBetter(candSum, candMax, eCity)){
            distSum[eCity] = candSum;
            distMax[eCity] = candMax;

            parentEdge[eCity] = e;
            runIncrementalDijkstra(eCity);
        }
	}

	public void remove(int mId) {
        Edge e = idToEdge.remove(mId);
        e.active = false;

        // 삭제된 간선이 현재 parent 간선이면 캐시가 무효화된다.
        if(parentEdge[e.to] == e) dirty = true;
	}

    public int calculate(int mCity) {
        // 필요할 때만 전체 재계산하는 지연(lazy) 방식.
        if(dirty) {
            runFullDijkstra();
        }

        if(distSum[mCity] == INF) return -1;

		return distMax[mCity];
	}

    // 최적화 기준: (총거리, 경로 내 최대 간선 길이) 사전순 최소.
    private boolean isBetter(int candSum, int candMax, int city) {
        int curSum = distSum[city];
        if (candSum != curSum) {
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
        pq.offer(new State(capital, 0, 0));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            // 최신 dist와 맞지 않는 오래된 PQ 상태는 건너뛴다.
            if (cur.sum != distSum[cur.node] || cur.maxVal != distMax[cur.node]) {
                continue;
            }

            for (Edge e : graph.get(cur.node)) {
                if (!e.active) {
                    continue;
                }

                int ns = cur.sum + e.weight;
                int nm = Math.max(cur.maxVal, e.weight);
                if (isBetter(ns, nm, e.to)) {
                    distSum[e.to] = ns;
                    distMax[e.to] = nm;
                    parentEdge[e.to] = e;
                    pq.offer(new State(e.to, ns, nm));
                }
            }
        }

        // 전체 재계산이 끝나면 캐시는 최신 상태다.
        dirty = false;
    }

    private void runIncrementalDijkstra(int startNode) {
        // add()로 처음 개선된 정점부터 영향 구간만 전파한다.
        pq.clear();
        pq.offer(new State(startNode, distSum[startNode], distMax[startNode]));

        while (!pq.isEmpty()) {
            State cur = pq.poll();

            if (cur.sum != distSum[cur.node] || cur.maxVal != distMax[cur.node]) {
                continue;
            }

            for (Edge e : graph.get(cur.node)) {
                if (!e.active) {
                    continue;
                }

                int ns = cur.sum + e.weight;
                int nm = Math.max(cur.maxVal, e.weight);
                if (isBetter(ns, nm, e.to)) {
                    distSum[e.to] = ns;
                    distMax[e.to] = nm;
                    parentEdge[e.to] = e;
                    pq.offer(new State(e.to, ns, nm));
                }
            }
        }
    }
}
