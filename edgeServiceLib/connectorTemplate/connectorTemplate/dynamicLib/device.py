from kafka import KafkaConsumer
from kafka import KafkaProducer
import json 
import requests
import redis
import time
import os
from abc import abstractmethod

class Device():
    def __init__(self):
        self.__scene = None
        self.__config = None
        self.__pubJson = {}
        self.__producer = None
        self.__redis = None
        self.updateConfig()
        self.__consumer = None
        self.initConsumer()
        
    def updateConfig(self):
        configPath = "/app/config/custom.json"
        if os.path.exists(configPath):
            with open(configPath,'r') as load_f:
                configJson = json.load(load_f)
                self.__scene = configJson["scene"]
                self.__config = configJson["serviceConfig"]
                router = configJson["router"]["#serviceName"]
                if "pub" in router:
                    self.__pubJson = router["pub"]

    def getConfig(self,service ="#serviceName"):
        return self.__config[service]
            

    def run(self):
        self.__consumer.subscribe(topics="command_#serviceName")
        for msg in self.__consumer:
            print("msg come")
            value = msg.value
            print(value)
            value = str(value, "utf-8")
            value = json.loads(value)
            deviceName = value["deviceName"]
            command = value["command"]
            params = value["params"]
            self.commandHandle(deviceName,command,params)

    def initProducer(self):
        try:
            self.__producer = KafkaProducer(bootstrap_servers='edge-kafka:9092')
        except Exception as e:
            print("waiting kafka client start...")
            time.sleep(2)
            self.initProducer()

    def initRedis(self):
        try:
            self.__redis = redis.Redis(host='edge-cache', port=6379, decode_responses=True)   # host是redis主机，需要redis服务端和客户端都启动 redis默认端口是6379
        except:
            print("waiting redis client start...")
            time.sleep(2)
            self.initRedis()

    def publish(self, msg):
        if not self.__producer:
            self.initProducer()
        deviceName = msg.deviceName
        topic = ""
        if deviceName in self.__pubJson:
            topic = self.__pubJson[deviceName]
        elif "default" in self.__pubJson:
            topic = self.__pubJson["default"]
        else:
            print("lack default router")
            return
        print(msg.__dict__)
        print("topic:"+topic+" msg:"+str(msg))
        self.__producer.send(topic,bytes(json.dumps(msg.__dict__), encoding = "utf8"))

    def updateCache(self, deviceName,property,value, uploadTime = 0):
        def handleList(name,length,value):
            if self.__redis.llen(name)==0:
                self.__redis.rpush(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个
            elif self.__redis.llen(name)==length:
                self.__redis.lpop(name)    # 删除列表最左边的元素，并且返回删除的元素
                self.__redis.rpushx(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个    
            else:
                self.__redis.rpushx(name, value)   # 这里"foo_list1"之前已经存在，往列表最右边添加一个元素，一次只能添加一个    s 
        if not self.__redis:
            self.initRedis()
        nowTime = uploadTime
        if uploadTime:
            nowTime = uploadTime
        else:
            nowTime = time.time()
        handleList(deviceName+":"+property,5,json.dumps({"value":value,"time":nowTime}))

    # def updateCache(self, msg):
    #     if not self.__producer:
    #         self.initProducer()
    #     topic = "cache_redis"
    #     print("topic:"+topic+" msg:"+str(msg.__dict__))
    #     self.__producer.send(topic,bytes(json.dumps(msg.__dict__), encoding = "utf8"))

    def initConsumer(self):
        try:
            self.__consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000)
        except Exception as e:
            print("waiting kafka client start...")
            time.sleep(2)
            self.initConsumer()

    @abstractmethod
    def init_client(self):
        pass
    
    @abstractmethod
    def commandHandle(self,deviceName,command,params):
        pass
