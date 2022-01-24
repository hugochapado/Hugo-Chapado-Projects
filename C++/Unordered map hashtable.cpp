#include <iostream>
#include <string>
#include <vector>
#include <iomanip>
#include <cstring>
//using namespace std;

unsigned int hashFunction(char const* key, int table_size) {
    unsigned int hashcode=0;
    unsigned int b=0;
    //const char* convertedKey = key.c_str();
    //int aux = 2147483647;
    for(int i=0; i<strlen(key); i++){
        int asci = key[i];
        //std::cout<<i<<std::endl;
        if(i%2==0){
            b=(hashcode<<7)^asci^(hashcode>>3);
        }
        else{
            b=(hashcode<<11)^asci^(hashcode>>5);
            b=~b;
        }
        hashcode=hashcode^b;
    }
    hashcode << 1;
    hashcode >> 1;
    hashcode = hashcode % table_size;
    return hashcode;
}

//Create the Liked List we Will Use and the Node class for its use
class Node{
	public:
		std::string name;
		std::string id;
		Node* next;
		Node(std::string id){
			this->id=id;
			this->name="";
			this->next=nullptr;
		}
};

//List Data Structure For implementing the unordered map
class linkedlist{
	private:
		Node* root;
	public:
		//Constructor
        linkedlist(){
			root==nullptr;
		}
        int size=0;
        Node* insert(Node* root,std::string  id);
		bool deletenode(Node* root,std::string  id);
        Node* search(Node* root,std::string  id);	
        Node* getroot();
        void setroot(Node* r);
		
  


};
Node* linkedlist::getroot(){
    return root;
}
void linkedlist::setroot(Node* r){
    root=r;
}
Node* linkedlist::insert(Node* node, std::string  id ){
	
	Node* newNode= new Node(id);		
	if(node==nullptr){
		root=newNode;
        size++;
		return newNode;
	}	
	Node* temp=node;	
	while(temp->next != nullptr){
		temp = temp->next;
	}	
	temp->next = newNode;
    size++;
    return newNode;
}

bool linkedlist::deletenode(Node* node, std::string id){
	Node* temp = node;
	Node* temp2 = NULL;
	if(root == NULL){  //Empty List
		return false;
	}
	else if(root->next == nullptr){ //Only the root
		root=nullptr;
		delete temp;
        size--;
		return true ;
	}						
	while(temp != nullptr){		//More than 1 node list
		if (temp->id == id){
			temp2->next = temp->next;
            size--;
			delete temp;
            return true;
		}
		temp2 = temp,
		temp = temp->next;
	}	
    return false;
    
}
Node* linkedlist::search(Node* node, std::string  id){ 
    Node* temp = node;
    while (temp != nullptr)
    {
        if (temp->id == id){
            return temp;
        }
        temp = temp->next;
    }
    return nullptr;
}



class UnorderedMap 
{
    private:
        //define your data structure here
		std::vector<linkedlist*> map;
        //define other attributes e.g. bucket count, maximum load factor, size of table, etc. 
        int bCount;
        double maxLoadFactor;

    public:
        int keys=0;//Use to have a count for the number of keys
        class Iterator;
        UnorderedMap(unsigned int bucketCount, double loadFactor);
        ~UnorderedMap();
        Iterator begin() const;
        Iterator end() const;
        std::string& operator[] (std::string const& key);
        void rehash();
        void remove(std::string const& key);
        unsigned int size();
        double loadFactor();
        double loadFactor2();

        class Iterator 
        {    
            private:
                Node* node;
            
            public:
                //this constructor does not need to be a default constructor;
                //the parameters for this constructor are up to your discretion.
                //hint: you may need to pass in an UnorderedMap object.
                Iterator(UnorderedMap map){
                    
				};
				
                Iterator& operator=(Iterator const& rhs){ 
					//return pointer==rsh.pointer;
				};
				
				
                Iterator& operator++(){
                	//pointer++;
                	//return *this;
				};
				
                bool operator!=(Iterator const& rhs){
                	//if(pointer==rsh.pointer){
                	//	return false;
					//}
					//return true;
				};
				
                bool operator==(Iterator const& rhs){ 
						//if(pointer!=rsh.pointer){
                		//	return false;
						//}
					//return true;
				};
				
