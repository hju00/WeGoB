# 1. 핵심 자료구조 설계

## 1. Node (단어)
* LinkedList 구현체를 위한 노드 클래스
* 단어의 ID, 시작 위치, 길이를 저장
* 단어의 삽입과 삭제가 빈번하게 발생하기 때문에, 노드 간 연결만 변경하여 `O(1)`에 처리하기 위함

## 2. Line (한 줄)
* LinkedList 구현체
* Node 객체를 LinkedList 형태로 관리하는 컨테이너
* 해당 라인에서 가장 긴 빈 공간의 길이를 저장하여 탐색의 속도를 높힘

## 3. Page (여러 줄의 묶음, Bucket)
* 여러 개의 줄을 묶은 그룹
* 해당 페이지에 속해 있는 Line 클래스들의 maxBalnk 데이터

# 2. 최적화 전략

## Write
1. Page 단위 건너뛰기
    * 입력하려는 단어 길이가 L일 때, Page의 p_maxBlank가 L보다 작다면 해당 페이지를 건너 뜀
2. Line 건너 뛰기
    * maxBlank가 L보다 작다면 해당 줄 건너 뜀

## Erase
1. id2Line[ID] 배열 사용하여 페이지, 줄에 있는지 확인함
2. 해당 라인으로 이동하여 삭제함

# 3. 알고리즘 흐름 요약

## 1. 단어 입력
1. Page 순회하여 Page.p_maxBlank >= L인 페이지를 찾음
2. 찾은 페이지 내에서 Line을 순회하며 Line.maxBlank >= L 인 라인을 찾음
3. 해당 라인의 Linked List를 순회하며 실제 들어갈 위치를 찾아 노드를 삽입
4. 삽입 후 해당 Line과 Page의 maxBlank를 갱신함
5. id2Line 배열에 위치 정보 기록

## 2. 단어 삭제
1. id2Line 배열을 통해 해당 단어가 위치한 Page와 Line 인덱스 조회
2. 해당 Line의 Linked List에서 ID가 일치하는 노드를 찾아 삭제함
3. 삭제 후 Line과 Page의 maxBlank 갱신
4. id2Line 정보 초기화