#include "stdafx.h"

#include"Limber.h"
#include<cmath>
#include <fstream>
#include <vector>
#include <stdlib.h>
#include <string>

using namespace std;
using namespace Eigen;
#define pi 3.141592654

LimbMotion::LimbMotion() {};

LimbMotion::~LimbMotion() {};

void LimbMotion::Cali(Limber &limber){
	//Vector3d z = limber.ForePendi;//假设备x轴的指向，也就是手表传感器加速度z轴的指向
	//Vector3d origin(1.0, 0.0, 0.0);
	//double value = z.dot(origin);
	////cout << "value1" << value << endl;
	//value = acos(value);  //value可能要取负号TODO
	////cout << "value2" << value << endl;
	//Vector3d cross = z.cross(origin);
	//cross.normalize();
	////cout << "cross" << cross(0) << " " << cross(1) << " " << cross(2) << endl;
	//double c = cos(value / 2);
	//double s = sin(value / 2);
	//Quaterniond rotation(c, s*cross(0), s*cross(1), s*cross(2));
	//Quaterniond wati(limber.Wati(0), limber.Wati(1), limber.Wati(2), limber.Wati(3));
	//rotation.normalize();
	//wati = rotation * wati;
	//limber.Wati << wati.w(), wati.x(),wati.y(),wati.z();
	//Vector3d insSB, insSA, insSC;
	//double q0, q1, q2, q3;
	//q0 = limber.Wati(0);
	//q1 = limber.Wati(1);
	//q2 = limber.Wati(2);
	//q3 = limber.Wati(3);
	//Vector4d q4;
	//q4 << q0, q1, q2, q3;
	////axis X
	//insSB << pow(q4(0), 2) + pow(q4(1), 2) - pow(q4(2), 2) - pow(q4(3), 2), 2 * (q4(1)*q4(2) + q4(0)*q4(3)), 2 * (q4(1)*q4(3) - q4(0)*q4(2));
	////axis Y
	//insSA << 2 * (q4(1) * q4(2) - q4(0) * q4(3)), pow(q4(0), 2) + pow(q4(2), 2) - pow(q4(1), 2) - pow(q4(3), 2), 2 * (q4(2) * q4(3) + q4(0) * q4(1));
	////axis Z
	//insSC << 2 * (q4(0) * q4(2) + q4(1) * q4(3)), 2 * (q4(2) * q4(3) - q4(0) * q4(1)), pow(q4(0), 2) + pow(q4(3), 2) - pow(q4(1), 2) - pow(q4(2), 2);
	////Vector3d showacc;
	////showacc << limber.accel_x, limber.accel_y, limber.accel_z;
	//cout << "here we cali ";
	//cout << "xaxis" << insSB <<endl;
	//cout << "yaxis" << insSA <<endl;
	//cout << "zaxis  " << insSC << endl;
	//cali = 1.0;
	////cout << "acc " << showacc << endl;

}

