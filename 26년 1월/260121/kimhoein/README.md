# 2026년 1월 3주차 문제 풀이 업로드 폴더입니다.

금주 선정 문제는 다음과 같습니다.

## \[pro] 던전 탈출
기사가 던전에 떨어졌다

들어가는 게이트 나가는 게이트 한쌍이 존재한다

체력이 존재하는데 한칸 움직일때마다 체력 소모

체력은 들어가는 게이트 나가는 게이트를 제외한 나머지 게이트에서 충전 가능

n게이트에서 n2 게이트까지 간다고 할때 최소 거리는?
혹은 갈 수 있는지 없는지 판정?

### Solving Club -> 2026 SSAFY B형 스타티 :D

## 1. 문제 개요
* **목표:** 

n게이트에서 n2 게이트까지 간다고 할때 최소 거리는?
혹은 갈 수 있는지 없는지 판정?

add 200
remove 200
calculate 800

calculate는 빠르게
add, remove는 무겁게? 둘중 하나가 느려야 한다면 add나 
remove가 하자

add를 할때 미리 계산하고
remove를 할때 계산하지 않고 link를 끊거나 flag를 줘서 처리한다면?

미리 계산한거를 bfs로 돌리자? -> 가중치가 있는 간선 -> 다익스트라
bfs로 간선 추가하고 다익스트라로 최종적으로 위치를 구하자

* **제약 사항:**
java 3초
체력 존재
id는 한칸씩 오른다

## 2. 자료구조 및 알고리즘 설계

static int mMaxStamin;   // 스테미나
static int mMap[][];     // 맵 어떻게 생겼는지 저장
static Node gate_Map[][];  // 맵에 존재하는 gate
static int N;           // N은 맵의 크기
static Node gate[];     // 게이트 저장
static int mIdCount=0;  // 게이트 개수


static HashMap<Integer, Node> hash; // 저장된 hash
static ArrayList<ArrayList<Mode>> list; // 다익용
static int delta[][] = {{1,0},{0,1},{-1,0},{0,-1}};

## 3. 핵심 로직 흐름

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

init
초기화

add
hash 추가에 추가 해둠

remove
hash 추가에 있는지 확인 후 있으면 삭제 
hash 추가에 없다면?
gate 찾아서 remove index


connect_gate
만약 다익스트라 돌릴때
hash 추가에 뭐 하나라도 존재한다면 이걸로 bfs 돌리면서 간선 추가

getMinTime
다익스트라 돌리면서 값 찾기
