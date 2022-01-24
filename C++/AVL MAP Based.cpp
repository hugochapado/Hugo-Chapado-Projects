



//Add your peer's code for the AVL Tree
#include <iostream>
#include <string>
#include <sstream>
using namespace std;
//here is the node structure, these are the nodes that the entire tree will be composed of
struct node {
    int studentID;
    string studentName;
    struct node* left;
    struct node* right;
};
//here is the class for the AVL tree, it has all of the functions neccessary to modify and balance the tree
class avlTree {
    private:
    int getHeight(node*);
    int checkBalance(node*);
    node* balance(node*);
    node* rightRotation(node*);
    node* leftRotation(node*);
    node* rightLeftRotation(node*);
    node* leftRightRotation(node*);
    node* leftMostNode(node*);
    void removeInorder(node*);
    public:
    string order="";
    int i=0;
    struct node* root;
    int countNodes(node*);
    node* searchID(node*, int);
    bool searchName(node*, string);
    void printInorder(node*);
    void printPreorder(node*);
    void printPostorder(node*);
    void printLevelCount(node*);
    node* insert(node*, string, int);
    node* remove(node*, int);
    void findInorder(node*, node*, int);
    //void setroot(node* );
    avlTree(){
        this->root = NULL;
    }
    
    
};
//void avlTree::setroot(node* Node){
  //  root=Node;
//}

//getHeight is used on any given Node in the tree to obtain its height using recursion and max()
int avlTree::getHeight(node* Node){
    int height=0;
    if(Node != NULL){
        height=max(getHeight(Node->left), getHeight(Node->right));
        height++;
    }
        return height;
}
//checkBalance will return the difference in height between the two child nodes of a node
int avlTree::checkBalance(node* Node){
    return getHeight(Node->left) - getHeight(Node->right);
}
node* avlTree::balance(node* Node){
    int nodeBalance = checkBalance(Node);
    if(nodeBalance > 1){
        if(checkBalance(Node->left) > 0)
           Node = rightRotation(Node);
        else
           Node = leftRightRotation(Node);
    }
    else if(nodeBalance < -1){
        if(checkBalance(Node->right) <= 0)
            Node = leftRotation(Node);
        else
            Node = rightLeftRotation(Node);
    }
    return Node;
}
node* avlTree::rightRotation(node* Node){
    node* temp = Node->left;
    Node->left = temp->right;
    temp->right = Node;
    return temp; 
}
node* avlTree::leftRotation(node* Node){
    node* temp = Node->right;
    Node->right = temp->left;
    temp->left = Node;
    return temp; 
}
node* avlTree::rightLeftRotation(node* Node){
    Node->right = rightRotation(Node->right);
    return leftRotation(Node);
}
node* avlTree::leftRightRotation(node* Node){
    Node->left = leftRotation(Node->left);
    return rightRotation(Node);
}
//if the ID is not within the tree, NULL will be returned. This method is written this way so that it could be used in conjunction with remove
node* avlTree::searchID(node* rootNode, int ID){
    if(rootNode == NULL)
        return NULL;
    else if(rootNode->studentID < ID)
        return searchID(rootNode->right, ID);
    else if(rootNode->studentID > ID)
        return searchID(rootNode->left, ID);
    else 
        return rootNode;
}
//searchName is a bit different from searchID because there can be many of the same name in the tree
bool avlTree::searchName(node* rootNode, string name){
    bool found= false;
    bool found2= false;
    bool found3= false;
    if(rootNode != NULL){
        if(rootNode->studentName == name){
            cout << rootNode->studentID << endl;
            found = true;
        }
        //return (found || searchName(rootNode->left, name) || searchName(rootNode->right, name));
        found2 = searchName(rootNode->left, name);    
        found3 = searchName(rootNode->right, name);
        return found || found2 || found3;
    }
    return found;
}
//this method allows me to find out if the removeInorder function goes out of bounds
int avlTree::countNodes(node* rootNode){
    if(rootNode == NULL)
        return 0;
    else
        return 1+countNodes(rootNode->left)+countNodes(rootNode->right);
}
//these next three methods print the tree in different ways
void avlTree::printInorder(node* rootNode){
    if(rootNode == NULL)
        return;
    printInorder(rootNode->left);
    order+= rootNode->studentName + ", ";
    printInorder(rootNode->right);
}
void avlTree::printPreorder(node* rootNode){
    if(rootNode == NULL)
        return;
    order+= rootNode->studentName + ", ";
    printPreorder(rootNode->left);
    printPreorder(rootNode->right);
}
void avlTree::printPostorder(node* rootNode){
    if(rootNode == NULL)
        return;
    printPostorder(rootNode->left);
    printPostorder(rootNode->right);
    order+= rootNode->studentName + ", ";
}
void avlTree::printLevelCount(node* rootNode){
    cout << getHeight(rootNode) << endl;
}
//this method finds where the next node should go
node* avlTree::insert(node* rootNode, string name, int ID){
    if(rootNode == NULL){
        rootNode = new node;
        rootNode->studentName = name;
        rootNode->studentID = ID;
        rootNode->left=NULL;
        rootNode->right=NULL;
        //cout << "successful" << endl; eliminated succesfull message
    }
    else if(ID < rootNode->studentID){
        rootNode->left = insert(rootNode->left, name, ID);
        rootNode=balance(rootNode);
    }
    else{
        rootNode->right = insert(rootNode->right, name, ID);
        rootNode=balance(rootNode);
    }
    
    return rootNode;
}
//this method removes a node at a given ID
node* avlTree::remove(node* rootNode, int ID){
    if(rootNode == NULL){
        return NULL;
    }
    if(rootNode->studentID==ID){
         node* temp = rootNode;
            if(temp->left == NULL && temp->right == NULL){
                rootNode = NULL;
                //cout << "successful" << endl;
            }
            else if(temp->left == NULL && temp->right != NULL){
                rootNode = temp->right;
                //cout << "successful" << endl;
            }
            else if(temp->left != NULL && temp->right == NULL){
                rootNode = temp->left;
                //cout << "successful" << endl;
            }
            else{
                node* replacement = leftMostNode(temp->right);
                rootNode->studentID = replacement->studentID;
                rootNode->studentName = replacement->studentName;
                rootNode->right = remove(rootNode->right, replacement->studentID);
            }
    }
    else{
        rootNode->left = remove(rootNode->left, ID);
        rootNode->right = remove(rootNode->right, ID);
    }
    return rootNode;
}
node* avlTree::leftMostNode(node* rootNode){
    if(rootNode->left == NULL)
        return rootNode;
    else
        return leftMostNode(rootNode->left);
}
//this method actually just gets the ID of the node to remove and then sends it to the remove method so that It can remove it
void avlTree::findInorder(node* rootNode, node* nextNode, int n){
    if(nextNode == NULL)
       return;
    findInorder(rootNode, nextNode->left, n);
    if(i == n)
        removeInorder(nextNode);
    i++;
    findInorder(rootNode, nextNode->right, n);
}
void avlTree::removeInorder(node* Node){
    root = remove(root, Node->studentID);
}


