import redis
import json
import time

class Redis:
    def __init__(self):
        self.__client = None
        self.connect()

    def connect(self):
        try:
            pool = redis.ConnectionPool(host='edge-cache', port=6379, decode_responses=True)   # host是redis主机，需要redis服务端和客户端都起着 redis默认端口是6379
            self.__client = redis.Redis(connection_pool=pool)
        except:
            print("waiting redis client start...")
            time.sleep(2)
            self.connect()

    def updateData(self, deviceName, property, value ,time):
        #self.__client.set(deviceName+":"+property, {"value":value,"time":time}) 
        self.handleList(deviceName+":"+property,5,json.dumps({"value":value,"time":time}))

    def handleList(self, name,length,value):
        if self.__client.llen(name)==0:
            self.__client.rpush(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个
        elif self.__client.llen(name)==length:
            self.__client.lpop(name)    # 删除列表最左边的元素，并且返回删除的元素
            self.__client.rpushx(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个    
        else:
            self.__client.rpushx(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个    

