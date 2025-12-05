import java.util.*;

class UserSolution {
	private static final int MAX_K = 200;
	static class Train{
		int start, end, interval;
		boolean valid;

		void set(int start, int end, int interval){
			this.start = start;
			this.end = end;
			this.interval = interval;

			this.valid = true;
		}
	}

	Train[] trains = new Train[MAX_K];
	ArrayList<Integer>[] graphs = new ArrayList[MAX_K];
	Map<Integer, Integer> idToIdx = new HashMap<>();
	int cnt;

	private boolean isStop(int tIdx, int station) {
        Train t = trains[tIdx];
        return station >= t.start && station <= t.end && 
               (station - t.start) % t.interval == 0;
    }

	private boolean connectionCheck(int a, int b){
		if (trains[a].start > trains[b].end || trains[a].end < trains[b].start) return false;

		int u = a;
		int v = b;

		long stopsA = (long) (trains[a].end - trains[a].start) / trains[a].interval;
        long stopsB = (long) (trains[b].end - trains[b].start) / trains[b].interval;

        if (stopsA > stopsB) {
            u = b;
            v = a;
        }

        for (int i = trains[u].start; i <= trains[u].end; i += trains[u].interval) {
            if (i < trains[v].start) continue;
            if (i > trains[v].end) break;
            
            if ((i - trains[v].start) % trains[v].interval == 0) {
                return true;
            }
        }

		return false;
	}

	public void init(int N, int K, int mId[], int sId[], int eId[], int mInterval[]) {
		idToIdx.clear();
		cnt = 0;
		
		for (int i = 0; i < MAX_K; i++) {
            if (trains[i] == null) trains[i] = new Train();
            if (graphs[i] == null) graphs[i] = new ArrayList<>();
            graphs[i].clear(); 
        }

		for(int i = 0; i < K; ++i){
			add(mId[i], sId[i], eId[i], mInterval[i]);
		}
	}

	public void add(int mId, int sId, int eId, int mInterval) {
		int idx = cnt++;

		idToIdx.put(mId, idx);

		graphs[idx].clear();
		trains[idx].set(sId, eId, mInterval);

		for(int j = 0; j < idx; ++j){
			if(!trains[j].valid) continue;

			if(connectionCheck(idx, j)){
				graphs[idx].add(j);
				graphs[j].add(idx);
			}
		}
	}

	public void remove(int mId) {
		if (idToIdx.containsKey(mId)) {
            int idx = idToIdx.get(mId);
            trains[idx].valid = false;
        }
	}

	public int calculate(int sId, int eId) {
		boolean[] visited = new boolean[MAX_K];
		Queue<int[]> q = new ArrayDeque<>();

		for(int i = 0; i < cnt; ++i){
			if(!trains[i].valid) continue;

			if (isStop(i, sId)) {
                visited[i] = true;
                q.offer(new int[]{0, i});
            }
		}

		while (!q.isEmpty()) {
            int[] curr = q.poll();
            int dist = curr[0];
            int u = curr[1];

            if (isStop(u, eId)) {
                return dist;
            }

            for (int v : graphs[u]) {
                if (!trains[v].valid) continue;
                if (visited[v]) continue;

                visited[v] = true;
                q.offer(new int[]{dist + 1, v});
            }
        }

		return -1;
	}
}