#ifndef LimbMotion_H
#define LimbMotion_H
#include<iostream>
#include"Eigen/Core"
#include"Eigen/Dense"
#include<vector>
#include<string>
#include "OCTree.h"
//#include"consql.h"


struct Limber {
	int Forearmlen;
	int Upperarmlen;
	//attitude and simplified attitude amd location of joints
	Eigen::Vector4d Eati;//quaternion
	Eigen::Vector3d Esati;//3d vector
	Eigen::Vector3d Eloc;//3d location
	Eigen::Vector3d ForePendi;
	Eigen::Vector3d UpperPendi;
	Eigen::Vector4d Wati;
	Eigen::Vector3d Wsati;
	Eigen::Vector3d Wloc;
	Eigen::Vector3d DrawElbowsati;

	// Process noise matrix
	Eigen::Matrix4d Q;

	// Measurement noise matrix
	Eigen::Matrix3d R;

	// EKF covariance
	Eigen::Matrix4d P;

	// accelerometer measurement variance
	float accel_var;

	// Process variance
	float process_var;

	// Entries for storing processed sensor data
	Eigen::Vector3d gyro;
	float gyro_x;
	float gyro_y;
	float gyro_z;

	Eigen::Vector3d acc;
	float accel_x;
	float accel_y;
	float accel_z;

	float Vdis;
	float range;

	double halfT = 0.0055555555;

	double time;

	//float mag_x;
	//float mag_y;
	//float mag_z;
};

class LimbMotion {
public:
	//motion 用于判断move or stable
	//start, 游戏start
	int movetime = 0;
	int changetag = 0;
	int freefallcount;
	int start = 0;
	int end = 0;
	int movectag = 0;
	int movectag2 = 0;
	Eigen::Vector3d movecElbow;
	Eigen::Vector2i Eul;
	double diserrorcop;
	//char state;
	int inittag = 0;
	int esttag = 0;
	int motion = 0;
	int globalcount = 0;
	int savepast = 0;
	float cali = 0.0;
	double PastAcc[3][20];
	double PastSati[3][20];
	Eigen::Vector3d AVERsati;
	Eigen::Vector3d AVERacc;
	Eigen::Vector3d sensorloc;//传感器位置
	int storek = 50;//storek越大，就画图的延时越大，但是画图越平滑
	Eigen::Vector3d store[50];
	
	char workmode;
	char state;
	double proMax;
	float control = 0.0f;
	double refdis;
	double last10dis;
	Eigen::Vector3d refAcc;
	Eigen::Vector3d refSati;
	Eigen::Vector3d  wristloc; Eigen::Vector3d candycopy;

	LimbMotion();
	~LimbMotion();
	//void DetectSingleControl(Limber &limber);
	//int DetectContinuousControl(Limber &limber);
	void Setting(Limber &limber);//初始化
	void Cali(Limber &limber);//校准x,z方向
	void WristEstimate(Limber &limber);//预测腕位姿
	//void Initialize();
	//void createOCTree();
	void ElbowEstimate(Limber &limber, OCTree tree);//预测肘位姿
	//void selectFromOCTree(Limber &limber);
	void Locating(Limber &limber);
	void GetMode(Limber &limber);
	void SplitString(std::string str, std::vector<std::string>& v, std::string sep);
	Eigen::Vector3d Eular2RotationMatrix(const Eigen::Vector3i& E);
	Eigen::Vector2i RotationMatrix2Eular(const Eigen::Vector3d& Ori);
	void ElbowEstimate2(Limber &limber, OCTree* octree);
	void test1(OCTree* octree);
	void test2(OCTree octree);
private:


};

#endif


#pragma once
