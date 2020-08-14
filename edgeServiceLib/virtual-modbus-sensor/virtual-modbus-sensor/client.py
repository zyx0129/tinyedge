from kafka import KafkaProducer
import json 
import requests
#import redis
import time
import os

class Message(object):
    def __init__(self,msg = None):
        if msg:
            try:
                msg = json.loads(msg)
                self.dataType = msg["dataType"]
                self.data = msg["data"]
                self.deviceName = msg["deviceName"]
                self.time = msg["time"]
                self.appId = msg["appId"]
            except Exception as e:
                print("wrong msg:"+str(msg))
        else:
            self.data = None
            self.dataType = None
            self.deviceName = None
            self.time = time.time()
            self.appId = None

class Client:
    def __init__(self):
        self.__scene = None
        self.__config = None
        self.__serviceList = {}
        self.__subTuple = None
        self.__pubJson = {}
        self.__producer = None
        self.__redis = None
        self.__moduleName = os.environ["MODULE_NAME"]
        self.updateConfig()

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

    def getSubTuple(self):
        return self.__subTuple

    def initProducer(self):
        try:
            self.__producer = KafkaProducer(bootstrap_servers='edge-kafka:9092')
        except Exception as e:
            print("Waiting for service kafka to initialize...")
            time.sleep(2)
            self.initProducer()

    def initRedis(self):
        try:
            self.__redis = redis.Redis(host='edge-cache', port=6379, decode_responses=True)
        except:
            print("Waiting for service redis to initialize...")
            time.sleep(2)
            self.initRedis()

    def publish(self, msg):
        if not self.__producer:
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
        print("Publish new message, topic: "+topic+" , msg: "+str(msg.__dict__)+" , timestamp:"+str(time.time()))
        self.__producer.send(topic,bytes(json.dumps(msg.__dict__), encoding = "utf8"))

    def callDeviceService(self, deviceName,service,payload):
        if not self.__producer:
            self.initProducer()
        r = requests.get("http://edge-device-management/device/connector?deviceName="+deviceName)
        connector = r.json()["data"]
        topic = ""
        msg ={}
        msg["type"] = "callService"
        msg["deviceName"] = deviceName
        msg["service"] = service
        msg["payload"] = payload
        topic = "command_" + connector 
        print("topic:"+topic+" msg:"+str(msg))
        self.__producer.send(topic,bytes(json.dumps(msg), encoding = "utf8"))

    def setDeviceProperties(self, deviceName,payload):
        if not self.__producer:
            self.initProducer()
        r = requests.get("http://edge-device-management/device/connector?deviceName="+deviceName)
        connector = r.json()["data"]
        topic = ""
        msg ={}
        msg["type"] = "setValue"
        msg["deviceName"] = deviceName
        msg["payload"] = payload
        topic = "command_" + connector 
        print("topic:"+topic+" msg:"+str(msg))
        self.__producer.send(topic,bytes(json.dumps(msg), encoding = "utf8"))

    def read(self, deviceName,property,num = 1,freshness = 60):
        if(num<1):
            return None
        if not self.__redis:
            self.initRedis()
        #value = r.get(deviceName+":"+property)
        key = deviceName+":"+property
        length = self.__redis.llen(key)
        if length>=num:
            dataList = self.__redis.lrange(key, length-num, -1)
            valueList = []
            for data in dataList:
                data = json.loads(data)
                value = data["value"]
                if int(time.time())-int(data["time"])<freshness:
                    valueList.append(value)
                else:
                    print("Now time is " + str(int(time.time()))+" , "+str(data["time"])+" is too old")
            return valueList
        else:
            print("not enought")
            return None

    def updateCache(self, deviceName,property,value, uploadTime = 0):
        def handleList(name,length,value):
            if self.__redis.llen(name)==0:
                self.__redis.rpush(name, value)
            elif self.__redis.llen(name)==length:
                self.__redis.lpop(name)
                self.__redis.rpushx(name, value)
            else:
                self.__redis.rpushx(name, value) 
        if not self.__redis:
            self.initRedis()
        nowTime = uploadTime
        if uploadTime:
            nowTime = uploadTime
        else:
            nowTime = time.time()
        handleList(deviceName+":"+property,5,json.dumps({"value":value,"time":nowTime}))

    def registerDevice(self,deviceName,connector):
        if not self.checkServiceIfExist("device-management"):
            return 0
        try:
            r = requests.post("http://edge-device-management/device/register", data = {"deviceName":deviceName,"connector":connector})
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
            return self.registerDevice(deviceName,connector)