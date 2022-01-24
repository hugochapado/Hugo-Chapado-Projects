#include<iostream>
#include<string>
#include<vector>
#include<stdio.h>
#include <queue> 
#include <string.h>


using namespace std;


class Student{
	private: 
		int id;
		string name;
	public:
		void setID(int d){  //Use Get and Set functions to acces the student private data from AVL
			id=d;
		}
		int getID(){
			return id;
		}
		void setname(string n){
			name=n;
		}
		string getname(){
			return name;
		}
};

class TreeNode {
   public:
      int id;
      Student student;
	  int height;
      TreeNode *left, *right;
      TreeNode() : id(0),height(1), left(nullptr), right(nullptr) {}
      TreeNode(int x) : id(x),height(1), left(nullptr), right(nullptr) {}
      
};

class AVL{
	private://Atributes
		TreeNode* root;
        
	public:
		AVL(){//Defaultconstructor	
			root=NULL;	
		}
		string order="";
        int countt = 0;
		//Functions of height and Balance Factor
		int heightt(TreeNode* node){//Function to Calculate the height of a Given Node
            int height=0;
            if(node != NULL){
                height=max(heightt(node->left), heightt(node->right));
                height++;
            }
            return height;     
		} 
		int balancefactor(TreeNode* node){//Function that Obtains the balance factor of a node
                
				return heightt(node->left) - heightt(node->right);
		}
		
		
		//Now lets create Funtions that make the rotations of the tree
		
			
	    TreeNode* leftleftrotation(TreeNode* node){//Right case:Left Left Rotation
	      	TreeNode* temp=node->left->right;
	 		TreeNode* newParent=node->left;
	 		newParent->right=node;
            node->left=temp;
	 		return newParent;
	    }
	    TreeNode* rightrightrotation(TreeNode* node){ //Left case:Right Right Rotation
	 		TreeNode* temp=node->right->left;
	 		TreeNode* newParent=node->right;
	 		newParent->left=node;	
            node->right=temp;
	 		return newParent;
	    }
	    TreeNode* rightleftrotation(TreeNode* node){//RightLeft Rotation
	        TreeNode* temp=node->right;
	 		TreeNode* temp2=node->right->left;
			node->right=temp2->left;
			temp->left=temp2->right;
			temp2->left=node;
			temp2->right=temp;
			return temp2; 
	    }
	    TreeNode* leftrightrotation(TreeNode* node){//LeftRight Rotation
	        TreeNode* temp=node->left;
	 		TreeNode* temp2=node->left->right;
			node->left=temp2->right;
			temp->right=temp2->left;
			temp2->right=node;
			temp2->left=temp;
			
			return temp2;
	    }
	
	
		//Lets Now create a function that manages the balance of a tree
		TreeNode* balance(TreeNode* root,int bf,int id){ //Function to Balance a Tree
		      //Code to Know the type of balance to do
		    if(bf>1 && id<root->left->id){
		    	return leftleftrotation(root);
			}
			if(bf<-1 && id>root->right->id){
				return rightrightrotation(root);
			}
			if(bf<-1 && id<root->right->id){
				return rightleftrotation(root);
			}
			if(bf>1 && id>root->left->id){
		    	return leftrightrotation(root);
			}
            return root;
		}
		
		
		TreeNode* insertNameID(TreeNode* root,Student s){//Function Insert NAME ID
            int id=s.getID();
            string name=s.getname();
            int length=to_string(id).length();
            if (length==8){                
                if (root==NULL){
                    TreeNode* temp=new TreeNode();
                    temp->id=id;
                    temp->student=s;
                    temp->height=heightt(temp);
                    cout<<"successful"<<endl;
                    return temp;
                }
                else if(id < root->id){
                    root->left=insertNameID(root->left,s);
                    }
                else if(id > root->id){
                    root->right=insertNameID(root->right,s);
                }
                else{
                    return root;
                }
                root->height=1+max(heightt(root->right),heightt(root->left));
                int bf=balancefactor(root);

                return balance(root,bf,id);
                }
            else{
                cout<<"unsuccessful"<<endl;	
            }
            
        }	   
        
        //Search ID and Name Functions
    
