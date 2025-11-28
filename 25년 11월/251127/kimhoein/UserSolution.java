package samsung01;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

class UserSolution {
    class Space implements Comparable<Space>
    {
        int hight;
        int row;
        int d;
        
        Space(int hight, int row, int d)
        {
            this.hight = hight;
            this.row = row;
            this.d = d;
        }

        @Override
        public int compareTo(Space o) {
            if(this.hight == o.hight)
            {
                return this.row - o.row;
            }
            return this.hight - o.hight;
        }
        
         @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Space)) return false;
            Space other = (Space) obj;
            return this.hight == other.hight && this.row == other.row;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(hight) * 31 + Integer.hashCode(row);
        }
    }
    
    TreeSet<Space>[] bucket;
    TreeSet<Space>[] erase_tree;
    HashMap<Integer, Space> word;
    static int N,M;
    
    public void init(int N, int M)
    {
        bucket = new TreeSet[11];
        erase_tree = new TreeSet[N];
        word = new HashMap<>();
        this.N= N;
        this.M = M;
        
        for(int i=0; i<11; i++)
        {
            bucket[i] = new TreeSet<>();
        }
        
        for(int i=0; i<N; i++)
        {
            erase_tree[i] = new TreeSet<>();
            Space space = new Space(i, 0, M);
            erase_tree[i].add(space);
            bucket[space.d/100].add(space);
        }
        
        
    }

    public int writeWord(int mId, int mLen)
    {
        int h_min = N+1;            // 초기 값 주어서 어떤 값이 들어와도 업데이트 가능하도록
        int count = -1;                // mLen 보다 작은게 있는지 없는지 체크용 및 어느라인이 가장 작은 곳 인지 저장
                                    // class인 space를 쓰면 하나로 가능한데 자꾸 변수명이 너무 길어져서
                                    // 구현난이도가 상승하는 문제가 있어 고냥 이렇게 했습니다.
        Space min = null;
        for(int i=mLen/100; i<11; i++)
        {
            if(bucket[i].isEmpty()) continue;
            Space bhight = bucket[i].first();
            
            //if(mId == 10) System.out.println("i : " + i + " " + mLen + " " + bhight.d + " " + bhight.hight);
            //System.out.println(mLen + " " + bhight.d + " " + bhight.hight);
            
            //System.out.println("bucket[i].isEmpty() : " + bucket[i].isEmpty());
            while(mLen > bhight.d && bucket[i].higher(bhight) != null)
            {
                bhight = bucket[i].higher(bhight);
            }
            
            //if(mId == 10) System.out.println(mLen + " " + bhight.d + " " + bhight.hight);
            
            if(mLen <= bhight.d && h_min > bhight.hight) {
                h_min = bhight.hight;
                min = bhight;
                count = i;
            }
        }
        
        if(count == -1) return -1;
        
        //Space min = 
        Space w = new Space(min.hight, min.row, mLen);
//        System.out.println("bucket[min.d/100]. : " + bucket[min.d/100].size());
        bucket[min.d/100].remove(min);
//        System.out.println("bucket[min.d/100]. : " + bucket[min.d/100].size());
        
//        System.out.println("erase_tree[min.hight] : " + erase_tree[min.hight].size());
        erase_tree[min.hight].remove(min);
//        System.out.println("erase_tree[min.hight] : " + erase_tree[min.hight].size());
        
        if(min.d != mLen)
        {
            min.d = min.d - mLen;
            min.row += mLen;
            bucket[min.d/100].add(min);
            erase_tree[min.hight].add(min);
        }
        word.put(mId, w);
        return w.hight;
    }

    public int eraseWord(int mId)
    {
        if(word.containsKey(mId))
        {
            Space s = word.get(mId);
            word.remove(mId);
            
            Space left = erase_tree[s.hight].lower(s);
            Space right = erase_tree[s.hight].higher(s);
            
            
            if(left != null && left.row + left.d == s.row) {
                s.d += left.d;
                s.row = left.row;
                erase_tree[s.hight].remove(left);
                bucket[left.d/100].remove(left);
            }
            
            if(right != null && s.row + s.d == right.row) {
                s.d += right.d;
                erase_tree[s.hight].remove(right);
                bucket[right.d/100].remove(right);
            }
            
            //if(s.hight == 2) System.out.println("aa" + s.hight + " " + s.row + " " + s.d + " " + bucket[s.d/100].first().hight + " " + bucket[s.d/100].first().d);
            
            bucket[s.d/100].add(s);
            erase_tree[s.hight].add(s);
            //if(s.hight == 2) System.out.println("aa" + s.hight + " " + s.row + " " + s.d + " " + bucket[s.d/100].first().hight + " " + bucket[s.d/100].first().d);
            
            return s.hight;
        }
        
        return -1;
    }

}