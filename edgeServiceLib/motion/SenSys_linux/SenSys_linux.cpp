// PC_Server.cpp: 定义控制台应用程序的入口点。
//

#include<stdio.h>
#include<stdlib.h>  
#include<sys/socket.h>  
#include<arpa/inet.h>  
#include<string.h>
#include<netinet/in.h>  
#include<iostream>
#define CONNECT_NUM_MAX 10
#include<sys/time.h>
#include"Limber.h"
#include<vector>
#include<string>
#include<fstream>
#include<unistd.h>
#include<pthread.h>

typedef int SOCKET; 
typedef unsigned char BYTE; 
typedef unsigned long DWORD; 
#define FALSE 0 
#define SOCKET_ERROR (-1) 

using namespace std;

float acc_x_f;
float acc_y_f;
float acc_z_f;
float gyo_x_f;
float gyo_y_f;
float gyo_z_f;
int filter = 0;
unsigned short int range;
char* ptr;

float pos1_x, pos1_y, pos1_z;
float pos2_x, pos2_y, pos2_z;
float rot1_x, rot1_y, rot1_z;
float rot2_x, rot2_y, rot2_z;
int vec_pos1_x, vec_pos1_y, vec_pos1_z;
int vec_pos2_x, vec_pos2_y, vec_pos2_z;
int vec_rot1_x, vec_rot1_y, vec_rot1_z;
ofstream testdata("tri-track.txt");

Limber limber;
LimbMotion limbmotion;

double kx = 1.006542;
double bx = 0.006543;
double ky = 1.006542;
double by = 0.006543;
double kz = 1.004016;
double bz = 0.05421;
double pii = 3.14159265;

