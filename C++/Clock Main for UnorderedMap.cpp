
#include <iostream>
#include <chrono>
using namespace std::chrono;       

typedef high_resolution_clock Clock;
using namespace std;

//Do not change main() 
int main()
{
     UnorderedMap myMap = UnorderedMap (5,0.80) ;
     UnorderedMap myMap2 =UnorderedMap (5,0.80) ;
     UnorderedMap myMap3 = UnorderedMap (5,0.80);
    //Insertion
    auto t1 = Clock::now();
    string name = "";
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        myMap[to_string(num)] = name;
       
    }
    auto t2 = Clock::now();
    std::cout << "Insertion (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap.size()<<endl;          
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
         myMap2[to_string(num)] = name;
    }
    t2 = Clock::now();
    std::cout << "Insertion (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap2.size()<<endl;         
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
         myMap3[to_string(num)] = name;
    }
    t2 = Clock::now();
    std::cout << "Insertion (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap3.size()<<endl;         
              t1 = Clock::now();
    //Search
     t1 = Clock::now();
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        myMap[to_string(num)];
    }
     t2 = Clock::now();
    std::cout << "Search (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap.size()<<endl;         
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
        myMap2[to_string(num)];
    }
    t2 = Clock::now();
    std::cout << "Search (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap2.size()<<endl;         
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
        myMap3[to_string(num)];
    }
    t2 = Clock::now();
    std::cout << "Search (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap3.size()<<endl;        
              t1 = Clock::now();
    
    //Remove
     t1 = Clock::now();
    for(int i=0;i<1000;i++){
        int num = rand()+1000000;
        myMap.remove(to_string(num));
    }
     t2 = Clock::now();
    std::cout << "Remove (1000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap.size()<<endl;         
    t1 = Clock::now();
    for(long ii=0;ii<10000;ii++){
        int num = rand()+1000000;
        myMap2.remove(to_string(num));
    }
    t2 = Clock::now();
    std::cout << "Remove (10000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
    cout<<"Map size: "<<myMap2.size()<<endl;           
              t1 = Clock::now();
    for(long ii=0;ii<100000;ii++){
        int num = rand()+1000000;
        myMap3.remove(to_string(num));
    }
    t2 = Clock::now();
    std::cout << "Remove (100000): " 
              << duration_cast<nanoseconds>(t2 - t1).count()
              << " nanoseconds" << '\n';
      cout<<"Map size: "<<myMap3.size()<<endl;         
              t1 = Clock::now();


    return 0;
}
