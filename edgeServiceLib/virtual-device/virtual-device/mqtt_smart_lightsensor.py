import paho.mqtt.client as mqtt
import time
import random
import json

HOST = "edge-broker"
PORT = 1883
deviceName = "mqtt_smart_lightsensor"
client_id = deviceName
appId = "app_1"
topic = deviceName+"/data/post"
password = ""


def on_connect(client, userdata, flags, rc): 
    if(rc ==0):
        print ("Connection returned success")
    else:
        print ("Connection returned failed")

if __name__ == "__main__":
    client = mqtt.Client()
    client.username_pw_set(deviceName, password) 
    client.connect(HOST, PORT, 60)
    client.on_connect = on_connect
    client.loop_start()
    time.sleep(10)
    while 1:
        light = int(input("Please enter a light value:"))
        message ={
            "deviceName":deviceName,
            "appId":appId,
            "data":{
                "light":light,
            }
        }
        print("Publish new message, topic: "+topic+" , msg: "+str(message)+" , timestamp:"+str(time.time()))
        client.publish(topic,payload = json.dumps(message),qos = 0)