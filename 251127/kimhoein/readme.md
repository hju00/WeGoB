# 11월 4주차 문제 풀이 업로드 폴더입니다.
금주 선정 문제는 다음과 같습니다.
## [Pro] 단어암기장
### Code Battle -> 25년 SSAFY Pro 연습 추가


토지 내에서 가장 긴 빈공간을 선택한다.
가장 긴 빈공간이 여러 개라면 가장 왼쪽에 위치한 빈공간을 선택한다.

-> pq에 대한 조건

list가 편하지 않을까? -> hashmap으로 빠르게 찾자

빌딩을 지우고 나면 빈 공간과 통합 해줘야 한다
-> 좌우 hashmap으로 빈공간 찾고 합치기


# init

static n에 길이 저장

# build
priotyque pq(길이, 시작점)에서 가장 큰거 하나 꺼내서 중앙에 넣어줌

pq 길이 > 지어야 할 빌딩 길이

시작점 0 길이 11
11-9/2

시작점 끝점

9
3

9-3/2 따라서3 부터 +1
10-3/2 따라서 3부터 + 1
11-3/2 따라서 4 부터 + 1

길이-넣어줄 build의 길이 + 1 이 시작 값


빈 공간 hash.get(시작점) 제거
빈공간 끝 hash.put(끝점, 길이)

빈공간중 앞공간
시작점은 처음 시작점 그대로 가져올 것
(빈공간 길이- 빌딩 길이) /2 -> 빈공간 중 앞공간의 길이

빈 공간 중 앞 공간 hash.put(시작점, 빈공간 중 앞공간의 길이)

빈공간 pq 에 시작점, 길이 추가

빈공간 중 뒷 공간
시작점 = 빈공간 중 앞공간의 길이 + 빌딩 길이 
길이 = (빈공간 길이- 빌딩 길이) /2 + 빈공간 길이- 빌딩 길이) %2
빈공간 시작 hash.put(시작점,길이)
빈공간 끝 hash.put(끝점, 길이)


빈공간 pq 에 시작점, 길이 추가2

공간 있는 hash.put(빌딩 시작점, 빌딩 길이)

# demolish
제거 하는 부분

if hash.contina(mAddr) 
공간 있는 hash.remove

int 총길이 

if(빈공간 시작 hash. contain(mAddr+제거된 빌딩 길이))		// 만약 빌딩 오른쪽 부분에 빈공간 존재한다면 미리길이 값 받아오고 제거한다.
총길이 +=  hash. get(mAddr+제거된 빌딩 길이)
빈공간 끝 hash.remove(빈공간 시작의 시작점 + 빈공간 시작 push의 get)
빈공간 시작 hash.remove(mAddr+제거된 빌딩 길이)

if(빈공 끝 hash.get(mAddr))
	hash.put(key값 그대로, 총길이 + hash.get(mAddr))
else		// 이러면 시작 값이 빌딩이 된다.
빈공 시작 hash.put(mAddr)

return 제거할 빌딩의 주소

else
return -1


pq 를 같이 제거 해줘야함
demolish할때
hashmap 삭제시 같이


