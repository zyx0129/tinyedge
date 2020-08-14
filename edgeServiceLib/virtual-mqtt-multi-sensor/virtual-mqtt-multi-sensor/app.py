from client import Client,Message

import paho.mqtt.client as mqtt
import time
import random
import json
from multiprocessing import Process


host = "edge-broker"
port = 1883
concurrentNum = 2
collectCycle = 10
appId = "app_1"
deviceName = "mqtt_smart_lightsensor"
topic = deviceName+"/data/post"
username = deviceName
password = ""

configClient = Client()

def dataCollect(pid,host,port,topic):
    deviceName = "mqtt_smart_lightsensor_" + str(pid)
    appId = "app_1"
    topic = deviceName+"/data/post"
    password = ""
    configClient.registerDevice(deviceName,"mqtt")
    client = mqtt.Client()
    client.username_pw_set(username, password) 
    client.connect(host, port, 60)
    client.on_connect = on_connect
    client.loop_start()
    while 1:
        light = random.uniform(100,500)
        message ={
            "deviceName":deviceName,
            "appId":appId,
            "data":{
                "light":light,
            }
        }
        print("Publish new message, topic: "+topic+" , msg: "+str(message)+" , timestamp:"+str(time.time()))
        client.publish(topic,payload = json.dumps(message),qos = 0)
        time.sleep(10)

def on_connect(client, userdata, flags, rc): 
    if(rc ==0):
        print ("Connection returned success")
    else:
        print ("Connection returned failed")

if __name__ == "__main__":   

    config = configClient.getConfig()
    if config:
        if "broker" in config:
            host = config["broker"]["host"]
            port = config["broker"]["port"]
        if "collectCycle" in config:
            collectCycle = config["collectCycle"]
        if "concurrentNum" in config:
            concurrentNum = config["concurrentNum"]
        if "topic" in config:
            topic = config["topic"]
        if "appId" in config:
            appId = config["appId"]

    for pid in range(concurrentNum):
        p = Process(target = dataCollect,args=(pid,host,port,topic,))
        p.start()
