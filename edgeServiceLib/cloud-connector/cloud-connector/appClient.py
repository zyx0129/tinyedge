from kafka import KafkaConsumer
from client import Client,Message
from msgRec import on_message_come
import time
import os
import paho.mqtt.client as mqtt

class AppClient():
    def __init__(self):
        self.__consumer = None
        self.__mqttClient = mqtt.Client()
        self.__configClient = Client()
        self.initMqtt()
        self.initConsumer()

    def initConsumer(self):
        try:
            self.__consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000,group_id = os.environ["MODULE_NAME"])
        except Exception as e:
            print("Waiting for service kafka to initialize...")
            time.sleep(2)
            self.initConsumer()
    
    def initMqtt(self):
        host = "locolhost"
        port = "1883"
        deviceKey = "deviceKey"
        deviceSecret = ""
        serviceTopic = deviceKey+"/service"
        config = self.__configClient.getConfig()
        if config:
            if "broker" in config:
                host = config["broker"]["host"]
                port = config["broker"]["port"]
            if "deviceKey" in config:
                deviceKey = config["deviceKey"]
            if "deviceSecret" in config:
                deviceSecret = config["deviceSecret"]
            if "serviceTopic" in config:
                serviceTopic = config["serviceTopic"]

        def on_connect(client, userdata, flags, rc): 
            if(rc ==0):
                print ("Connection returned success")
            else:
                print ("Connection returned failed")

        def on_disconnect(client, userdata, rc):
            if rc != 0:
                print("Unexpected disconnection.")

        def on_message_come(client, userdata, msg):
            print("New message come: "+ str(msg.payload)+ " , timestamp:"+str(time.time()))
            #print(msg.topic + " " + ":" + str(msg.payload))
            message = json.loads(msg.payload)
            if "service" in message:
                service = message["service"]
                if service == "turnOnLed":               
                    led = 1
                    print("Led turnOn success")
                    message ={
                        "deviceName":deviceName,
                        "appId":appId,
                        "data":{
                            "led":led,
                        }
                    }
                    client.publish(dataTopic,payload = json.dumps(message),qos = 0)
                elif service == "turnOffLed":
                    led = 0
                    print("Led turnOff success")
                    message ={
                        "deviceName":deviceName,
                        "appId":appId,
                        "data":{
                            "led":led,
                        }
                    }
                    client.publish(dataTopic,payload = json.dumps(message),qos = 0)
                else:
                    print("Undefined service:"+service)
        
        def on_subscribe():
            self.__mqttClient.subscribe(serviceTopic, 1)
            self.__mqttClient.on_message = on_message_come 

        def connectBroker():
            try:
                self.__mqttClient.connect(host, port, 60)
            except Exception as e:
                print(e)
                print("Waiting for broker initialization")
                time.sleep(2)
                connectBroker()
        
        self.__mqttClient.username_pw_set(deviceKey, deviceSecret) 
        connectBroker()
        self.__mqttClient.on_connect = on_connect
        self.__mqttClient.on_disconnect = on_disconnect
        on_subscribe()
        self.__mqttClient.loop_start()
  

    def startListen(self):
        subTuple = self.__configClient.getSubTuple()
        self.__consumer.subscribe(topics = subTuple)
        print("Connect to kafka client successfully,start to subscribe messages")
        for msg in self.__consumer:
            value = msg.value
            value = str(value,"utf-8")
            print("New message come: "+ str(value)+ " , timestamp:"+str(time.time()))
            routeMsg = Message(value)
            on_message_come(self.__mqttClient,self.__configClient,routeMsg)