void LimbMotion::Setting(Limber &limber) {

	//cout << "setting" << endl;
	//确定初始的attitude
	Vector3d reading(limber.accel_x, limber.accel_y, limber.accel_z);
	reading.normalize();
	//cout << limber.accel_x << "accx" << limber.accel_y << "accy" << limber.accel_z << "accz" << endl;
	Vector3d origin(0.0, 1.0, 0.0);
	double value = (reading.dot(origin));
	//cout << "value1" << value << endl;
	value = acos(value);  //value可能要取负号TODO
	//cout << "value2" << value << endl;
	Vector3d cross = reading.cross(origin);
	cross.normalize();
	//cout << "cross" << cross(0) << " " << cross(1) << " " << cross(2) << endl;
	double c = cos(value / 2);
	double s = sin(value / 2);
	Quaterniond rotation(c, s*cross(0), s*cross(1), s*cross(2));
	rotation.normalize();
	//Quaterniond freefall(0.5, -0.5, 0.5, -0.5);

	//根据我建立的点云，不管手表坐标系是怎么样的，我都要把自然下垂对应到我的初始位置四元数。
	//蓝表左手手表向内	//Quaterniond freefall(0, 0.707, 0, 0.707);
	//蓝表左手手表向外
	//Quaterniond freefall(0, -0.707, 0, 0.707);
	//黑表手表向外
	Quaterniond freefall(0, 1, 0, 0);
	//Quaterniond q4 = freefall;
	//Vector4d q4;
	//q4 << freefall.w(), freefall.x(), freefall.y(), freefall.z();
	//cout << "freefall" << 2 * (q4(1) * q4(2) - q4(0) * q4(3)) << " " << pow(q4(0), 2) + pow(q4(2), 2) - pow(q4(1), 2) - pow(q4(3), 2) << " " << 2 * (q4(2) * q4(3) + q4(0) * q4(1)) << endl;
	//蓝表右手手表向外，与左手向内同一种佩戴方法
	//Quaterniond freefall(0, 0.707, 0, 0.707);

	//黄表左手向外，戴法与蓝表相反，
	//Quaterniond freefall(0, -0.707, 0, 0.707);
	//黄表右手向外，戴法与蓝表相反，
	//Quaterniond freefall(0, 0.707, 0, 0.707);

	//testfreefall(freefall);
	Quaterniond instant = rotation * freefall;
	instant.normalize();
	//freefall = instant;
	//q4 << freefall.w(), freefall.x(), freefall.y(), freefall.z();
	//cout << "instant" << 2 * (q4(1) * q4(2) - q4(0) * q4(3)) << " " << pow(q4(0), 2) + pow(q4(2), 2) - pow(q4(1), 2) - pow(q4(3), 2) << " " << 2 * (q4(2) * q4(3) + q4(0) * q4(1)) << endl;
	//getchar();

	//testsetting(instant);

	limber.Wati << instant.w(), instant.x(), instant.y(), instant.z();
	/*
	if (globalmovecount == 0){
	workmode == 'a';
	}
	else if (globalmovecount == 3 ){
	workmode == 'b';
	}
	else if (globalmovecount == 6 ){
	workmode == 'c';
	}
	*/
	limber.Upperarmlen = 27;
	limber.Forearmlen = 24;
	//limber.Wsati << 0, 0, 1;
	//limber.Esati << 0, 0, 1;


	limber.P << 0.001, 0, 0, 0,
		0, 0.001, 0, 0,
		0, 0, 0.001, 0,
		0, 0, 0, 0.001;

	limber.Q << 0.001, 0, 0, 0,
		0, 0.001, 0, 0,
		0, 0, 0.001, 0,
		0, 0, 0, 0.001;

	limber.R << 1, 0, 0,
		0, 1, 0,
		0, 0, 1;
	//cout << "Game begin" << endl;
	for (int i = 0; i < storek; i++){
		store[i] << 0.0, 0.0, 0.0;
	}


}





