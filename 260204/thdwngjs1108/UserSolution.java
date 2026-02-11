import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

class UserSolution {
	private static final int MAX_FILES = 500;
	private static final int CAP = 5001;

	private int N;
	private int fileCount;
	private HashMap<Integer, Integer> fileIdToIndex;
	private FileInfo[] files;
	private boolean[][] hasFile;
	private Request[][] requests;
	private ArrayList<Request> activeRequests;
	private ArrayList<Request>[] requestsByFile;
	private int[][] dist;
	private ArrayList<Edge>[] adj;

	private static class Edge {
		int to;
		int w;
		Edge(int to, int w) { this.to = to; this.w = w; }
	}

	private static class FileInfo {
		int size;
		int[] sources;
		int srcCount;

		FileInfo(int size) {
			this.size = size;
			this.sources = new int[4];
			this.srcCount = 0;
		}

		void addSource(int com) {
			if (srcCount == sources.length) {
				sources = Arrays.copyOf(sources, srcCount * 2);
			}
			sources[srcCount++] = com;
		}
	}

	private static class Request {
		int com;
		int fileIndex;
		int rateSources;
		int lastTime;
		long downloaded;
		boolean completed;

		Request(int com, int fileIndex, int lastTime) {
			this.com = com;
			this.fileIndex = fileIndex;
			this.lastTime = lastTime;
			this.rateSources = 0;
			this.downloaded = 0;
			this.completed = false;
		}
	}

