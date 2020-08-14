from device import Device
import paho.mqtt.publish as publish
import paho.mqtt.client as mqtt
import json
import time
from client import Message

class DeviceClient(Device):
    def __init__(self):
        self.__brokerClient = None
        super(DeviceClient, self).__init__()

    def initClient(self):

        def connectBroker():
            try:
                self.__brokerClient.connect("edge-broker", 1883, 60)
            except Exception as e:
                print("Waiting for broker initialization")
                time.sleep(2)
                connectBroker()

        def on_message_come(client, userdata, msg):
            print(msg.topic + " " + ":" + str(msg.payload))
            message = json.loads(msg.payload)
            deviceName = ""
            appId = ""
            data = None
            try:
                deviceName = message["deviceName"]
                appId = message["appId"]
                data = message["data"]
            except Exception as e:
                print("wrong msg:"+str(msg.payload))
                return
            if self.getScene() and "cache" in self.getScene():
                cacheScene = self.getScene()["cache"]
                if cacheScene:
                    for property,value in data.items():
                        self.updateCache(deviceName,property,value)
            routeMsg = Message()
            routeMsg.dataType = "data"
            routeMsg.deviceName = deviceName
            routeMsg.appId = appId
            routeMsg.data = data
            self.publish(routeMsg)

        def on_connect(client, userdata, flags, rc):
            if(rc == 0):
                print("Connection returned success")
            else:
                print("Connection returned failed,waiting for next connecttion")

        self.__brokerClient = mqtt.Client()
        #client.username_pw_set(username, password)
        #self.__brokerClient.connect("edge-broker", 1883, 60)
        connectBroker()
        self.__brokerClient.on_connect = on_connect
        self.__brokerClient.loop_start()
        self.__brokerClient.subscribe("+/+/post", 1)
        #brokerClient.subscribe("/hello/data", 1)
        self.__brokerClient.on_message = on_message_come  # 消息到来处理函数

    

    def callService(self,deviceName,service,payload):
        topic = deviceName + "/service/set"
        msg = {"service": service, "payload": payload}
        self.__brokerClient.publish(topic, payload=json.dumps(msg), qos=0)

    def setProperties(self,deviceName,payload):
        topic = deviceName + "/data/set"
        msg = payload
        self.__brokerClient.publish(topic, payload=json.dumps(msg), qos=0)
    
            #time.sleep(2)
            #self.connectBroker()


    
    
