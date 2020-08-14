from client import Client,Message

import paho.mqtt.client as mqtt
import time
import random
import json


host = "edge-broker"
port = 1883
collectCycle = 10
deviceName = "mqtt_smart_lightsensor"
client_id = deviceName
appId = "app_1"
topic = deviceName+"/data/post"
username = deviceName
password = ""

mqttClient = mqtt.Client()

def connectBroker(host,port):
    try:
        mqttClient.connect(host, port, 60)
    except Exception as e:
        print(e)
        print("Waiting for broker initialization")
        time.sleep(2)
        connectBroker(host,port)


if __name__ == "__main__":

    def on_connect(client, userdata, flags, rc): 
        if(rc ==0):
            print ("Connection returned success")
        else:
            print ("Connection returned failed")

    configClient = Client()
    config = configClient.getConfig()
    if config:
        if "broker" in config:
            host = config["broker"]["host"]
            port = config["broker"]["port"]
        if "collectCycle" in config:
            collectCycle = config["collectCycle"]
        if "deviceName" in config:
            deviceName = config["deviceName"]
            username = deviceName
            topic = deviceName+"/data/post"
        if "topic" in config:
            topic = config["topic"]
        if "username" in config:
            username = config["username"]
        if "password" in config:
            password = config["password"]
        if "appId" in config:
            appId = config["appId"]

    configClient.registerDevice(deviceName,"mqtt")
    mqttClient.username_pw_set(username, password) 
    connectBroker(host,port)
    mqttClient.on_connect = on_connect
    mqttClient.loop_start()
   
    while 1:
        message ={
            "deviceName":deviceName,
            "appId":appId,
            "data":{
                "light":random.uniform(100,500)
            }
        }
        print("Publish new message, topic: "+topic+" , msg: "+str(message)+" , timestamp:"+str(time.time()))
        mqttClient.publish(topic,payload = json.dumps(message),qos = 0)
        time.sleep(collectCycle)
