## \[pro] 고대 통신망망
통신망에서 최소의 거리를 찾으며 가장 길지 않은 길을 찾을때때

### Solving Club -> 2026 SSAFY B형 스타티 :D

## 1. 문제 개요
* **목표:** 

node 0 ~ n-1

다익스트라
add 최대 15000번
erase 1000번

calculate 5000번

* **제약 사항:**
java 3초


## 2. 자료구조 및 알고리즘 설계

private int n;
private int capital;

private List<List<Edge>> graph;
private HashMap<Integer, Edge> idToEdge;

private int[] distSum;
private int[] distMax;
private Edge[] parentEdge;

private boolean dirty;

private Queue<State> pq;
    

## 3. 핵심 로직 흐름

처음 실행시 다익스트라 돌려서 모든 상황 체크

add시 이전 보다 길이 개선이 되었는지 되지 않았는지 체크
개선 되었다면 다익스트라

erase시 dirty = true로 
계산 실행시 업데이트 실행