void LimbMotion::WristEstimate(Limber &limber) {

	int threfreefall = 120;
	int threfreefall2 = 400;
	//if (limber.Wsati(1) > -0.8)//这里用位姿还是用传感器啊大哥
	if (limber.accel_y < 0.85)
	{
		freefallcount = 0;
	}
	//if (limber.Wsati(1) < -0.93)//这里用位姿还是用传感器啊大哥
	if (limber.accel_y > 0.9)
	{
		freefallcount++;
	}
	if (freefallcount > threfreefall && limber.Esati.dot(limber.Wsati) <= 0.8) {
		freefallcount = 0;
		esttag = 0;
		Setting(limber);
		cout << "ReSettingg    " << endl;
	}
	//if (freefallcount > threfreefall && limber.Esati(1) <= -0.85){
	if (freefallcount > threfreefall2){
		freefallcount = 0;
		Setting(limber);
		cout << "recalibration  " << endl;
	}
	//cout << "accy   " << limber.accel_y << endl;
	//cout << "estimation1" <<endl;
	//if (limber.accel_x > 0.95){
	//	Quaterniond Worigin(0, -0.708339, 0.705868, 0.002466);
	//	limber.Wati << Worigin.w(), Worigin.x(), Worigin.y(), Worigin.z();
	//	limber.Eati = limber.Wati;
	//}
	Vector3d gyro;
	gyro << limber.gyro_x, limber.gyro_y, limber.gyro_z;
	//cout << "gyro" <<gyro(0) << " " << gyro(1) << " " << gyro(2) << endl;
	double halfT, half, sixth;
	halfT = limber.halfT;
	sixth = 0.166666666667;
	/*double noise = 5.0 * pi / 180;
	if (abs(limber.gyro_x) < noise){
	limber.gyro_x = 0;
	}
	if (abs(limber.gyro_y) < noise){
	limber.gyro_y = 0;
	}
	if (abs(limber.gyro_z) < noise){
	limber.gyro_z = 0;
	}*/

	double w, x, y, z;
	w = limber.Wati(0);
	x = limber.Wati(1);
	y = limber.Wati(2);
	z = limber.Wati(3);

	//cout << "wxzy" << w <<"   " <<x << "   " << y << "  " << z <<endl;

	Matrix4d F1, F2, F3, F4, F, Ftranspose, I;
	Vector4d lastA, insA, K1, K2, K3, K4, q1, q2, q3, q4;
	lastA << w, x, y, z;

	//cout << "gyrox" << gyro(0) <<"gyroy  " <<gyro(1) << " gyroz  " <<gyro(2) <<endl;

	MatrixXd m(4, 3);
	m << -x, -y, -z,
		w, -z, y,
		z, w, -x,
		-y, x, w;
	K1 = m * gyro * halfT;
	q1 = lastA + K1;

	w = q1(0);
	x = q1(1);
	y = q1(2);
	z = q1(3);
	m << -x, -y, -z,
		w, -z, y,
		z, w, -x,
		-y, x, w;
	K2 = m * gyro * halfT;
	q2 = lastA + K2 * 0.5;

	w = q2(0);
	x = q2(1);
	y = q2(2);
	z = q2(3);
	m << -x, -y, -z,
		w, -z, y,
		z, w, -x,
		-y, x, w;
	K3 = m * gyro * halfT;
	q3 = lastA + K3 * 0.5;

	w = q3(0);
	x = q3(1);
	y = q3(2);
	z = q3(3);
	m << -x, -y, -z,
		w, -z, y,
		z, w, -x,
		-y, x, w;
	K4 = m * gyro * halfT;
	q4 = lastA + (K1 + 2 * K2 + 2 * K3 + K4) * sixth;
	//cout << q4(0) << "Q411  " << q4(1) << "  "<< q4(2) << "  "<< q4(3) <<endl; 
	// getchar();
	double p, q, r;
	p = limber.gyro_x;
	q = limber.gyro_y;
	r = limber.gyro_z;
	//cout << "pqr" <<p << "q  " <<q << "r  " <<r << endl;
	F1 << 0, -p, -q, -r,
		p, 0, r, -q,
		q, -r, 0, p,
		r, q, -p, 0; //代码错了，要乘以halfT,而且角速度x应该写反的，赋值的符号有点存疑，因为输出array的感觉像是按列输出
	F1 = F1 * halfT;
	// cout << F1 <<endl;
	I.setIdentity();
	F2 = F1 * (F1 + I);
	F3 = F2 * (F1 + I);
	F4 = F3 * (F1 + I);
	F = I + (F1 + 2 * F2 + 2 * F3 + F4) * sixth;

	limber.P = F * limber.P * F.transpose() + limber.Q; //这里少了一个计算的东西,因为我用了固定Q


	Vector3d acc;
	acc << limber.accel_x, limber.accel_y, limber.accel_z;

	double accamplitude = sqrt(pow(acc(0), 2) + pow(acc(1), 2) + pow(acc(2), 2));
	double Gvalue = -1;
	if (abs(accamplitude - abs(Gvalue)) < 0.03) {


		Vector3d m;
		//transform gravity in human coordi into device coordi
		m << Gvalue * (2 * (q4(1)*q4(2) + q4(0)*q4(3))), Gvalue * (pow(q4(0), 2) + pow(q4(2), 2) - pow(q4(1), 2) - pow(q4(3), 2)), 2 * Gvalue * ((q4(2)*q4(3) - q4(0)*q4(1)));
		//m << Gvalue * (2 * (q4(0)*q4(2) + q4(1)*q4(3))), 2 * Gvalue * ((q4(2)*q4(3) - q4(0)*q4(1))), Gvalue * (pow(q4(0), 2) + pow(q4(3), 2) - pow(q4(1), 2) - pow(q4(2), 2)) ;
		/*	double mx, my, mz;
		mx = Gvalue * (2 * (q4(2)*q4(3) + q4(1)*q4(4)));
		my = Gvalue * (pow(q4(1), 2) + pow(q4(3), 2) - pow(q4(2), 2) - pow(q4(4), 2));
		mz = Gvalue * (2 * (q4(4)*q4(3) + q4(1)*q4(2)));*/

		MatrixXd H(3, 4);
		H << q4(3), q4(2), q4(1), q4(0),
			q4(0), -q4(1), q4(2), -q4(3),
			-q4(1), -q4(0), q4(3), q4(2);

		H = H * 2 * Gvalue;
		Vector3d res;
		res = acc - m;
		//cout << "acc" << acc << endl;
		//cout << "m" << m << endl;
		//cout << "res" << res(0) << " " << res(1) << " " <<res(2) <<endl;
		//getchar();
		//关于重力的正负号等等不对，m算成0了

		Matrix3d Sk;
		Sk = limber.R + H * limber.P * H.transpose();

		MatrixXd K = limber.P * H.transpose() * Sk.inverse();
		Vector4d gain = K * res;
		// cout << "gain" << gain(0) << " " << gain(1) << " " <<gain(2) << " " << gain(3)<<endl;
		q4 = q4 + gain;
		q4.normalize();
		//  cout << "Q4normal" << q4(0) << "  " << q4(1) << "  "<< q4(2) << "  "<< q4(3) <<endl;
		limber.P = (I - K * H) * limber.P;
	}
	Vector3d insSA, insSB, insSC;
	//axis X
	insSB << pow(q4(0), 2) + pow(q4(1), 2) - pow(q4(2), 2) - pow(q4(3), 2), 2 * (q4(1)*q4(2) + q4(0)*q4(3)), 2 * (q4(1)*q4(3) - q4(0)*q4(2));
	//axis Y
	insSA << 2 * (q4(1) * q4(2) - q4(0) * q4(3)), pow(q4(0), 2) + pow(q4(2), 2) - pow(q4(1), 2) - pow(q4(3), 2), 2 * (q4(2) * q4(3) + q4(0) * q4(1));
	//axis Z
	insSC << 2 * (q4(0) * q4(2) + q4(1) * q4(3)), 2 * (q4(2) * q4(3) - q4(0) * q4(1)), pow(q4(0), 2) + pow(q4(3), 2) - pow(q4(1), 2) - pow(q4(2), 2);
	//Vector3d showacc;
	//showacc << limber.accel_x, limber.accel_y, limber.accel_z;
	//cout << "xaxis" << insSB <<endl;
	//cout << "yaxis" << insSA <<endl;
	//cout << "acc " << showacc << endl; 
	insSA.normalize();
	limber.Wsati = insSA;
	limber.ForePendi = -insSB;
	//limber.UpperPendi = insSC;
	//cout << "wsatiESTEST  " << insSA(0) << " " << insSA(1) << " " << insSA(2) << endl;
	//getchar();
	limber.Wati = q4;
	//cout << "wati" << limber.Wati(0) << "   "<< limber.Wati(1) << "    "<< limber.Wati(2) << "       "<< limber.Wati(3) <<endl;
	if (esttag == 0) {
		//cout << "erroresttag" << endl;
		limber.Esati = limber.Wsati;
		limber.DrawElbowsati = limber.Wsati;
	}
	if (abs(limber.range - limber.Vdis) > 30 && (limber.Vdis >=20 && limber.Vdis <=500)){
		limber.range = 0;
	}

	if (20 < limber.range && limber.range < 500 ) {
		limber.Vdis = limber.range;
	}




}




