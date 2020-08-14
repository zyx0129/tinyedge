#include "stdafx.h"
#include <vector>
#include <algorithm>
#include "OCTree.h"
#include<array>
#include<iostream>
using namespace std;



void insertNode(Node *p, int i[3]){
	Node *node = new Node;
	node->eul[0] = i[0];
	node->eul[1] = i[1];
	node->eul[2] = i[2];
	node->next = p->next;
	p->next = node;
}

void deleteNode(Node *p){
	p->eul[0] = p->next->eul[0];
	p->eul[1] = p->next->eul[1];
	p->eul[2] = p->next->eul[2];
	p->next = p->next->next;
}

//大立方体的范围
MapRect::MapRect(int mine1, int maxe1, int mine2, int maxe2, int mine3, int maxe3){
	minE1 = mine1;
	maxE1 = maxe1;
	minE2 = mine2;
	maxE2 = maxe2;
	minE3 = mine3;
	maxE3 = maxe3;
}
//ele point的生成函数
ElePoint::ElePoint() {}

ElePoint::ElePoint(int Ewrist1, int Ewrist2, int Ewrist3, int Eelbow1, int Eelbow2, int Eelbow3)
{	//根据我后面写得，key是eul[0-2],代表腕位姿
	eul[0] = Ewrist1;
	eul[1] = Ewrist2;
	eul[2] = Ewrist3;
	eul[3] = Eelbow1;
	eul[4] = Eelbow2;
	eul[5] = Eelbow3;
	p.eul[0] = Eelbow1;
	p.eul[1] = Eelbow2;
	p.eul[2] = Eelbow3;
	p.next = NULL;
	//int loc[6] = { Eelbow1, Eelbow2, Eelbow3, Ewrist1, Ewrist2, Ewrist3 };
	//eul = loc;
}
//tree node的生成函数
//一开始是大立方体
//然后继续切分
OCTreeNode::OCTreeNode(MapRect box, string id)
{
	Box = new MapRect(box.minE1, box.maxE1, box.minE2, box.maxE2, box.minE3, box.maxE3);
	nEleCount = 0;
	aCount = 0;
	isLeaf = true;
	//cout << id << endl;;
	ID = id;
	if (id.length() > 100){
		testCount = 0;
		cout << id << endl;
		cout << "splitBOX  " << Box->minE1 << " " << Box->maxE1 << " " << Box->minE2 << " " << Box->maxE2 << " " << Box->minE3 << " " << Box->maxE3 << endl;
		getchar();
	}
}