	void init(int N, int mShareFileCnt[], int mFileID[][], int mFileSize[][]) {
		this.N = N;
		this.fileCount = 0;
		this.fileIdToIndex = new HashMap<>();
		this.files = new FileInfo[MAX_FILES];
		this.hasFile = new boolean[N + 1][MAX_FILES];
		this.requests = new Request[N + 1][MAX_FILES];
		this.activeRequests = new ArrayList<>();
		this.requestsByFile = new ArrayList[MAX_FILES];
		this.dist = new int[N + 1][N + 1];
		this.adj = new ArrayList[N + 1];

		for (int i = 1; i <= N; i++) {
			adj[i] = new ArrayList<>();
		}

		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				dist[i][j] = (i == j) ? 0 : CAP;
			}
		}

		// 초기 공유 파일 등록
		for (int i = 0; i < N; i++) {
			int cnt = mShareFileCnt[i];
			for (int k = 0; k < cnt; k++) {
				int fid = mFileID[i][k];
				int fsz = mFileSize[i][k];
				int idx = getOrCreateFileIndex(fid, fsz);
				files[idx].addSource(i + 1);
				hasFile[i + 1][idx] = true;
			}
		}
	}

	void makeNet(int K, int mComA[], int mComB[], int mDis[]) {
		// 초기 링크 구성
		for (int i = 0; i < K; i++) {
			int a = mComA[i];
			int b = mComB[i];
			int w = mDis[i];
			adj[a].add(new Edge(b, w));
			adj[b].add(new Edge(a, w));
		}

		// 제한 Dijkstra로 모든 쌍 최단거리(<=5000) 계산
		for (int s = 1; s <= N; s++) {
			dijkstra(s);
		}
	}

	void addLink(int mTime, int mComA, int mComB, int mDis) {
		// 시각 mTime까지 다운로드 진행 반영
		advanceTime(mTime);
		// 신규 링크를 경유하는 경우만 고려해 거리 갱신
		updateDistWithNewEdge(mComA, mComB, mDis);
		// 기존 요청의 다운로드 소스 수 재평가
		updateAllRatesAfterLink();
	}

	void addShareFile(int mTime, int mComA, int mFileID, int mSize) {
		// 시각 mTime까지 다운로드 진행 반영
		advanceTime(mTime);
		int idx = getOrCreateFileIndex(mFileID, mSize);
		files[idx].addSource(mComA);
		hasFile[mComA][idx] = true;

		// 해당 파일을 요청 중인 컴퓨터들만 소스 수 증가
		ArrayList<Request> list = requestsByFile[idx];
		if (list == null) return;
		for (int i = 0; i < list.size(); i++) {
			Request r = list.get(i);
			if (r.completed) continue;
			if (dist[r.com][mComA] <= 5000) {
				r.rateSources++;
			}
		}
	}

	int downloadFile(int mTime, int mComA, int mFileID) {
		// 시각 mTime까지 다운로드 진행 반영
		advanceTime(mTime);
		Integer idxObj = fileIdToIndex.get(mFileID);
		if (idxObj == null) return 0;
		int idx = idxObj;

		Request r = requests[mComA][idx];
		if (r == null) {
			r = new Request(mComA, idx, mTime);
			requests[mComA][idx] = r;
			activeRequests.add(r);
			if (requestsByFile[idx] == null) requestsByFile[idx] = new ArrayList<>();
			requestsByFile[idx].add(r);
		} else {
			if (r.completed) return 0;
		}

		// 첫 요청 시점에 도달 가능한 공유 파일 소스 수 계산
		if (r.rateSources == 0) {
			r.rateSources = countSourcesWithin(mComA, idx);
		}
		return r.rateSources;
	}

	int getFileSize(int mTime, int mComA, int mFileID) {
		// 시각 mTime까지 다운로드 진행 반영
		advanceTime(mTime);
		Integer idxObj = fileIdToIndex.get(mFileID);
		if (idxObj == null) return 0;
		int idx = idxObj;
		// 공유 파일이면 요청 여부와 무관하게 크기 반환
		if (hasFile[mComA][idx]) return files[idx].size;
		Request r = requests[mComA][idx];
		if (r == null) return 0;
		return (int) r.downloaded;
	}

	private void advanceTime(int t) {
		// 모든 활성 요청의 다운로드 진행량을 누적
		for (int i = 0; i < activeRequests.size(); i++) {
			Request r = activeRequests.get(i);
			if (r.completed) continue;
			int dt = t - r.lastTime;
			if (dt > 0) {
				if (r.rateSources > 0) {
					long add = (long) dt * 9L * (long) r.rateSources;
					long size = files[r.fileIndex].size;
					long nd = r.downloaded + add;
					if (nd >= size) {
						r.downloaded = size;
						r.completed = true;
						r.rateSources = 0;
					} else {
						r.downloaded = nd;
					}
				}
				r.lastTime = t;
			}
		}
	}

	private int getOrCreateFileIndex(int fileId, int size) {
		Integer idxObj = fileIdToIndex.get(fileId);
		if (idxObj != null) return idxObj;
		int idx = fileCount++;
		fileIdToIndex.put(fileId, idx);
		files[idx] = new FileInfo(size);
		return idx;
	}

	private void dijkstra(int src) {
		int[] d = new int[N + 1];
		Arrays.fill(d, CAP);
		d[src] = 0;

		PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
		pq.add(new int[] {0, src});

		while (!pq.isEmpty()) {
			int[] cur = pq.poll();
			int distU = cur[0];
			int u = cur[1];
			if (distU != d[u]) continue;
			if (distU > 5000) break;

			ArrayList<Edge> edges = adj[u];
			for (int i = 0; i < edges.size(); i++) {
				Edge e = edges.get(i);
				int v = e.to;
				int nd = distU + e.w;
				if (nd < d[v] && nd < CAP) {
					d[v] = nd;
					pq.add(new int[] {nd, v});
				}
			}
		}

		System.arraycopy(d, 0, dist[src], 0, N + 1);
	}

	private void updateDistWithNewEdge(int u, int v, int w) {
		// 기존 거리 행렬을 사용해 (u-v) 신규 링크를 경유하는 경로만 갱신
		int[] distU = Arrays.copyOf(dist[u], N + 1);
		int[] distV = Arrays.copyOf(dist[v], N + 1);

		for (int i = 1; i <= N; i++) {
			int diu = distU[i];
			int div = distV[i];
			if (diu >= CAP && div >= CAP) continue;
			for (int j = 1; j <= N; j++) {
				int cur = dist[i][j];
				int best = cur;

				int dvj = distV[j];
				if (diu < CAP && dvj < CAP) {
					int nd = diu + w + dvj;
					if (nd < best) best = nd;
				}
				int duj = distU[j];
				if (div < CAP && duj < CAP) {
					int nd = div + w + duj;
					if (nd < best) best = nd;
				}
				if (best < cur) dist[i][j] = best;
			}
		}
	}

	private void updateAllRatesAfterLink() {
		// 활성 요청에 대해 도달 가능한 소스 수를 재계산
		for (int i = 0; i < activeRequests.size(); i++) {
			Request r = activeRequests.get(i);
			if (r.completed) continue;
			int newCount = countSourcesWithin(r.com, r.fileIndex);
			if (newCount > r.rateSources) r.rateSources = newCount;
		}
	}

	private int countSourcesWithin(int com, int fileIdx) {
		// 해당 컴퓨터에서 거리 <= 5000인 공유 소스 수 계산
		FileInfo f = files[fileIdx];
		int count = 0;
		for (int i = 0; i < f.srcCount; i++) {
			int src = f.sources[i];
			if (dist[com][src] <= 5000) count++;
		}
		return count;
	}
}