class OrderedMap 
{
    private:
        //create an object of the AVL Tree which your peer implemented 
        avlTree* avl;

    public:
        OrderedMap();
        ~OrderedMap();
        bool insert(const std::string ID, const std::string NAME);
        std::string search(const std::string ID);
        std::string traverse();
        bool remove(const std::string ID);
};


OrderedMap::OrderedMap()
{
    //code here
    avl=new avlTree;
    
    
}

OrderedMap::~OrderedMap()
{
    //code here
    avl->root=NULL;
   
}

bool OrderedMap::insert(const std::string ID, const std::string NAME)
{
    //code here
    int id=stoi(ID);
    avl->root=avl->insert(avl->root,NAME,id);
    
    return true;
}

std::string OrderedMap::search(const std::string ID)
{
    //code here
    int id=stoi(ID);
    node* NODE=avl->searchID(avl->root,id);
    if (NODE==NULL){
        return "";
    }
    else{
        return NODE->studentName;
    }
}

std::string OrderedMap::traverse()
{
    //code here
   avl->order="";
   avl->printPreorder(avl->root);
   return avl->order.substr(0,avl->order.length()-2);

}

bool OrderedMap::remove(const std::string ID)
{
    //code here
    int id=stoi(ID);
    node* NODE=avl->remove(avl->root, id);
    if(NODE==NULL){
        return false;
    }
    return true;
}

//Do not change main() 
int main()
{
    OrderedMap myMap;
    int lines = 0;
    std::string command = "", ufid = "", name = "";
    std::cin >> lines;
    while(lines--)
    {
        std::cin >> command;
        if(command == "insert") 
        {
            std::cin >> ufid >> name;
            std::cout << myMap.insert(ufid, name) << "\n";
        }
        else if(command == "search")
        {
            std::cin >> ufid;
            std::cout << myMap.search(ufid) << "\n";
        }
        else if(command == "traverse")
        {
            std::cout << myMap.traverse() << "\n";
        }
        else if(command == "remove")
        {
            std::cin >> ufid;
            std::cout << myMap.remove(ufid) << "\n";
        }  
    }

    return 0;
}
