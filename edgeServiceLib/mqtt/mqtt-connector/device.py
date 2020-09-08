from kafka import KafkaConsumer
from kafka import KafkaProducer
import json 
import requests
import redis
import time
import os
from abc import abstractmethod
from abc import ABC
import uuid

class Device(ABC):
    def __init__(self):
        self.__scene = None
        self.__config = None
        self.__serviceList = {}
        self.__pubJson = {}
        self.__producer = None
        self.__redis = None
        self.__moduleName = os.environ["MODULE_NAME"]
        self.updateConfig()
        self.__consumer = None
            
    def updateConfig(self):
        configPath = "/app/config/custom.json"
        if os.path.exists(configPath):
            with open(configPath,'r') as load_f:
                configJson = json.load(load_f)
                if "scene" in configJson:
                    self.__scene = configJson["scene"] 
                self.__serviceList = configJson["serviceInfo"]
                if "serviceConfig" in  configJson:
                    self.__config = configJson["serviceConfig"]
                if "router" in configJson and self.__moduleName in configJson["router"]:
                    router = configJson["router"][self.__moduleName]
                    if "pub" in router:
                        self.__pubJson = router["pub"]
                    if "sub" in router:
                        self.__subTuple = tuple(router["sub"])

    def getConfig(self,service = os.environ["MODULE_NAME"]):
        if service in self.__config:
            return self.__config[service]
        return None

    def checkServiceIfExist(self,service):
        return service in  self.__serviceList
            
    def getScene(self):
        return self.__scene

    def run(self):
        self.initConsumer()
        self.__consumer.subscribe(topics="command_"+self.__moduleName)
        print("Connect to kafka client successfully,start to subscribe messages")
        for msg in self.__consumer:
            value = msg.value     
            value = str(value, "utf-8")
            print("New message come: "+ str(value)+ " , timestamp:"+str(time.time()))
            value = json.loads(value)
            if value["type"] == "callService":
                self.callService(value["deviceName"],value["service"],value["payload"])
            elif value["type"] == "setValue":
                self.setProperties(value["deviceName"],value["payload"])
        

    def initProducer(self):
        try:
            self.__producer = KafkaProducer(bootstrap_servers='edge-kafka:9092')
        except Exception as e:
            print("Waiting for service kafka to initialize...")
            time.sleep(2)
            self.initProducer()

    def initConsumer(self):
        try:
            self.__consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000)
        except Exception as e:
            print("Waiting for service kafka to initialize...")
            time.sleep(2)
            self.initConsumer()

    def initRedis(self):
        try:
            self.__redis = redis.Redis(host='edge-cache', port=6379, decode_responses=True)   # host是redis主机，需要redis服务端和客户端都启动 redis默认端口是6379
        except:
            print("Waiting for service redis to initialize...")
            time.sleep(2)
            self.initRedis()

    def registerDevice(self,deviceName):
        try:
            r = requests.post("http://edge-device-management/device/register", data = {"deviceName":deviceName,"connector":self.__moduleName})
            res = r.json()
            if res["code"]!=0:
                print(res["message"])
                return 0
            else:
                print(deviceName + " register success")
                return 1
        except:
            print("Waiting for service device-management to initialize...")
            time.sleep(2)
            return self.registerDevice(deviceName)


    def onlineDevice(self,deviceName):
        r = requests.post("http://edge-device-management/device/updateStatus", data = {"deviceName":deviceName,"status":"online"})

    def offlineDevice(self,deviceName):
        r = requests.post("http://edge-device-management/device/updateStatus", data = {"deviceName":deviceName,"status":"offline"})     

    def updateAddress(self,deviceName,address):
        r = requests.post("http://edge-device-management/device/updateAddress", data = {"deviceName":deviceName,"address":address})

    def publish(self, msg):
        if self.__pubJson and not self.__producer:
            self.initProducer()
        appId = msg.appId
        topic = ""
        if appId in self.__pubJson:
            topic = self.__pubJson[appId]
        elif "default" in self.__pubJson:
            topic = self.__pubJson["default"]
        else:
            print("Warning: Missing default route,the message will be discarded")
            return
        print("Publish new message, topic: "+topic+" , msg: "+str(msg.__dict__)+" , timestamp:"+str(time.time()) + " , traceId:" + msg.traceId)
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

    def generateTraceId(self):
        return str(uuid.uuid4())
    # def updateCache(self, msg):
    #     if not self.__producer:
    #         self.initProducer()
    #     topic = "cache_redis"
    #     print("topic:"+topic+" msg:"+str(msg.__dict__))
    #     self.__producer.send(topic,bytes(json.dumps(msg.__dict__), encoding = "utf8"))

    @abstractmethod
    def initClient(self):
        pass
 
    def callService(deviceName,service,payload):
        pass

    def setProperties(deviceName,payload):
        pass
