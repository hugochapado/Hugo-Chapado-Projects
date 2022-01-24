//  MSH main file
// Write your msh source code here

//#include "parser.h"
#include <stddef.h>			/* NULL */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <math.h> //include it for floor fucntion

#define MAX_COMMANDS 8



// ficheros por si hay redirección
char filev[3][64];

//to store the execvp second parameter
char *argv_execvp[8];

void siginthandler(int param)
{
	printf("****  Saliendo del MSH **** \n");
	//signal(SIGINT, siginthandler); 
        exit(0);
}

/**
 * Get the command with its parameters for execvp
 * Execute this instruction before run an execvp to obtain the complete command
 * @param argvv
 * @param num_command
 * @return
 */
void getCompleteCommand(char*** argvv, int num_command) {
    //reset first
    for(int j = 0; j < 8; j++)
        argv_execvp[j] = NULL;

    int i = 0;
    for ( i = 0; argvv[num_command][i] != NULL; i++)
        argv_execvp[i] = argvv[num_command][i];
}


/**
 * Main sheell  Loop  
 */
int main(int argc, char* argv[])
{
    /**** Do not delete this code.****/
    int end = 0; 
    int executed_cmd_lines = -1;
    char *cmd_line = NULL;
    char *cmd_lines[10];

    if (!isatty(STDIN_FILENO)) {
        cmd_line = (char*)malloc(100);
        while (scanf(" %[^\n]", cmd_line) != EOF){
            if(strlen(cmd_line) <= 0) return 0;
            cmd_lines[end] = (char*)malloc(strlen(cmd_line)+1);
            strcpy(cmd_lines[end], cmd_line);
            end++;
            fflush (stdin);
            fflush(stdout);
        }
    }

    /*********************************/

    char ***argvv = NULL;
    int num_commands;
    int Acc=0; //set Acc to 0
    //signal(SIGCHLD,SIG_IGN);//signal for deleting zombie procesess

	while (1) 
	{
		int status = 0;
	        int command_counter = 0;
		int in_background = 0;
		signal(SIGINT, siginthandler);
		// Prompt 
		write(STDERR_FILENO, "MSH>>", strlen("MSH>>"));

		// Get command
                //********** DO NOT MODIFY THIS PART. IT DISTINGUISH BETWEEN NORMAL/CORRECTION MODE***************
                executed_cmd_lines++;
                if( end != 0 && executed_cmd_lines < end) {
                    command_counter = read_command_correction(&argvv, filev, &in_background, cmd_lines[executed_cmd_lines]);
                }else if( end != 0 && executed_cmd_lines == end)
                    return 0;
                else
                    command_counter = read_command(&argvv, filev, &in_background); //NORMAL MODE
                //************************************************************************************************


              /************************ STUDENTS CODE ********************************/
	      if (command_counter > 0) {
                if (command_counter > MAX_COMMANDS)
                      printf("Error: Numero máximo de comandos es %d \n", MAX_COMMANDS);
              }
		//We use getcompletecommand before runing an execvp
		for(int j=0; j<command_counter;j++){
			getCompleteCommand(argvv,j);
		}
	
	
		//mycalc code
		if (strcmp(argv_execvp[0], "mycalc")==0){
			if(argv_execvp[1]!=NULL && argv_execvp[2]!=NULL && argv_execvp[3]!=NULL){
				if  (argv_execvp[4]!=NULL){//if there is more than one operator or format is incorrect
					printf("[ERROR] The structure of the command is <operand1><add/mod><operand2>\n");
				}
				//We start with add operation
				else if(strcmp(argv_execvp[2], "add")==0){		
					char str[8];		
					int ope1=atoi(argv_execvp[1]); 
					int ope2=atoi(argv_execvp[3]);
					Acc=Acc+ope1+ope2;
					sprintf(str, "%d", Acc);	
					const char *val=str;
					if (setenv("Acc",val,2)==-1){
						perror("An error ocurr assigning the value to Acc \n");
						return -1;
					}	
					fprintf(stderr,"[OK] %d + %d = %d; Acc %s\n", ope1, ope2, ope1 +ope2, getenv("Acc")); 
				}
				//We do mod operation
				else if(strcmp(argv_execvp[2], "mod")==0){
					int ope1=atoi(argv_execvp[1]); 
					int ope2=atoi(argv_execvp[3]);
					fprintf(stderr,"[OK] %d %% %d = %d * %d + %d\n", ope1, ope2, ope2, abs(floor(ope1/ope2)),ope1 % ope2);
				}
				else { //Error if operand is different from mod or add
				printf("[ERROR] The structure of the command is <operand1><add/mod><operand2>\n");
				}
			}		
			else { //Error if format of operation is incorrect
				printf("[ERROR] The structure of the command is <operand1><add/mod><operand2>\n");
			}
		}
		//mycp code 
		else if	(strcmp(argv_execvp[0], "mycp")==0){
			if (argv_execvp[1]!=NULL && argv_execvp[2]!=NULL) {
		        	int originfile = open(argv_execvp[1], O_RDONLY, 0644);//We open Origin File and check if it has been open correctly
		        	if (originfile == -1) {
					printf("[ERROR] Error opening original file\n");
				}
				else if(argv_execvp[3]!=NULL){
					printf("[ERROR]The structure of the command os mycp<originalfile><copiedfile>\n");
				}
				else{
						
		              		char buf[1024];
		             		int destinyfile = open(argv_execvp[2], O_CREAT| O_WRONLY | O_TRUNC, 0666);
					if (destinyfile ==-1){
						printf("[ERROR] Error opening the copied file\n");
					}
					else{
						int fileread;
						int filewrite;
						while((fileread=read(originfile,buf,1024))>0){  //Reads 1024 bytes of data(or less if end of file or signal interruption) and writes them in destiny file
							filewrite=write(destinyfile,buf,fileread);
							if (filewrite==-1){
								if (close(destinyfile)==-1){
									perror("Error:Closing destinyfile\n");
									return -1;
								}
								perror("Error: writing the file \n");
								return -1;
							}
						}
						//check error if reading
						if (fileread==-1){
							if (close(destinyfile)==-1){
								perror("Error:Closing destinyfile\n");
								return -1;
							}
							perror("Error: reading the file \n");
							return -1;
						}
						//Close both files
						if (close(destinyfile)==-1){
							perror("Error:Closing destinyfile\n");
							return -1;
						}

						if (close(originfile)==-1){
							perror("Error:Closing originfile\n");
							return -1;
						}
						printf("[OK] Copy has been successful between %s and %s\n",argv_execvp[1],argv_execvp[2]);
					}
				}
			}
			else{
				printf("[ERROR]The structure of the command os mycp<originalfile><copiedfile>\n");
			}
		}
		else if (command_counter==1){  //Execution of simple commands
			int pid=fork();
			int file=0;
			int status;
			switch (pid){
				case -1://Error
					perror("Error: fork\n");
					exit(-1);//duda exit
				
				case 0://child process
					//Redirection
					if (strcmp(filev[0],"0")!=0){
						if((close(STDIN_FILENO))==-1){
							perror("Error:Closing STDIN_FILENO\n");
						}
						if ((file=open(filev[0],O_RDONLY))==-1){
							perror("Error: Opening File\n");
						}
					}
					if (strcmp(filev[1],"0")!=0){
						if((close(STDOUT_FILENO))==-1){
							perror("Error:Closing STDOUT_FILENO\n");
						}
						if ((file=open(filev[1],O_CREAT| O_WRONLY,0666))==-1){
							perror("Error: Opening File\n");
						}
					}
					if (strcmp(filev[2],"0")!=0){
						if((close(STDERR_FILENO))==-1){
							perror("Error:Closing STDERR_FILEN\nO");
						}
						if ((file=open(filev[2],O_CREAT| O_WRONLY, 0666))==-1){
							perror("Error: Opening File\n");
						}
					}
					//Child
					execvp(argv_execvp[0], argv_execvp);
					perror("Error:exec, if all is correct this should never be executed\n");


				default://father process
					//Close file if opened
					if (file!=0){
						if ((close(file))==-1){
							perror("Error:Closing File\n");
						}
					}
					if (in_background==1){	//executed in background padre
						printf("[%d]\n",getpid());
					}
					else if (in_background!=1){
						while(wait(&status) !=pid);
						if (status!=0){
							perror("Error:Executing Child\n");
						}
					}
					//duda Zombies
					
			}
		}
		else if (command_counter>1){	//Execution of Sequence Commands
			int fd[2];
			int pid;
			int file=0;
			int status=0;
			int fdn;
			//Implementation of arbitrary number of commands
			for (int j=0;j<command_counter;j++){
			//Now we have to create the pipes Beetween Procesess, which means if we have n process , we create n-1 pipes
				if (j!=(command_counter -1)){
					if (pipe(fd)==-1){
						perror("Error:Pipe\n");
					}
				}	
				
				switch (pid=fork()){
					case -1://Error
						perror("Error: fork");
						//IF Error we close both files because we dont use them
						if ((close(fd[0]))==-1){
							perror("Error:Closing Read descriptor\n");
						}
						if ((close(fd[1]))==-1){
							perror("Error:Closing Write descriptor\n");
						}
						exit(-1);
					case 0://Child Process
						//We close Write Descriptor because we dont use it, then duplicate Write descriptor in the stdout and close it afterwards 
						//(We duplicate it so we dont needit)
						if (j !=command_counter-1){
							if ((close(fd[0]))==-1){
								perror("Error:Closing Read descriptor\n");
							}
							if((dup2(fd[1],STDOUT_FILENO))==-1){
								perror("Error:Duplicating Write descriptor\n");
							}
							if ((close(fd[1]))==-1){
								perror("Error:Closing Write descriptor\n");
							}
						}
						//If we are not in the first process we duplicate the actual fdn
						if(j!=0){
							if((dup2(fdn,STDIN_FILENO))==-1){
								perror("Error:Duplicating Read descriptor\n");
							}
							if ((close(fdn))==-1){
								perror("Error:Closing Read descriptor\n");
							}
						}
						//Redirections
						//If is the first command we check input redirection
						if (strcmp(filev[0],"0")!=0 && j==0){
							if((close(STDIN_FILENO))==-1){
								perror("Error:Closing STDIN_FILENO\n");
							}
							if ((file=open(filev[0],O_RDONLY))==-1){
								perror("Error: Opening File\n");
							}
						}
						//If is the last command we check output redirection
						if (strcmp(filev[1],"0")!=0 && j==command_counter-1){
							if((close(STDOUT_FILENO))==-1){
								perror("Error:Closing STDOUT_FILENO\n");
							}
							if ((file=open(filev[1],O_CREAT| O_WRONLY,0666))==-1){
								perror("Error: Opening File\n");
							}
						}
						//Redirection of error affects all commands

						if (strcmp(filev[2],"0")!=0 ){
							if((close(STDERR_FILENO))==-1){
								perror("Error:Closing SDTERR_FILENO\n");
							}
							if ((file=open(filev[2],O_CREAT| O_WRONLY, 0666))==-1){
								perror("Error: Opening File\n");
							}
						}
						getCompleteCommand(argvv,j);//Get the next command in the sequence
						//We call the command
						execvp(argv_execvp[0], argv_execvp);
						perror("Error:exec, if all is correct this should never be executed\n");

					default://Father Process
						//If we are in the first process we dont have to close fdn because its not created yet 
						//We check if we are in the last procces, if we are we close fdn, if not , we close it and  change it
						if (j==0){
							//We give fdn the Reading descriptor bc is the want we want to use for next procces
							if((fdn=(dup(fd[0])))==-1){
								perror("Error:Duplicating Read descriptor\n");
							}
							if ((close(fd[1]))==-1){
								perror("Error:Closing Write descriptor\n");
							}
						}
						else if(j !=(command_counter -1)){
							if ((close(fdn))==-1){
								perror("Error:Closing fdn descriptor\n");
							}
							//We give fdn the Reading descriptor bc is the want we want to use for next procces
							if((fdn=(dup(fd[0])))==-1){
								perror("Error:Duplicating Read descriptor\n");
							}
							if ((close(fd[1]))==-1){
								perror("Error:Closing Write descriptor\n");
							}
						}
						else{
							if ((close(fdn))==-1){
								perror("Error:Closing fdn descriptor\n");
							}
							if (in_background==1){	//executed in background the last process
							printf("[%d]\n",getpid());
							}
						}					
				}
				
			}
			//Close file if opened(If redirections)
			if (file!=0){
				if ((close(file))==-1){
					perror("Error:Closing File\n");
				}
			}
			if (in_background!=1){
					while(wait(&status) !=pid);
					if (status!=0){
						perror("Error:Executing Child\n");
				}	
			}
		
			
		}           
	}
	return 0;
}
