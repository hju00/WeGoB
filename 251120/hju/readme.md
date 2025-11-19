# [pro] 삼국지게임 문제 풀이

## 문제 분석

크게 2가지 기능

1. 빌딩 짓기
- 빌딩의 주소는 빌딩 가장 왼쪽의 셀의 주소
- 빌딩은 토지에서 가장 긴 빈공간에 지음
- 가장 긴 빈공간이 여러 개 이면 가장 왼쪽에 위치한 빈공간에 지음
- 빌딩은 선택된 빈공간 내에서 가운데 위치에 건설
- 빌딩을 지었을 때 새로 생기는 왼쪽과 오른쪽의 빈공간의 길이는 같거나, 오른쪽의 빈공간의 길이가 1 더 커야함

2. 빌딩 제거
- 셀의 주소가 주어지면 동일한 주소를 가진 빌딩 제거
- 제거된 빌딩의 길이를 반환
- 빌딩을 제거 못한 경우 -1 반환

## 알고리즘 선정
**가장 긴** 빈공간? -> 정렬 자료구조 쓰고싶음<br>
빌딩 관리 -> Map<빌딩 주소, 빌딩> 쓰면 될 것같은디<br>
추가) 생각해보니까 빌딩을 제거하면 새로운 빈공간을 빠르게 계산하기 위해서 정렬된 빌딩들도 필요할 듯

빌딩 짓기 {<br>
1. spaces에서 가장 길고 가장 왼쪽의 빈공간 추출 (정렬되어있으니까 first 확인하면됨)
2. if(빌딩 길이 > spaces.first.length) -> return -1
3. spaces.remove(first) - O(log n)
4. buildingDB 빌딩 추가
5. buildings 빌딩 추가 - O(log n)
6. spaces에 새로 생기는 공간 추가 (0개, 1개, 2개 중 하나) - {0, 1, 2} X O(log n)
7. return 빌딩 주소
<br>}

빌딩 제거 {<br>
1. DB에 주소 조회
2. null 인 경우 retrun -1
3. DB에서 빌딩 제거
4. buildings에서 빌딩 제거
5. 새로 생기는 빈공간 추가
6. buildings 를 이용해서 양옆의 다른 building 찾기(lower, higher) - 2 X O(log n)
7. 양옆의 다른 building을 이용해서 spaces에 존재하는 빈공간을 제거 - 2 X O(log n)
8. 양옆의 다른 building을 이용해서 새로운 빈공간 spaces에 추가 - O(log n)
9. return 제거한 빌딩 길이
<br>}

## 시간 복잡도
- init() X 1
- build() X 20,000 = (3 ~ 5) X O(log n) X 20,000 = (24 ~ 40) X 20,000 = 480,000 ~ 800,000
- demolish() X 6,000 = (4 ~ 6) X O(log n) X 6,000 = (32 ~ 48) X 6,000 = 192,000 ~ 288,000
- 최대 연산 시 1,088,000 정도

## 자료 구조
    HashMap<Integer, Building> buildingDB
    TreeSet<Space> spaces
    TreeSet<Building> buildings
    
    class Building implements Comparable<Building>{
        int s, e;
        int length;

        int compareTo(Building o) {
            return Integer.compare(this.s, o.s);
        }
    }

    class Space implements Comparable<Space>{
        int s, e;
        int length;

        int compareTo(Space o) {
            if(this.length == o.length)
                return Integer.compare(this.s, o.s);
            return Integer.compare(o.length, this.length);
        }
    }