                std::pair<std::string, std::string> operator*() const{
                	//Returns the key/value pair at the iterator's current position
					//return   
				};
				
                friend class UnorderedMap;
        };
};

UnorderedMap::UnorderedMap(unsigned int bucketCount, double loadFactor) 
{
    
    for(int i=0; i < bucketCount; i++){
        map.push_back(new linkedlist());
    }
    bCount=bucketCount;
	maxLoadFactor=loadFactor;
	
	
}

UnorderedMap::~UnorderedMap() 
{
	map.clear();
}

UnorderedMap::Iterator UnorderedMap::begin() const 
{

    //return UnorderedMap::Iterator();
}

UnorderedMap::Iterator UnorderedMap::end() const 
{
	//return UnorderedMap::Iterator();
}

std::string& UnorderedMap::operator[] (std::string const& key) 
{
    std::string id=key;
    const char* convertedKey = key.c_str();
    unsigned int pos = hashFunction(convertedKey,bCount);
    Node* node = map[pos]->search(map[pos]->getroot() , id);
    if (node != nullptr){ //Search
        return node->name;
        std::cout<<"Search"<<std::endl;
    }
   else{ //Insert
        Node* node = map[pos]->insert(map[pos]->getroot() , id);
        if(loadFactor2() >= maxLoadFactor ){ //check if reash
            rehash();
        }         
        keys++;
          
        return node->name;
    }
    
}

void UnorderedMap::rehash() 
{   
    int oldCount = bCount;
    bCount *= 2;
    UnorderedMap newmap= UnorderedMap( bCount, maxLoadFactor); 
    for(int i = 0; i < oldCount ; i++){
        linkedlist* l = map[i];
        if(l->size!=0){
            Node* node = l->getroot();
            for(int j = 0; j < l->size ; j++){           
                //newmap[node->id] = node->name;
                node = node->next ;
            }
        }
    }
    map.clear();
    map=newmap.map;
    
}

void UnorderedMap::remove(std::string const& key) 
{
    std::string id = key;
    const char* convertedKey = key.c_str();
    unsigned int pos = hashFunction(convertedKey,bCount);  
    bool res = map[pos]->deletenode(map[pos]->getroot(),id);
    if(res==true){
        keys--;
    }
}

unsigned int UnorderedMap::size()
{
    //Number of keys
   return keys;
}

double UnorderedMap::loadFactor()
{
    double loadfactor = (double)size()/(double)bCount;
	return  loadfactor;
}
double UnorderedMap::loadFactor2()
{
    double loadfactor = (double)(size()+1)/(double)bCount;
	return  loadfactor;
}
//implement other operators in Iterator class

        




//Do not change main() 
int main()
{
    int lines = 0, buckets = 0;
    double maxLoadFactor = 0.0;
    std::string command = "", ufid = "", name = "", key = "";
    std::cin >> lines >> buckets >> maxLoadFactor;
    UnorderedMap myMap = UnorderedMap(buckets, maxLoadFactor);
    while(lines--)
    {
        std::cin >> command;
        if(command == "hash")
        {
            std::cin >> key;
            const char* convertedKey = key.c_str();
            std::cout << hashFunction(convertedKey, buckets) << "\n";
        }
        else if(command == "insert") 
        {
            std::cin >> ufid >> name;
            myMap[ufid] = name;
        }
        else if(command == "size") 
        {
            std::cout << myMap.size() <<"\n";
        }
        else if(command == "load") 
        {
            std::cout << std::fixed << std::setprecision(2) << myMap.loadFactor() <<"\n";
        }
        else if(command == "search")
        {
            std::cin >> ufid;
            std::cout << myMap[ufid] << "\n";
        }
        else if(command == "traverse")
        {
            for (UnorderedMap::Iterator iter = myMap.begin(); iter != myMap.end(); ++iter) 
            {
                std::cout << (*iter).first << " " << (*iter).second << "\n";
            }

            /* This should also work
                for (auto tableEntry: myMap) 
                {
                    std::cout << tableEntry.first << " " << tableEntry.second << "\n";
                }
            */
        }
        else if(command == "remove")
        {
            std::cin >> ufid;
            myMap.remove(ufid);
        }  
    }

    return 0;
}
