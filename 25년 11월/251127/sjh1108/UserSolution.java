import java.util.*;

class UserSolution {
	private static final int PAGE_SIZE = 100;
	private static final int CALL_WRITE = 50000;
	private static final int MAX_LINE = 20000;

	static class Node{
		int id;
		int start;
		int length;
		Node prev;
		Node next;

		public Node(int id, int start, int length) {
            this.id = id;
            this.start = start;
            this.length = length;
        }

		public void init(int id, int start, int length) {
            this.id = id;
            this.start = start;
            this.length = length;
            this.prev = null;
            this.next = null;
		}
	}

	static class Line{
		Node head;
		Node tail;
		int maxBlank;

		public Line(){
			head = new Node(-1, -1, 0);
            tail = new Node(-1, -1, 0);

			head.next = tail;
			tail.prev = head;
		}

		void clear(int M){
			head.next = tail;
			tail.prev = head;
			maxBlank = M;
		}

		void insert(Node cur, int id, int start, int len){
			Node newNode = new Node(id, start, len);
            
            newNode.next = cur;
            newNode.prev = cur.prev;
            cur.prev.next = newNode;
            cur.prev = newNode;
		}

		void erase(Node cur){
			Node prev = cur.prev;
			Node next = cur.next;

			prev.next = next;
			next.prev = prev;
		}
	}

	private static int[] id2Line = new int[CALL_WRITE + 1];

	static class Page{
		int p_line_start;
		int p_line_end;
		int b_line_count;
		int p_maxBlank;
		int p_line_width;

		Line[] lines = new Line[PAGE_SIZE];
		
		public Page(){
			for(int i = 0; i < PAGE_SIZE; i++){
				lines[i] = new Line();
			}
		}

		void init(int index, int N, int M){
			p_line_start = index * PAGE_SIZE;
			p_line_end = Math.min(p_line_start + PAGE_SIZE, N);
			b_line_count = p_line_end - p_line_start;
			p_maxBlank = M;

			for(int i = 0; i < b_line_count; i++){
				lines[i].clear(M);
			}

			p_line_width = M;
		}

		void updateLineMaxBlank(int lineIndex){
			Line curLine = lines[lineIndex];

			int maxGap = 0;
			int start = 0;
			Node cur = curLine.head.next;

			while(cur != curLine.tail){
				int gap = cur.start - start;
				if(gap > maxGap){
					maxGap = gap;
				}
				start = cur.start + cur.length;
				cur = cur.next;
			}

			int gap = p_line_width - start;
			maxGap = Math.max(maxGap, gap);
			curLine.maxBlank = maxGap;
		}

		boolean insertWord(int lineIndex, int mId, int mLen){
			Line curLine = lines[lineIndex];

            int start = 0;
            int ret = -1;
            Node cur = curLine.head.next;

            while (cur != curLine.tail) {
                int gap = cur.start - start;
                if (gap >= mLen) {
                    curLine.insert(cur, mId, start, mLen);
                    ret = start;
                    id2Line[mId] = p_line_start + lineIndex;
                    break;
                }
                start = cur.start + cur.length;
                cur = cur.next;
            }

            if (ret == -1) {
                int gap = p_line_width - start;
                if (gap >= mLen) {
                    curLine.insert(curLine.tail, mId, start, mLen);
                    ret = start;
                    id2Line[mId] = p_line_start + lineIndex;
                }
            }
            return ret != -1;
		}
		
		boolean findAndErase(int lineIndex, int mId) {
            Line curLine = lines[lineIndex];
            boolean ret = false;
            Node cur = curLine.head.next;

            while (cur != curLine.tail) {
                if (cur.id == mId) {
                    ret = true;
                    curLine.erase(cur);
                    break;
                }
                cur = cur.next;
            }
            return ret;
        }

		void updateBucketMaxBlank() {
            p_maxBlank = 0;
            for (int i = 0; i < b_line_count; i++) {
                p_maxBlank = Math.max(lines[i].maxBlank, p_maxBlank);
            }
        }

		int writeWord(int mId, int mLen) {
            int ret = -1;
            for (int i = 0; i < b_line_count; i++) {
                if (lines[i].maxBlank >= mLen && insertWord(i, mId, mLen)) {
                    updateLineMaxBlank(i);
                    ret = i + p_line_start;
                    break;
                }
            }
            updateBucketMaxBlank();
            return ret;
        }

        int eraseWord(int mId, int lineIdx) {
            int ret = -1;
            if (findAndErase(lineIdx, mId)) {
                updateLineMaxBlank(lineIdx);
                ret = lineIdx + p_line_start;
            }
            updateBucketMaxBlank();
            return ret;
        }
	}

	static int cntPages;
	static Page[] pages = new Page[MAX_LINE / PAGE_SIZE + 1];
	
	static{
		for(int i = 0; i < pages.length; i++){
			pages[i] = new Page();
		}
	}

	public void init(int N, int M)
	{
		cntPages = (N + PAGE_SIZE - 1) / PAGE_SIZE;

		for(int i = 0; i < cntPages; i++){
			pages[i].init(i, N, M);
		}

		Arrays.fill(id2Line, -1);
	}

	public int writeWord(int mId, int mLen)
	{
		int ret = -1;
		for(int i = 0; i < cntPages; i++){
			if(pages[i].p_maxBlank >= mLen){
				ret = pages[i].writeWord(mId, mLen);
				if(ret != -1){
					return ret;
				}
			}
		}

		return ret;
	}

	public int eraseWord(int mId)
	{
		int ret = -1;

		if(mId < CALL_WRITE && id2Line[mId] != -1){
			int lineIdx = id2Line[mId];
			int pageIdx = lineIdx / PAGE_SIZE;
			int internalLineIdx = lineIdx % PAGE_SIZE;

			ret = pages[pageIdx].eraseWord(mId, internalLineIdx);
			if(ret != -1){
				id2Line[mId] = -1;
			}
		}

		return ret;
	}

}