int main()
{
	OCTree octree;
	//生成树
	OCTree *octreepointer = &octree;
	octree.createOCTree();
	//加载套接字库
	struct sockaddr_in server_addr;
	int serverSocket;
	int iRet = 0;

	//初始化服务器地址族变量
	bzero(&server_addr, sizeof(server_addr)); // 置字节字符串前n个字节为0，包括'\0'
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htons(INADDR_ANY); // 转小端,INADDR_ANY就是指定地址为0.0.0.0的地址
    server_addr.sin_port = htons(3333);  
	
	//创建套接字
	serverSocket = socket(AF_INET, SOCK_STREAM, 0);
	if (serverSocket < 0)
	{
		cout << "serverSocket = socket(AF_INET, SOCK_STREAM, 0) execute failed!" << endl;
		return -1;
	}

	//绑定
	iRet = bind(serverSocket, (struct sockaddr*)&server_addr, sizeof(server_addr));
	if (iRet < 0)
	{
		cout << "bind(serverSocket, (SOCKADDR*)&addrSrv, sizeof(SOCKADDR)) execute failed!" << endl;
		return -1;
	}

	//监听
	iRet = listen(serverSocket, CONNECT_NUM_MAX);
	if (iRet < 0)
	{
		cout << "listen(serverSocket, 10) execute failed!" << endl;
		return -1;
	}

	cout << "listenning..." << endl;

	//等待连接_接收_发送
	struct sockaddr_in client_addr;
    int clientSocket;
    socklen_t length;
	length = sizeof(client_addr);
	
	while (1)
	{
		clientSocket = accept(serverSocket, (struct sockaddr*)&client_addr, &length);
		if (clientSocket < 0)
		{
			cout << "accept(serverSocket, (SOCKADDR*)&client_addr, &len) execute failed!" << endl;
			close(serverSocket);
			return -1;
		}
		else
		{
			cout << "raspi_client connected\n";
			break;
		}
	}

	double kx, bx, ky, by, kz, bz;
	kx = 1.006542;
	bx = 0.006543;
	ky = kx;
	by = bx;
	kz = 1.004016;
	bz = 0.05421;

	while (1)
	{
		char recvBuf[26];
		memset(recvBuf, 0, sizeof(recvBuf));
		if (recv(clientSocket, recvBuf, 26, 0) >0)
		{
			ptr = recvBuf;
			ptr[0] = recvBuf[0];
			ptr[1] = recvBuf[1];
			ptr[2] = recvBuf[2];
			ptr[3] = recvBuf[3];
			memcpy(&acc_x_f, ptr, 4);
			ptr = recvBuf + 4;
			ptr[0] = recvBuf[4];
			ptr[1] = recvBuf[5];
			ptr[2] = recvBuf[6];
			ptr[3] = recvBuf[7];
			memcpy(&acc_y_f, ptr, 4);
			ptr = recvBuf + 8;
			ptr[0] = recvBuf[8];
			ptr[1] = recvBuf[9];
			ptr[2] = recvBuf[10];
			ptr[3] = recvBuf[11];
			memcpy(&acc_z_f, ptr, 4);
			ptr = recvBuf + 12;
			ptr[0] = recvBuf[12];
			ptr[1] = recvBuf[13];
			ptr[2] = recvBuf[14];
			ptr[3] = recvBuf[15];
			memcpy(&gyo_x_f, ptr, 4);
			ptr = recvBuf + 16;
			ptr[0] = recvBuf[16];
			ptr[1] = recvBuf[17];
			ptr[2] = recvBuf[18];
			ptr[3] = recvBuf[19];
			memcpy(&gyo_y_f, ptr, 4);
			ptr = recvBuf + 20;
			ptr[0] = recvBuf[20];
			ptr[1] = recvBuf[21];
			ptr[2] = recvBuf[22];
			ptr[3] = recvBuf[23];
			memcpy(&gyo_z_f, ptr, 4);
			ptr = recvBuf + 24;
			ptr[0] = recvBuf[24];
			ptr[1] = recvBuf[25];
			memcpy(&range, ptr, 2);
			//printf("%d\n", range);
			//cout << "range " << range << endl;
			limber.range = range;
			double noise = 4;
			//cout << "acc  " << acc_x_f << " " << acc_y_f << " " << acc_z_f << endl;
			//cout << "gyro  " << gyo_x_f << " " << gyo_y_f << " " << gyo_z_f << endl;
			if (abs(gyo_x_f) < noise) { gyo_x_f = 0; }
			if (abs(gyo_y_f) < noise) { gyo_y_f = 0; }
			if (abs(gyo_z_f) < noise) { gyo_z_f = 0; }
			acc_x_f = acc_x_f * kx + bx;
			acc_y_f = acc_y_f * ky + by;
			acc_z_f = acc_z_f * kz + bz;
			limber.accel_x = -acc_z_f;
			limber.accel_y = acc_y_f;
			limber.accel_z = -acc_x_f;
			limber.gyro_x = gyo_z_f * pii / 180;
			limber.gyro_y = -gyo_y_f * pii / 180;
			limber.gyro_z = gyo_x_f * pii / 180;
			filter++;
			time_t t;
			time(&t);
			//cout << "time" << t.time << " " << t.millitm << endl;
			if (filter > 100){
				if (limbmotion.inittag == 0)
				{
					limbmotion.Setting(limber);
					limbmotion.inittag = 1;
				}
				if (limbmotion.inittag == 1) {
					limbmotion.cali = 0.0;
					limbmotion.WristEstimate(limber);
					limbmotion.ElbowEstimate2(limber, octreepointer);
					//limbmotion.test1(octreepointer);
					//limbmotion.test2(octree);

					//testdata << to_string(t.time * 1000 + t.millitm) << " " << limber.Esati(0) << " " << limber.Esati(1) << " " << limber.Esati(2) << " " << limber.Wsati(0) << " " << limber.Wsati(1) << " " << limber.Wsati(2) << endl;
					//testdata << limber.Wsati(0) << " " << limber.Wsati(1) << " " << limber.Wsati(2) << " " << limber.Esati(0) << " " << limber.Esati(1) << " " << limber.Esati(2) << " " << limber.Vdis << " " << limbmotion.Eul(0) << " " << limbmotion.Eul(1)<<" " <<limbmotion.changetag <<" " << limbmotion.diserrorcop << endl;		
					//testdata << to_string(t.time * 1000 + t.millitm) << " " << limber.Wsati(0) << " " << limber.Wsati(1) << " " << limber.Wsati(2) << " " << acc_x_f <<" " << acc_y_f<<" " << acc_z_f<< " " << gyo_x_f <<" "<<gyo_y_f<<" "<<gyo_z_f << endl;
				}



				//pos1_x = (float)limber.Esati[0];
				//pos1_y = (float)limber.Esati[1];
				//pos1_z = (float)limber.Esati[2];
				pos1_x = (float)limber.DrawElbowsati[0];
				pos1_y = (float)limber.DrawElbowsati[1];
				pos1_z = (float)limber.DrawElbowsati[2];
				pos2_x = (float)limber.Wsati[0];
				pos2_y = (float)limber.Wsati[1];
				pos2_z = (float)limber.Wsati[2];
				rot1_x = (float)limber.ForePendi[0];
				rot1_y = (float)limber.ForePendi[1];
				rot1_z = (float)limber.ForePendi[2];
				rot2_x = (float)limber.UpperPendi(0);
				rot2_y = (float)limber.UpperPendi(1);
				rot2_z = (float)limber.UpperPendi(2);
				float cali = (float)limbmotion.cali;
				char sendline[78];
				memcpy(sendline, &pos1_x, 4);
				memcpy(sendline + 4, &pos1_y, 4);
				memcpy(sendline + 8, &pos1_z, 4);
				memcpy(sendline + 12, &pos2_x, 4);
				memcpy(sendline + 16, &pos2_y, 4);
				memcpy(sendline + 20, &pos2_z, 4);
				memcpy(sendline + 24, &rot1_x, 4);
				memcpy(sendline + 28, &rot1_y, 4);
				memcpy(sendline + 32, &rot1_z, 4);
				memcpy(sendline + 36, &acc_x_f, 4);
				memcpy(sendline + 40, &acc_y_f, 4);
				memcpy(sendline + 44, &acc_z_f, 4);
				memcpy(sendline + 48, &gyo_x_f, 4);
				memcpy(sendline + 52, &gyo_y_f, 4);
				memcpy(sendline + 56, &gyo_z_f, 4);
				memcpy(sendline + 60, &range, 2);
				memcpy(sendline + 62, &rot2_x, 4);
				memcpy(sendline + 66, &rot2_y, 4);
				memcpy(sendline + 70, &rot2_z, 4);
				memcpy(sendline + 74, &cali, 4);
				cout << sendline << endl;
			}
		}
	}

	close(clientSocket);
	close(serverSocket);

	//system("pause");
	return 0;
}

