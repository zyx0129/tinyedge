#ifndef OCTREE_H1
#define OCTREE_H1
#include "stdafx.h"
#include <math.h>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

#define MAX_ELE_NUM 1		//节点包含的最多位置点个数

struct Node{
	int eul[3];
	Node *next;
};

/*空间对象MBR坐标范围*/
class MapRect{
public:
	int minE1, maxE1, minE2, maxE2, minE3, maxE3;

public:
	MapRect(int minE1, int maxE1, int minE2, int maxE2, int minE3, int maxE3);
};

/*空间对象MBR信息*/
class ElePoint
{
public:
	int eul[6];
	Node p;
	//bool prior;

public:
	ElePoint();
	ElePoint(int Eelbow1, int Eelbow2, int Eelbow3, int Ewrist1, int Ewrist2, int Ewrist3);
};

class OCTreeNode {
public:
	string ID;            //左：0，右：1，上：0，下：1，前：0，后：1
	bool isLeaf;        //是否是叶子节点
	MapRect* Box;        //节点代表的矩形区域
	int nEleCount;        //节点包含的所有位置根点个数
	int aCount;				//节点包含的所有位姿条目个数
	int testCount;
	ElePoint elePointObj[MAX_ELE_NUM];        //位置点列表,最多2000个
	OCTreeNode* LUF;        //左上前
	OCTreeNode* LDF;        //左下前
	OCTreeNode* RUF;        //右上前
	OCTreeNode* RDF;        //右下前
	OCTreeNode* LUL;        //左上后
	OCTreeNode* LDL;        //左下后
	OCTreeNode* RUL;        //右上后
	OCTreeNode* RDL;        //右下后

public:
	//一个node的构造函数，包含了一个立方体（长方体）的长宽高，还有代表位置的string
	OCTreeNode(MapRect box, string id);
	//向一个octreenode 插入一个条目
	void InsertEle(ElePoint elePoint);
	//把一个octreenode 
	void SplitNode();
	//判断一个条目是否属于一个octreenode
	bool isContain(ElePoint elePoint);
	OCTreeNode* SelectElePoint(ElePoint elePoint);
	string* selectNeiborID();
	void getElePointFromIDs(string* seleIDs, vector<OCTreeNode*>& neiborNodes);
	OCTreeNode* getElePointFromID(string seleID);
	string getID();
	void handleOCTreeNode(OCTreeNode myNode, double diry, double dirz, double dis);
	int getEleCount();
	ElePoint* getElePoint();

	//void TraverseTree();
};

class OCTree{
public:
	OCTreeNode* root;

public:
	static void SplitString(string str, vector<string>& v, string sep);
	void createOCTree();
};
#endif


  