        bool searchID2(TreeNode* root, int id){//Auxiliar funtion for searchID function
            if(root->id==id){
				string name=root->student.getname();
				cout<<name<<endl;
                return true;
			}
			if (root->right!=NULL){
               		bool x=searchID2(root->right,id);
               		if (x==true){
               			return true;
					}
                }
            if (root->left!=NULL){    
                    bool x=searchID2(root->left,id);
                    if (x==true){
               			return true;
					}
                }
        	return false;
        }
        void searchID(TreeNode* root, int id){//Function search ID
			if(root==NULL){//If tree is empty
                cout<<"unsuccessful"<<endl;
				return;
			}
			if(root->id==id){//If the root is the node corresponding to id
				string name=root->student.getname();
				cout<<name<<endl;
                return;
			}
            else{
            	if(root->right==NULL && root->left==NULL){
                    cout<<"unsuccessful"<<endl;
                    return;
                }
                if (root->right!=NULL){//If we find the Id we already printed and we end the function, if not we
                                       //check if we found it in left node
               		bool x=searchID2(root->right,id);
               		if (x==true){
               			return;
					}
                }
                if (root->left!=NULL){  
                    bool x=searchID2(root->left,id);
                    if (x==true){
               			return;
					}
                }
                cout<<"unsuccessful"<<endl;//If neihter left or right find it , we pritn unsuccessful
            }
		}
        
        int searchName2(TreeNode* root,string name,int count){
			if(root==NULL){//If tree is empty                
				return 0;
			}
            string name2=root->student.getname();
			if(name==name2){//If the root is the node corresponding to id
                int id=root->student.getID();
				cout<<id<<endl;
                count++;
			}
            count=count+searchName2(root->left,name,count)+searchName2(root->right,name,count);            
            return count;
		}
        void searchName(TreeNode* root,string name){
            if(root!=NULL){
                 int count=0;
                 int count2=searchName2(root,name,count);
                 if (count2==0){
                     cout<<"unsuccessful"<<endl;
                     return;
                 }
            }
            else{
                cout<<"unsuccessful"<<endl;
            }
        }
    
    
        //Helpfunction Numofnodes
        int countNode(TreeNode* root){//count number of nodes of a tree
            if(root==NULL){
                return 0;
            }
            return 1 + countNode(root->left) + countNode(root->right);
        }
        //PrintFuncctions
		int printInorder2(TreeNode* root,int count,int count2){ //Function that prints a comma separated InorderTrav
			if (root==NULL){
				return count2;
			}
    		count2=printInorder2(root->left,count,count2);
            string name=root->student.getname();
            cout<<name;
            count2++;
            if(count2<count){  
                cout<<", ";
                
            }          
    		count2=printInorder2(root->right,count,count2);
            return count2;
		}
		void printInorder(TreeNode* root){
            if (root == NULL){
				return;
			}
            int count=countNode(root);
            int count2=0;
            count2=printInorder2(root,count,count2);
            cout<<endl;
        }

        void printPreorder2(TreeNode* root,int count){//Function that prints a comma separated Preorder Traversal
		
            if (root == NULL){
				return;
			}
    		string name=root->student.getname(); 
            cout<<name;
            countt++;
            if(countt<count){               
                cout<<", ";
            }            
    		printPreorder2(root->left,count);
    		printPreorder2(root->right,count);
		}
		void printPreorder(TreeNode* root){
            if (root == NULL){
				return;
			}     
            int count=countNode(root);
            printPreorder2(root,count);
            cout<<endl;
        }

    
		void printPostorder2(TreeNode* root,int count){//Function that prints a comma separated Postorder Traversal
            if (root == NULL){
	            return;
			}
    		printPostorder2(root->left,count);
    		printPostorder2(root->right,count);
    		string name=root->student.getname();
            cout<<name;
            countt++;
            if(countt<count){ 
                cout<<", ";               
            }
		}   
		void printPostorder(TreeNode* root){
            if (root == NULL){
				return;
			}
            int count=countNode(root);
            printPostorder2(root,count);
            cout<<endl;
        }
    
