
## 문제 조건

- 특이 사항

- 단어 작성
    - 하나의 단어를 적을 때 줄바꿈이 이루어지면 안된다.
    - 단어 작성의 우선 순위는 행의 번호, 열의 번호 순이다.
    - 암기장 내에 단어 길이만큼 공간이 남아 있지 않은 경우도 있다.

- 단어 삭제
    - 주어지는 mId값은 이전에 호출된 writeWord 함수에서 주어진 ID값보다 더 클 수도 있음.


## 문제 분석
- 단어를 저장하려고 하다보니 빈공간이 눈에 띄었고, 저번 문제와 똑같이 빈공간을 이용해서 TreeSet + PQ + Map으로 설계해봄.
- 우선 순위를 고려하는데 행,열을 기준으로 뽑아내더라도 길이가 맞지 않으면 그 다음 걸 poll해야하는 문제가 발생함.
- 이렇게 되면 최악의 경우엔 N의 순회가 발생할 것 같다..
- 어떤 알고리즘을 써야할지 모르겠어서 찾아봤습니다.

## 자료구조
```java
static class Word {
    int id;
    int left;
    int right;
    int len;
}
```

```java
static class Line {
    int rowIdx;
    int maxBlank;
    ArrayList<Word> words;
}
```

```java
static class Bucket {
    int idx;
    int startRow;
    int endRow;
    int maxBlank;
}
```

Line[] lines101
Bucket[] buckets;
Map<Integer, Integer> mIdToRowMap;

## 회고
- 버킷 알고리즘을 알게 되었다..
- 내일 다시 문제를 풀어보자..