/*插入位置点*/
//nElecount，在哪里出现了，已满是咋判断的。一个节点最多包含2000个item,啊，这是一个类方法。
//某个treenode 加入一个elepoint
void OCTreeNode::InsertEle(ElePoint elePoint)
{
	//首先判断是否是叶子节点
	if (isLeaf){
		if (nEleCount == MAX_ELE_NUM){//判断节点是否已满
			if (elePoint.eul[0] == elePointObj[0].eul[0] && elePoint.eul[1] == elePointObj[0].eul[1] && elePoint.eul[1] == elePointObj[0].eul[1]){
				int h[3] = { elePoint.eul[3], elePoint.eul[4], elePoint.eul[5] };
				aCount++;
				insertNode(&elePointObj[0].p, h);
				//cout << elePointObj[0].p.eul[0] << " " << elePointObj[ 0].p.eul[1] << " " << elePointObj[0].p.eul[2] << endl;
				//cout << elePointObj[0].p.next->eul[0] << " " << elePointObj[0].p.next->eul[1] << " " << elePointObj[0].p.next->eul[2] << endl;
				//cout << elePointObj[0].p.next->eul[0] << " " << elePointObj[0].p.next->eul[1] << " " << elePointObj[0].p.next->eul[2] << endl;
				//cout << "111" << endl;
				//cout << "ncoun" << nEleCount << endl;
			}
			else{
				//分裂节点后插入
				//cout << "555" << endl;
				if (ID.length() > 30){
					testCount = 0;
					cout << ID << endl;
					cout << "splitBOX  " << Box->minE1 << " " << Box->maxE1 << " " << Box->minE2 << " " << Box->maxE2 << " " << Box->minE3 << " " << Box->maxE3 << endl;
					cout << "EUL555   " << elePoint.eul[0] << " " << elePoint.eul[1] << " " << elePoint.eul[2] << endl;
					cout << "insidenode" << elePointObj[0].eul[0] << " " << elePointObj[0].eul[1] << " " << elePointObj[0].eul[2] << endl;
					getchar();
				}
				SplitNode();
				//cout << "333" << endl;
				InsertEle(elePoint);
			}
		}
		else if (nEleCount < MAX_ELE_NUM)
		{
			//cout << "ncoun22   " << nEleCount << endl;
			elePointObj[nEleCount] = elePoint;
			//cout << "EUL222   " << elePoint.eul[0] << " " << elePoint.eul[1] << " " << elePoint.eul[2] << endl;
			nEleCount++;
			//cout << "222" << endl;

			aCount++;
			//if (nEleCount == MAX_ELE_NUM){
			//SplitNode();
			//}
		}
	}
	else
	{
		//cout << "444" << endl;
		//nEleCount++;
		aCount++;
		if (LUF->isContain(elePoint))
		{
			LUF->InsertEle(elePoint);
		}
		else if (LDF->isContain(elePoint))
		{
			LDF->InsertEle(elePoint);
		}
		else if (RUF->isContain(elePoint))
		{
			RUF->InsertEle(elePoint);
		}
		else if (RDF->isContain(elePoint))
		{
			RDF->InsertEle(elePoint);
		}
		else if (LUL->isContain(elePoint))
		{
			LUL->InsertEle(elePoint);
		}
		else if (LDL->isContain(elePoint))
		{
			LDL->InsertEle(elePoint);
		}
		else if (RUL->isContain(elePoint))
		{
			RUL->InsertEle(elePoint);
		}
		else if (RDL->isContain(elePoint))
		{
			RDL->InsertEle(elePoint);
		}
		else{
			cout << elePoint.eul[0] << " " << elePoint.eul[1] << " " << elePoint.eul[2] << endl;
			cout << "error1" << endl;
		}
	}
}


/*拆分节点*/
void  OCTreeNode::SplitNode()
{
	nEleCount++;
	//cout << nEleCount << "count" << endl;
	int middleE1 = round((Box->minE1 + Box->maxE1) / 2);
	int middleE2 = round((Box->minE2 + Box->maxE2) / 2);
	int middleE3 = round((Box->minE3 + Box->maxE3) / 2);

	MapRect boxLUF(middleE1, Box->maxE1, middleE2, Box->maxE2, middleE3, Box->maxE3);
	MapRect boxLDF(middleE1, Box->maxE1, Box->minE2, middleE2, middleE3, Box->maxE3);
	MapRect boxRUF(Box->minE1, middleE1, middleE2, Box->maxE2, middleE3, Box->maxE3);
	MapRect boxRDF(Box->minE1, middleE1, Box->minE2, middleE2, middleE3, Box->maxE3);
	MapRect boxLUL(middleE1, Box->maxE1, middleE2, Box->maxE2, Box->minE3, middleE3);
	MapRect boxLDL(middleE1, Box->maxE1, Box->minE2, middleE2, Box->minE3, middleE3);
	MapRect boxRUL(Box->minE1, middleE1, middleE2, Box->maxE2, Box->minE3, middleE3);
	MapRect boxRDL(Box->minE1, middleE1, Box->minE2, middleE2, Box->minE3, middleE3);

	LUF = new OCTreeNode(boxLUF, ID + "000");
	LDF = new OCTreeNode(boxLDF, ID + "010");
	RUF = new OCTreeNode(boxRUF, ID + "100");
	RDF = new OCTreeNode(boxRDF, ID + "110");
	LUL = new OCTreeNode(boxLUL, ID + "001");
	LDL = new OCTreeNode(boxLDL, ID + "011");
	RUL = new OCTreeNode(boxRUL, ID + "101");
	RDL = new OCTreeNode(boxRDL, ID + "111");

	//将父节点的内容插入子节点
	for (int i = 0; i < MAX_ELE_NUM; i++)
	{
		if (LUF->isContain(elePointObj[i]))
		{
			//cout << "88888" << endl;
			//cout << "LUFCOUNT   " << LUF->nEleCount << endl;
			LUF->InsertEle(elePointObj[i]);
			//cout << "LUFCOUNTafter   " << LUF->nEleCount << endl;
			//LUF->nEleCount = 1;
		}
		else if (LDF->isContain(elePointObj[i]))
		{
			LDF->InsertEle(elePointObj[i]);
		}
		else if (RUF->isContain(elePointObj[i]))
		{
			RUF->InsertEle(elePointObj[i]);
		}
		else if (RDF->isContain(elePointObj[i]))
		{
			RDF->InsertEle(elePointObj[i]);
		}
		else if (LUL->isContain(elePointObj[i]))
		{
			LUL->InsertEle(elePointObj[i]);
		}
		else if (LDL->isContain(elePointObj[i]))
		{
			LDL->InsertEle(elePointObj[i]);
		}
		else if (RUL->isContain(elePointObj[i]))
		{
			RUL->InsertEle(elePointObj[i]);
		}
		else if (RDL->isContain(elePointObj[i]))
		{
			RDL->InsertEle(elePointObj[i]);
		}
	}

	isLeaf = false;
	//elePointObj = NULL;
}