void LimbMotion::ElbowEstimate2(Limber &limber, OCTree* octree){
	if (limber.Vdis > 300 || limber.Vdis < 20) {
		return;
	}
	//sensorloc = { 67, -28, 0 };
	sensorloc = { 59, -5, 87.5 };
	//cout << "enter1  " << endl;

	double dis = limber.Vdis;

	Vector2i Eular = RotationMatrix2Eular(limber.Wsati);
	Eul = Eular;
	//changetag = 0;
	//cout << "wristori  " << wristori(3) << " " << wristori(4) << " " << wristori(5) << endl;
	//cout << "EUlar    " << Eular(0) << " " << Eular(1) << endl;
	ElePoint teste;
	Vector3d selectelbow;
	teste.eul[0] = Eular(0);
	teste.eul[1] = Eular(1);
	teste.eul[2] = 0;
	int count = 0;

	OCTreeNode* testnode;
	testnode = octree->root->SelectElePoint(teste);
	//cout << testnode->elePointObj[0].eul[0] << " " << testnode->elePointObj[0].eul[1] << " " << testnode->elePointObj[0].eul[2] << " " << endl;
	//getchar();
	Node *a = &testnode->elePointObj[0].p;
	double diserror = 1000;
	Vector3d candy = limber.Esati;
	//cout << "candyini   " << candy << endl;
	int change = 0;
	while (a != NULL){
		//cout << "enter2  " << endl;
		selectelbow = Eular2RotationMatrix(Vector3i{ a->eul[0], a->eul[1], a->eul[2] });
		//cout << "selectelbow   " << selectelbow << endl;
		wristloc = limber.Wsati *limber.Forearmlen + selectelbow * limber.Upperarmlen; //wristori(3)* forearm + selectelbow(0)* upperarm, wristori(4)* forearm + selectelbow(1)* upperarm, wristori(5)* forearm + selectelbow(2)* upperarm };
		//cout << "wristloc   " << wristloc << endl;
		//cout << "abs   " << abs((wristloc - sensorloc).norm() - dis) << endl;
		if (esttag == 0){
			candycopy = selectelbow;
			//cout << "dot" << selectelbow.dot(candycopy) << candycopy << endl;
		}
		double candi = abs((wristloc - sensorloc).norm() - dis) + 30;
		//cout << "candi  " << candi << endl;
		double dot = selectelbow.dot(candycopy);
		if (dot > 1 || dot < -1){
			dot = dot / abs(dot) * 1.0;
		}
		//cout << "dot   " << dot << endl;
		double comp = candi / 30 * (acos(dot) + 3.1415926) * 3 / 3.1415926;
		//cout << "comp   " << comp << endl;
		//cout << "selecandi   " <<candi << endl;
		//if (abs((wristloc - sensorloc).norm() - dis) < diserror && abs((wristloc - sensorloc).norm() - dis) < 10 && selectelbow.dot(candycopy) > 0.95){
		if (comp < diserror && candi <  40 && dot > 0.95){
			//cout << "enter3   " << endl;
			candy = selectelbow;
			/*if (candi == 0 && 0 == 1){
				cout << "wristloc   " << wristloc << endl;
				cout << "sensorloc  " << sensorloc << endl;
				cout << "candi  " << candi << endl;
				cout << "candy   " << candy << endl;
				cout << "getchar112  " << endl;
				getchar();
			}
			if (candy.dot(candycopy) < 0.95) {
				cout << "doterror  " << endl;
				getchar();
			}*/
			//cout << "candydate   " << candy << endl;
			diserror = comp;
			//cout << "diserr  " << diserror << endl;
			change++;
			changetag++;
		}

		//getchar();

		//allselect = allselect + selectelbow;
		//	cout << "selectelbow   " << selectelbow << endl;
		//getchar();
		a = a->next;
		count++;
		//cout << "anext" <<a << endl;
	}
	//getchar();
	for (int i = 0; i < storek-1; i++){
		store[storek - i - 1] = store[storek - i - 2];
	}
	store[0] = candy;
	//store[3] = store[2];
	//store[2] = store[1];
	//store[1] = store[0];
	//store[0] = candy;
	if (store[storek - 1].norm() > 0){
		for (int i = 0; i < storek; i++){
			limber.DrawElbowsati += 1.0/storek * store[i];
		}
	}



	limber.DrawElbowsati.normalize();
	esttag = 1;
	//cout << "esttag  " << esttag << endl;
	//cout << "candycopy   " << candycopy << endl;
	//cout << "dot   " << candy.dot(candycopy) << endl;
	//cout << "candyhere   " << candy << endl;
	//cout << "diserrorhere   " << diserror << endl;
	//cout << "counthere  " << count << endl;
	//cout << "changehere   " << change << endl;
	//cout << "getcharcandy   " << endl;
	//getchar();
	candycopy = candy;
	limber.Esati = candy;
	//Quaterniond axis(0, limber.UpperPendi(0), limber.UpperPendi(1), limber.UpperPendi(2));
	//int theta = -90;
	//double sTheta = sin(theta / 180.0 * 3.1415926 / 2);
	//double cTheta = cos(theta / 180.0 * 3.1415926 / 2);
	//Quaterniond rotate(cTheta, sTheta * axis.x(), sTheta * axis.y(), sTheta * axis.z());
	//Quaterniond upperpendi(0, candy(0), candy(1), candy(2));
	//upperpendi = (rotate * upperpendi * rotate.inverse());
	//limber.UpperPendi << upperpendi.x(), upperpendi.y(), upperpendi.z();

	if ((limber.Esati.dot(limber.Wsati) <= 0.8) && limber.Wsati(1) < -0.8){
		//limber.Esati = 0.3 * limber.Esati + 0.7 * limber.Wsati;
		//limber.Esati.normalize();
		limber.DrawElbowsati = 0.3 * limber.DrawElbowsati + 0.7 * limber.Wsati;
	}
	limber.DrawElbowsati.normalize();

//	if (0 == 0){
		//diserrorcop = diserror;
		//cout << "vdis" << dis << endl;
		//cout << "wristori  " << limber.Wsati << endl;
		//cout << "Drawori    " << limber.DrawElbowsati << endl;
		//cout << "change    " << change << endl;g
		//cout << "Elbow" << limber.Esati(0) << " " << limber.Esati(1) << " " << limber.Esati(2) << endl;
		//cout << "diserror" << diserror << endl;
		//cout << "count" << count << endl;
		//cout << "EUlar    " << Eular(0) << " " << Eular(1) << endl;
		//cout << "candy    " << candy << endl;
		//getchar();
//	}
}

