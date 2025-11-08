package study;

import java.io.IOException;
import java.util.*;

class UserSolution {
    static class Road
    {
        int id;            // id
        int time;        // 이동 시간
        int energy;        // 에너지
        int to_city;    // 목적지
        
        Road(int id, int time, int energy, int to_city)
        {
            this.id = id;
            this.time = time;
            this.energy = energy;
            this.to_city = to_city;
        }
    }
    
    static class City implements Comparable<City>
    {
        int id;
        int time;
        
        City(int id, int time)
        {
            this.id = id;
            this.time = time;
        }

        @Override
        public int compareTo(City o) {
            return Integer.compare(this.time, o.time);
        }
    }
    
    static class Car implements Comparable<Car>
    {
        int city;
        int time;
        int charge;
        
        Car(int city, int time, int charge)
        {
            this.city = city;
            this.time = time;
            this.charge = charge;
        }

        @Override
        public int compareTo(Car o) {
            return Integer.compare(this.time, o.time);
        }
        
    }
    
    static int[] mCharge;                    // 도시의 충전 속도 저장 리스트
    static List<Road>[] city_list;            // 도시 리스트
    static HashMap<Integer, Road> hashcity;        // 이름이 이상한데 도로 검색 및 지울때 사용할 hash
    static int[] Infested_city;        // 도시들이 감염된 타이밍 저장
    static int total_charge;        // 차 충전시 최대 충전 가능한량 저장
    static int[][] moveTime; // 다익 돌릴때 시간 저장 용 배열
    static int N;
    public void init(int N, int mCharge[], int K, int mId[], int sCity[], int eCity[], int mTime[], int mPower[]) {
        this.N = N;
        this.mCharge = new int[N];
        this.mCharge = mCharge;
        city_list = new List[N];
        Infested_city = new int[N];
        hashcity = new HashMap<>();
        
        //moveTime = new int[N][];
        for(int i=0; i<N; i++)
        {
            city_list[i] = new LinkedList<>();
        }
        
        for(int i=0; i<K; i++)
        {
            Road road = new Road(mId[i],mTime[i],mPower[i],eCity[i]);
            hashcity.put(mId[i], road);
            city_list[sCity[i]].add(road);
        }
        
        return;
    }

    public void add(int mId, int sCity, int eCity, int mTime, int mPower) {
        
        Road road = new Road(mId, mTime, mPower,eCity);
        hashcity.put(mId, road);
        city_list[sCity].add(road);
        
        return;
    }

    public void remove(int mId) {
        hashcity.remove(mId);
        return;
    }

    public int cost(int B, int sCity, int eCity, int M, int mCity[], int mTime[]) {
        moveTime = new int[N][B+1];
        Arrays.fill(Infested_city, 0);
        for(int i=0; i<N; i++)
        {
           Arrays.fill(moveTime[i], Integer.MAX_VALUE);
           
        }
        total_charge = B;
        Infested(mCity, mTime,M);

        return Dijkstra(sCity,eCity,B);
    }
    
    static int Dijkstra(int sCity, int eCity,int B)
    {
        PriorityQueue<Car> pq = new PriorityQueue<>();
        int result = Integer.MAX_VALUE;
        
        pq.add(new Car(sCity,0,B));
        moveTime[sCity][B] = 0;
        
        while(!pq.isEmpty())
        {
            Car car = pq.poll();
            if(moveTime[car.city][car.charge] < car.time) continue;
            
            for(Road road : city_list[car.city])    // 충전중 전염 병 만나는 경우 고려
            {
            	if(!hashcity.containsKey(road.id)) continue;		// 삭제된 도로 이동하지 않음
               int count =-1;
                for(int i=car.charge; i<=total_charge; i += mCharge[car.city])
                {
                   count++;
                   
                    if(road.time + car.time + count >= Infested_city[road.to_city]) break;		// 같은 시간인지 출발을 한건지 고민 해볼 필요가 있다.
                    
                    if(i >= road.energy)
                    {

                       if(moveTime[road.to_city][i-road.energy] <= road.time + car.time + count) continue;
                       moveTime[road.to_city][i-road.energy] = (int) (road.time + car.time + count);
                       
                       for(int j = i-road.energy-1; j>=road.energy; --j) {
                    	   if(moveTime[road.to_city][j] <= moveTime[road.to_city][i-road.energy]) break;
                    	   
                    	
                    	   moveTime[road.to_city][j] = moveTime[road.to_city][i-road.energy];
                       }

                       if(road.to_city == eCity) result =Math.min(result, moveTime[road.to_city][i-road.energy]);
                       else  pq.add(new Car(road.to_city,moveTime[road.to_city][i-road.energy],i - road.energy));
                      
                    }
                    
                    if(i!=total_charge && i + mCharge[car.city] > total_charge) i = total_charge -mCharge[car.city];
                }

            }
        }
        
        if(result == Integer.MAX_VALUE) result = -1;
       
        return result;
    }
    
    static void Infested(int mCity[],int mTime[],int M)
    {
        PriorityQueue<City> pq = new PriorityQueue<>();
        Arrays.fill(Infested_city, Integer.MAX_VALUE);
        
        for(int i=0; i<M; i++)
        {        
            pq.add(new City(mCity[i],mTime[i]));
            Infested_city[mCity[i]] = mTime[i];
        }
        
        while(!pq.isEmpty())
        {
            City city = pq.poll();
            
            if(Infested_city[city.id] < city.time) continue;
            
            for(Road road : city_list[city.id])
            {
            	if(!hashcity.containsKey(road.id)) continue;		// 삭제된 도로 이동하지 않음
            	
                if(Infested_city[road.to_city] <= road.time + city.time) continue;
                
                Infested_city[road.to_city] = road.time + city.time;
                pq.add(new City(road.to_city,Infested_city[road.to_city]));  // 다 만들어 놓고 pq 빼먹음
            }
        }
        
    }
    
}