void OCTree::createOCTree() {
	//MapRect mapRect(-180, 180, -180, 180, -180, 180);
	MapRect mapRect(-181, 181, -181, 181, -181, 181);
	root = new OCTreeNode(mapRect, "s");
	//ifstream file("1030test5deg.txt");
	//ifstream file("111test3deg.txt");
	ifstream file("111test4deg.txt");
	//ifstream file("test.txt");
	if (!file){
		cout << "file not exists\n";
	}
	else{
		cout << "file exists\n";
	}
	string str;
	while (getline(file, str))
	{
		vector<string> v;
		SplitString(str, v, "\t");
		//ElePoint elePoint(atoi(v[3].c_str()), atoi(v[4].c_str()), atoi(v[5].c_str()), atoi(v[0].c_str()), atoi(v[1].c_str()), atoi(v[2].c_str()));
		ElePoint elePoint(atoi(v[2].c_str()), atoi(v[3].c_str()), 0, atoi(v[0].c_str()), atoi(v[1].c_str()), 0);
		//ElePoint elePoint(atoi(v[2].c_str()), atoi(v[3].c_str()), 0, atoi(v[0].c_str()), atoi(v[1].c_str()), 0);
		int test0 = 0;
		if (test0 == 1){
			cout << "test" << atoi(v[2].c_str()) << " " << atoi(v[3].c_str()) << " " << atoi(v[0].c_str()) << " " << atoi(v[1].c_str()) << endl;
			cout << "ele   " << elePoint.eul[0] << " " << elePoint.eul[1] << " " << elePoint.eul[2] << " " << elePoint.eul[3] << " " << elePoint.eul[4] << " " << elePoint.eul[5] << endl;
			getchar();
		}
		//LOGE("%d", elePoint.prior);   
		//cout << "ttt" << endl;
		//cout << "Ecount" << root->nEleCount << endl;
		root->InsertEle(elePoint);
	}
	cout << "create ok" << endl;
	//root->TraverseTree();
}

void OCTree::SplitString(string str, vector<string>& v, string sep)
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


bool OCTreeNode::isContain(ElePoint elePoint)
{
	//cout << "eul" << elePoint.p.eul[0] << " " << Box->minE1 << " " << Box->maxE1 << "eul" << elePoint.p.eul[1] << " " << Box->minE2 << " " << Box->maxE2 << "eul" << elePoint.p.eul[2] << " " << Box->minE3 << " " << Box->maxE3 << endl;
	if ((elePoint.eul[0] >= Box->minE1) && ((elePoint.eul[0] < Box->maxE1) || (elePoint.eul[0] == 1000)) && (elePoint.eul[1] >= Box->minE2) && ((elePoint.eul[1] < Box->maxE2) || (elePoint.eul[1] == 1000)) && (elePoint.eul[2] >= Box->minE3) && ((elePoint.eul[2] < Box->maxE3) || (elePoint.eul[2] == 1000)))
	{
		return true;
	}
	else
	{
		return false;
	}
}

