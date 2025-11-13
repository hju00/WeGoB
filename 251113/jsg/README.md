## 아이디어
동맹을 union-find를 이용해 구현하자.
전투가 일어날 때마다 방어 지점의 8방을 조회해 공격팀,방어팀을 구해 계산하자.


## 자료구조
HashMap<String, Integer> nameToId: 이름으로 영토 ID 저장
int[] parents: 동맹의 root를 저장
Set<Integer>[] enemies: 적대 관계를 동맹의 root로 각 칸에 저장
int[][] lordIds: 각 칸의 군주 ID 저장