void LimbMotion::test1(OCTree* octree){
	ElePoint teste;
	Vector3d selectelbow;
	teste.eul[0] = 163;
	teste.eul[1] = 5;
	teste.eul[2] = 0;
	OCTreeNode* testnode;
	testnode = octree->root->SelectElePoint(teste);
	Node *a = &testnode->elePointObj[0].p;
	int count = 0;
	while (a != NULL){
		cout << "enter2  " << endl;
		count++;
		selectelbow = Eular2RotationMatrix(Vector3i{ a->eul[0], a->eul[1], a->eul[2] });
		cout << "selectelbow   " << selectelbow << endl;
		a = a->next;
	}
	cout << "counttest   " << count << endl;
	getchar();
}

void LimbMotion::test2(OCTree octree){
	ElePoint teste;
	Vector3d selectelbow;
	teste.eul[0] = 163;
	teste.eul[1] = 5;
	teste.eul[2] = 0;
	OCTreeNode* testnode;
	testnode = octree.root->SelectElePoint(teste);
	Node *a = &testnode->elePointObj[0].p;
	int count = 0;
	while (a != NULL){
		cout << "enter33  " << endl;
		count++;
		selectelbow = Eular2RotationMatrix(Vector3i{ a->eul[0], a->eul[1], a->eul[2] });
		cout << "selectelbow33   " << selectelbow << endl;
		a = a->next;
	}
	cout << "counttest333    " << count << endl;
	getchar();
}