/*查找某个位置在哪个节点内*/
OCTreeNode* OCTreeNode::SelectElePoint(ElePoint elePoint)
{
	OCTreeNode* myNode = NULL;
	if (isContain(elePoint))
	{
		//判断是否为叶子节点
		if (isLeaf)
		{
			myNode = this;
			//cout << "selectBOX  " <<   myNode->Box->minE1 << " " << myNode->Box->maxE1 << " " << myNode->Box->minE2 << " " << myNode->Box->maxE2 << " " << myNode->Box->minE3 << " " << myNode->Box->maxE3 << endl;
		}
		else
		{
			if (LUF->isContain(elePoint))
			{
				myNode = LUF->SelectElePoint(elePoint);
			}
			else if (LDF->isContain(elePoint))
			{
				myNode = LDF->SelectElePoint(elePoint);
			}
			else if (RUF->isContain(elePoint))
			{
				myNode = RUF->SelectElePoint(elePoint);
			}
			else if (RDF->isContain(elePoint))
			{
				myNode = RDF->SelectElePoint(elePoint);
			}
			else if (LUL->isContain(elePoint))
			{
				myNode = LUL->SelectElePoint(elePoint);
			}
			else if (LDL->isContain(elePoint))
			{
				myNode = LDL->SelectElePoint(elePoint);
			}
			else if (RUL->isContain(elePoint))
			{
				myNode = RUL->SelectElePoint(elePoint);
			}
			else if (RDL->isContain(elePoint))
			{
				myNode = RDL->SelectElePoint(elePoint);
			}
		}
	}
	else
	{
		cout << "ERROR2" << endl;
		//Log.e(Global.TAG, "此位置点不在节点范围内");
	}
	return myNode;
}





