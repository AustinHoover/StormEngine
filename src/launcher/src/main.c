#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<fcntl.h>
#include<sys/types.h>
#include<sys/stat.h>

#define MAX_PATH_SIZE 250
#define LOG_HOLDER_SIZE 500

void logVar(char * message, char * var);
void redirectStdout();

int main(){
	redirectStdout();
	char currentWorkingDirectory[MAX_PATH_SIZE];
	if(getcwd(currentWorkingDirectory,sizeof(currentWorkingDirectory)) != NULL){
	} else {
		printf("Failed to get current working directory!\n");
		fflush(stdout);
		return 1;
	}
	//printf("CWD: %s\n",currentWorkingDirectory);
	//fflush(stdout);
	//get java location
	char javaPath[MAX_PATH_SIZE + 50];
	strcpy(javaPath,currentWorkingDirectory);
	strcat(javaPath,"\\jdk\\bin\\java.exe\0");
	//get jar location
	char jarPath[MAX_PATH_SIZE + 50];
	
	strcpy(jarPath,"\"");
	strcat(jarPath,currentWorkingDirectory);
	strcat(jarPath,"\\engine.jar");
	strcat(jarPath,"\"");
	logVar("javaPath: ",javaPath);
	logVar("jarPath:  ",jarPath);
	printf("%s\n",jarPath);
	fflush(stdout);
	int execVal = execlp(javaPath,"java","-jar","-Djava.library.path=./shared-folder",jarPath, (char *)NULL);
	printf("Exec code: %d\n",execVal);
	perror("execlp");
	fflush(stdout);
	fflush(stderr);
	return 0;
}

void redirectStdout(){
	int outfd = open("stdout.txt", O_CREAT|O_WRONLY|O_TRUNC, 0644);
    if (!outfd)
    {
		printf("Error opening stdout redirect");
		fflush(stdout);
    }
    dup2(outfd, 1); // replace stdout
    close(outfd);
	int outerrfd = open("stderr.txt", O_CREAT|O_WRONLY|O_TRUNC, 0644);
    if (!outerrfd)
    {
		printf("Error opening stdout redirect");
		fflush(stderr);
    }
    dup2(outerrfd, fileno(stderr)); // replace stderr
    close(outerrfd);
}


char logHolder[LOG_HOLDER_SIZE];
void logVar(char * message, char * var){
	strcpy(logHolder,message);
	strcat(logHolder,var);
	printf("%s\n",logHolder);
	fflush(stdout);
}