Vector2i LimbMotion::RotationMatrix2Eular(const Vector3d& Ori){
	/*
	case 'xzy'
	%[cy*cz, sz*cx*cy + sy*sx, cy*sx*sz - sy*cx]
	% [-sz, cz*cx, cz*sx]
	% [sy*cz, sy*cx*sz - cy*sx, sy*sx*sz + cy*cx]

	[r1 r2 r3] = threeaxisrot(dcm(2, 3, :), dcm(2, 2, :), -dcm(2, 1, :), ...
	dcm(3, 1, :), dcm(1, 1, :), ...
	- dcm(3, 2, :), dcm(3, 3, :));


	function [r1 r2 r3] = threeaxisrot(r11, r12, r21, r31, r32, r11a, r12a)
	% find angles for rotations about X, Y, and Z axes
	r1 = atan2( r11, r12 );
	r2 = asin( r21 );
	r3 = atan2( r31, r32 );
	if strcmpi( lim, 'zeror3')
	for i = find(abs( r21 ) >= 1.0)
	r1(i) = atan2( r11a(i), r12a(i) );
	r2(i) = asin( r21(i) );
	r3(i) = 0;
	end
	end
	end	*/
	Vector2i res;
	int r1 = round(atan2(Ori(2), Ori(1)) * 180 / pi);
	int r2 = round(asin(-Ori(0)) * 180 / pi);
	res << r1, r2;
	//cout << "EUlar    " << r1 << " " << r2 << endl;
	return res;
}