		void printLevelCount(TreeNode* root){//Function that prints the number of levels of the tree
			int levels=heightt(root);
			cout<<levels<<endl;
		}
    
    
        //Remove Functions
        TreeNode* findMin(TreeNode* root){ //We use it to find the minimum node of the tree so we can use it when using deletion on a node
            while(root->left != NULL){
                root = root->left;
            }
	            return root;
        }
        TreeNode* deletion(TreeNode* root,int id){
           if(root==NULL){//Tree is empty
			    return root;
                
			}
			else if(id<root->id){//Id is Smaller, check left tree
				root->left=deletion(root->left,id);
			}
			else if(id>root->id){//ID is bigger check right tree
				root->right=deletion(root->right,id);
			}
            else{//Id  is the same we want to remove
                if((root->left==NULL)&&(root->right==NULL)){//no child, base case
                    delete root;
                    cout<<"successful"<<endl;   
                    root=NULL;
                }
                else if(root->left==NULL){//case 1 Child, left or right 
                    TreeNode* temp=root;
                    root=root->right;
                    delete temp;
                    cout<<"successful"<<endl;   
                }
                else if(root->right==NULL){
                    TreeNode* temp=root;
                    root=root->left;
                    delete temp;
                    cout<<"successful"<<endl;  
                }
                else{//Case 2 children
                     TreeNode* temp=findMin(root->right);
                     root->id=temp->id;
                     root->student=temp->student;
                     root->height=temp->height;
                     root->right=deletion(root->right,temp->id);
                }
            }
            return root;
        }
    
    
        void removeID(TreeNode* root,int id){
            if (root!=NULL){
            	TreeNode* d=deletion(root,id);
                return ;    
			}
			cout<<"unsuccessful"<<endl;               
		}
       
        void inorderNnode(TreeNode* root,TreeNode* node, int n){//function to find the id of the Nth node inorder
            
            if (node == NULL){
               return;
            }
            if (countt <=n) {
                inorderNnode(root,node->left, n);
                countt++;
                if (countt==n){
                   int id=node->student.getID();
                   TreeNode* d=deletion(root,id);//When we got the id we use same function as removeid to delete
                }
                /* now recur on right child */
                inorderNnode(root,node->right, n);
            }
           

        }
        void removeInorder(TreeNode* root,int n){
             if (root!=NULL){
                 //Now send to inorderNode to find de id and delete it
                 TreeNode* node=root;
                 int count=countNode(root);
                 if (n>=count){                
                     cout<<"unsuccessful"<<endl;
                     return;
                 }
                 inorderNnode(root,node,n+1);
                 return;
             }
            cout<<"unsuccessful"<<endl;         
		}    
            
		//We Are going to Use this pointer so calling functions in Main dont acces memory directly
        void insertNameIDX(Student s){
			this->root=insertNameID(this->root,s);
		}
        void searchIDX(int id){
			searchID(this->root,id);
		}
        void searchNameX(string name){
			searchName(this->root,name);
		}
        void printPreorderX(){
			printPreorder(this->root);
		}
		void printPostorderX(){
			printPostorder(this->root);
		}
		void printInorderX(){
			printInorder(this->root);
		}
		void printLevelCountX(){
			printLevelCount(this->root);
		}
		void removeIDX(int id){
			removeID(this->root,id);
		}	
        void removeInorderX(int n){
            removeInorder(this->root,n);
		}
};

int main(){
//your code to invoke the respective commands
	AVL myAVL;
    Student s;
    int numcommands;
    cin>>numcommands;
    for(int i=0;i<=numcommands;i++){
        string line;
        getline(cin,line);
        string comand;
        string input1;
        string input2;
        char* c=&line[0];
        comand=line.substr(0,line.find(" "));
        int pos=line.find(" ")+1;
        int left=line.length()-pos;
        line=line.substr(pos,left);        
                if(comand== "insert"){
						input1=line.substr(0,line.find(" "));
                        input1=input1.substr(1,input1.length()-2);                        
                        int pos2=line.find(" ")+1;
                        int left=line.length()-pos2;
                        input2=line.substr(pos2,left);
                        int id=stol(input2,nullptr);
                        s.setID(id);
                        s.setname(input1);
                        myAVL.insertNameIDX(s);
                }
                else if(comand== "search"){
                    int count=0;
                    for (int i=0;i < line.length(); i++){
                        if (!isdigit( line[i])){
                             count=1;
                        }
                    }
                    if(count==0){
                        int id=stoi(line,nullptr);
                        myAVL.searchIDX(id);
                    }
                    else{       
                        line=line.substr(1,line.length()-2);   
					    myAVL.searchNameX(line);
                    }
                }
			    else if(comand=="remove"){
					int id=stoi(line,nullptr);
					myAVL.removeIDX(id);
                }
				else if(comand=="removeInorder"){
					int n=stoi(line,nullptr);
                    myAVL.removeInorderX(n);
                    myAVL.countt=0;
                }
				else if(comand=="printPreorder"){             
                    myAVL.printPreorderX();
                    myAVL.countt=0;
                 
                }
				else if(comand=="printPostorder"){
					myAVL.printPostorderX();
					myAVL.countt=0;
                }
				else if(comand=="printInorder"){
					myAVL.printInorderX();
                }
				else if(comand=="printLevelCount"){
					myAVL.printLevelCountX();
                }
			
        }
    return 0;
}