///*判断位置点是否在这个节点里*/
//bool OCTreeNode::isContain(ElePoint elePoint)
//{
//	if ((elePoint.wristX >= Box->minX) && ((elePoint.wristX < Box->maxX) || (elePoint.wristX == 1)) && (elePoint.wristY >= Box->minY) && ((elePoint.wristY < Box->maxY) || (elePoint.wristY == 1)) && (elePoint.wristZ >= Box->minZ) && ((elePoint.wristZ < Box->maxZ) || (elePoint.wristZ == 1)))
//	{
//		return true;
//	}
//	else
//	{
//		return false;
//	}
//}
//
///*查找某个位置在哪个节点内*/
//OCTreeNode* OCTreeNode::SelectElePoint(ElePoint elePoint)
//{
//	OCTreeNode* myNode = NULL;
//	if (isContain(elePoint))
//	{
//		//判断是否为叶子节点
//		if (isLeaf)
//		{
//			myNode = this;
//		}
//		else
//		{
//			if (LUF->isContain(elePoint))
//			{
//				myNode = LUF->SelectElePoint(elePoint);
//			}
//			else if (LDF->isContain(elePoint))
//			{
//				myNode = LDF->SelectElePoint(elePoint);
//			}
//			else if (RUF->isContain(elePoint))
//			{
//				myNode = RUF->SelectElePoint(elePoint);
//			}
//			else if (RDF->isContain(elePoint))
//			{
//				myNode = RDF->SelectElePoint(elePoint);
//			}
//			else if (LUL->isContain(elePoint))
//			{
//				myNode = LUL->SelectElePoint(elePoint);
//			}
//			else if (LDL->isContain(elePoint))
//			{
//				myNode = LDL->SelectElePoint(elePoint);
//			}
//			else if (RUL->isContain(elePoint))
//			{
//				myNode = RUL->SelectElePoint(elePoint);
//			}
//			else if (RDL->isContain(elePoint))
//			{
//				myNode = RDL->SelectElePoint(elePoint);
//			}
//		}
//	}
//	else
//	{
//		cout << "ERROR2" << endl;
//		//Log.e(Global.TAG, "此位置点不在节点范围内");
//	}
//	return myNode;
//}
//
///*返回这个节点内有多少个位置点*/
//int OCTreeNode::getEleCount()
//{
//	return nEleCount;
//}
//
///*返回这个节点内包含的位置点*/
//ElePoint* OCTreeNode::getElePoint()
//{
//	return elePointObj;
//}
//
///*返回这个节点的ID*/
//string OCTreeNode::getID()
//{
//	return ID;
//}
//
///*返回一个节点周围的节点的ID*/
//string* OCTreeNode::selectNeiborID()
//{
//	int length = (ID.length() - 1) / 3;
//	char* dim1 = new char[length];		//左右
//	char* dim2 = new char[length];		//上下
//	char* dim3 = new char[length];		//前后
//	vector<string> dimVec1, dimVec2, dimVec3;
//	for (int i = 0; i < length; i++)
//	{
//		dim1[i] = ID[i * 3 + 1];
//		dim2[i] = ID[i * 3 + 2];
//		dim3[i] = ID[i * 3 + 3];
//	}
//	dim1[length] = '\0';
//	dim2[length] = '\0';
//	dim3[length] = '\0';
//	dimVec1.push_back(dim1);
//	dimVec2.push_back(dim2);
//	dimVec3.push_back(dim3);
//
//	dim1[length - 1] = (char)('1' - dim1[length - 1] + 48);
//	dim2[length - 1] = (char)('1' - dim2[length - 1] + 48);
//	dim3[length - 1] = (char)('1' - dim3[length - 1] + 48);
//	dimVec1.push_back(dim1);
//	dimVec2.push_back(dim2);
//	dimVec3.push_back(dim3);
//
//	dim1[length - 1] = (char)('1' - dim1[length - 1] + 48);
//	dim2[length - 1] = (char)('1' - dim2[length - 1] + 48);
//	dim3[length - 1] = (char)('1' - dim3[length - 1] + 48);
//	for (int i = length - 1; i >= 0; i--)
//	{
//		if (dim1[i] != dim1[length - 1])
//		{
//			for (int j = i; j < length; j++)
//			{
//				dim1[j] = (char)('1' - dim1[j] + 48);
//			}
//			dimVec1.push_back(dim1);
//			break;
//		}
//	}
//	for (int i = length - 1; i >= 0; i--)
//	{
//		if (dim2[i] != dim2[length - 1])
//		{
//			for (int j = i; j < length; j++)
//			{
//				dim2[j] = (char)('1' - dim2[j] + 48);
//			}
//			dimVec2.push_back(dim2);
//			break;
//		}
//	}
//	for (int i = length - 1; i >= 0; i--)
//	{
//		if (dim3[i] != dim3[length - 1])
//		{
//			for (int j = i; j < length; j++)
//			{
//				dim3[j] = (char)('1' - dim3[j] + 48);
//			}
//			dimVec3.push_back(dim3);
//			break;
//		}
//	}
//
//
//	int num = dimVec1.size() * dimVec2.size() * dimVec3.size();
//	string* res = new string[num + 1];
//	int n = 0;
//	for (int i = 0; i < dimVec1.size(); i++)
//	{
//		for (int j = 0; j < dimVec2.size(); j++)
//		{
//			for (int k = 0; k < dimVec3.size(); k++)
//			{
//				res[n] = "s";
//				for (int m = 0; m < length; m++)
//				{
//					res[n] += dimVec1[i][m];
//					res[n] += dimVec2[j][m];
//					res[n] += dimVec3[k][m];
//				}
//				n++;
//			}
//		}
//	}
//	res[num] = "stop";
//	return res;
//}
//
//
//
///*查找周边ID对应的节点，去掉重复*/
////TODO 处理不是叶子节点的邻近点
//void OCTreeNode::getElePointFromIDs(string* seleIDs, vector<OCTreeNode*>& neiborNodes)
//{
//	int n = 0;
//	while (seleIDs[n] != "stop")
//	{
//		OCTreeNode* neiborNode = getElePointFromID(seleIDs[n]);
//		neiborNodes.push_back(neiborNode);
//		n++;
//	}
//
//	sort(neiborNodes.begin(), neiborNodes.end());
//	neiborNodes.erase(unique(neiborNodes.begin(), neiborNodes.end()), neiborNodes.end());
//}
//
///*查找一个ID对应的节点*/
//OCTreeNode* OCTreeNode::getElePointFromID(string seleID)
//{
//	OCTreeNode* res = NULL;
//
//	if (seleID.find(ID, 0) != string::npos)
//	{
//		if (isLeaf)
//		{
//			res = this;
//		}
//		else
//		{
//			//找到的邻近点不一定是叶子节点的
//			if (seleID == ID)
//			{
//				res = this;
//			}
//			else
//			{
//				if (seleID.find(LUF->getID(), 0) != string::npos)
//				{
//					res = LUF->getElePointFromID(seleID);
//				}
//				else if (seleID.find(LDF->getID(), 0) != string::npos)
//				{
//					res = LDF->getElePointFromID(seleID);
//				}
//				else if (seleID.find(RUF->getID(), 0) != string::npos)
//				{
//					res = RUF->getElePointFromID(seleID);
//				}
//				else if (seleID.find(RDF->getID(), 0) != string::npos)
//				{
//					res = RDF->getElePointFromID(seleID);
//				}
//				else if (seleID.find(LUL->getID(), 0) != string::npos)
//				{
//					res = LUL->getElePointFromID(seleID);
//				}
//				else if (seleID.find(LDL->getID(), 0) != string::npos)
//				{
//					res = LDL->getElePointFromID(seleID);
//				}
//				else if (seleID.find(RUL->getID(), 0) != string::npos)
//				{
//					res = RUL->getElePointFromID(seleID);
//				}
//				else if (seleID.find(RDL->getID(), 0) != string::npos)
//				{
//					res = RDL->getElePointFromID(seleID);
//				}
//				else
//				{
//					//cout << "error111" << endl;
//					//exit(0);
//				}
//			}
//		}
//	}
//	else
//	{
//		//Log.e(Global.TAG, "error2:位置点不包含在索引内");
//	}
//	return res;
//}
//
//void OCTree::createOCTree() {
//	MapRect mapRect(-180, 180, -180, 180, -180, 180);
//	root = new OCTreeNode(mapRect, "s");
//	ifstream file("test.txt");
//	if (!file){
//		//LOGE("file not exists");
//	}
//	else{
//		//LOGE("file exists");
//	}
//	string str;
//	while (getline(file, str))
//	{
//		vector<string> v;
//		SplitString(str, v, "\t");
//		ElePoint elePoint(atof(v[0].c_str()), atof(v[1].c_str()), atof(v[2].c_str()), atof(v[3].c_str()), atof(v[4].c_str()), atof(v[5].c_str()), atoi(v[6].c_str()));
//		//LOGE("%d", elePoint.prior);
//		root->InsertEle(elePoint);
//	}
//	//root->TraverseTree();
//}
//
//
//void SplitString(string str, vector<string>& v, string sep)
//{
//	int length = str.length();
//	int posOld = 0;
//	int pos = 0;
//	bool flag = false;
//	for (int i = 0; i < length; i++)
//	{
//		if (str[i] == sep[pos])
//		{
//			flag = true;
//			pos++;
//		}
//		else
//		{
//			flag = false;
//			pos = 0;
//		}
//		if ((pos == sep.length()) && flag)
//		{
//			if ((i - posOld - sep.length() + 1) != 0)
//			{
//				v.push_back(str.substr(posOld, i - posOld - sep.length() + 1));
//			}
//
//			posOld = i + 1;
//			pos = 0;
//
//		}
//	}
//	if (posOld != length)
//	{
//		v.push_back(str.substr(posOld, length - posOld + 1));
//	}
//}


