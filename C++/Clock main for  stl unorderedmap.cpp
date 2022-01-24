
#include <iostream>
#include <chrono>
#include <unordered_map>
#include <string>
using namespace std::chrono;       

typedef high_resolution_clock Clock;
using namespace std;

//Do not change main() 
int main()
{
    unordered_map<string,string> map;
    unordered_map<string,string> map2;
    unordered_map<string,string> map3;
    //Insertion
    auto t1 = Clock::now();
    string name = "";
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        map[to_string(num)] = name;
       
    }
    auto t2 = Clock::now();
    std::cout << "Insertion (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
   // cout<<"Map size: "<<myMap.size()<<endl;          
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
         map2[to_string(num)] = name;
    }
    t2 = Clock::now();
    std::cout << "Insertion (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap2.size()<<endl;         
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
          map3[to_string(num)] = name;
    }
    t2 = Clock::now();
    std::cout << "Insertion (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap3.size()<<endl;         
              t1 = Clock::now();
    //Search
     t1 = Clock::now();
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        map[to_string(num)];
    }
     t2 = Clock::now();
    std::cout << "Search (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap.size()<<endl;         
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
        map2[to_string(num)];
    }
    t2 = Clock::now();
    std::cout << "Search (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap2.size()<<endl;         
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
        map3[to_string(num)];
    }
    t2 = Clock::now();
    std::cout << "Search (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap3.size()<<endl;        
              t1 = Clock::now();
    
    //Remove
     t1 = Clock::now();
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        map.erase(to_string(num));  
    }
     t2 = Clock::now();
    std::cout << "Remove (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
   // cout<<"Map size: "<<myMap.size()<<endl;         
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
        map2.erase(to_string(num)); 
    }
    t2 = Clock::now();
    std::cout << "Remove (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    //cout<<"Map size: "<<myMap2.size()<<endl;           
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
        map3.erase(to_string(num)); 
    }
    t2 = Clock::now();
    std::cout << "Remove (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
      //cout<<"Map size: "<<myMap3.size()<<endl;         
              t1 = Clock::now();


    return 0;
}