Vector3d LimbMotion::Eular2RotationMatrix(const Vector3i& E){
	/*
	case 'xzy'
	%[cy*cz, sz*cx*cy + sy*sx, cy*sx*sz - sy*cx]
	% [-sz, cz*cx, cz*sx]
	% [sy*cz, sy*cx*sz - cy*sx, sy*sx*sz + cy*cx]
	dcm(1, 1, :) = cang(:, 3).*cang(:, 2);
	dcm(1, 2, :) = cang(:, 1).*cang(:, 3).*sang(:, 2) + sang(:, 1).*sang(:, 3);
	dcm(1, 3, :) = sang(:, 1).*cang(:, 3).*sang(:, 2) - cang(:, 1).*sang(:, 3);
	dcm(2, 1, :) = -sang(:, 2);
	dcm(2, 2, :) = cang(:, 1).*cang(:, 2);
	dcm(2, 3, :) = sang(:, 1).*cang(:, 2);
	dcm(3, 1, :) = sang(:, 3).*cang(:, 2);
	dcm(3, 2, :) = cang(:, 1).*sang(:, 2).*sang(:, 3) - sang(:, 1).*cang(:, 3);
	dcm(3, 3, :) = sang(:, 1).*sang(:, 2).*sang(:, 3) + cang(:, 1).*cang(:, 3);*/
	Vector3d cang, sang;
	cang << cos(E(0) / 180.0 * pi), cos(E(1) / 180.0 * pi), cos(E(2) / 180.0 * pi);
	sang << sin(E(0) / 180.0 * pi), sin(E(1) / 180.0 * pi), sin(E(2) / 180.0 * pi);
	//double dcm11, dcm12, dcm13, dcm21, dcm22, dcm23, dcm31, dcm32, dcm33;
	double dcm21, dcm22, dcm23;
	//	dcm11 = cang(2) * cang(1);
	//dcm12 = cang(0) * cang(2) * sang(1) + sang(0)*sang(2);
//	dcm13 = sang(0)*cang(2)*sang(1) - cang(0)*sang(2);
	dcm21 = -sang(1);
	dcm22 = cang(0)*cang(1);
	dcm23 = sang(0)*cang(1);
	//dcm31 = sang(2)*cang(1);
	//dcm32 = cang(0)*sang(1)*sang(2) - sang(0)*cang(2);
	//dcm33 = sang(0)*sang(1)*sang(2) + cang(0)*cang(2);
	//cout << "row1  " << dcm11 << " " << dcm12 << " " << dcm13 << endl;
	//cout << "row2  " << dcm21 << " " << dcm22 << " " << dcm23 << endl;
	//cout << "row3  " << dcm31 << " " << dcm32 << " " << dcm33 << endl;
	//cout << endl;
	Vector3d res;
	res << dcm21, dcm22, dcm23;
	return res;
	/*
	x轴对应，dcm11,dcm12,dcm13
	y轴对应，dcm21,dcm22,dcm23，如果我要找的是Y轴的话，就OK了，只需要存前两个角
	z轴对应，dcm31,dcm32,dcm33
	*/
}


void LimbMotion::SplitString(string str, vector<string>& v, string sep)
{
	int length = str.length();
	int posOld = 0;
	int pos = 0;
	bool flag = false;
	for (int i = 0; i < length; i++)
	{
		if (str[i] == sep[pos])
		{
			flag = true;
			pos++;
		}
		else
		{
			flag = false;
			pos = 0;
		}
		if ((pos == sep.length()) && flag)
		{
			if ((i - posOld - sep.length() + 1) != 0)
			{
				v.push_back(str.substr(posOld, i - posOld - sep.length() + 1));
			}

			posOld = i + 1;
			pos = 0;

		}
	}
	if (posOld != length)
	{
		v.push_back(str.substr(posOld, length - posOld + 1));
	}
}
