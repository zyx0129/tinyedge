import paho.mqtt.client as mqtt
import requests
import time
import sys
import getopt
from multiprocessing import Process
import random
import json

HOST = "edge-broker"
PORT = 1883

def on_connect(client, userdata, flags, rc): 
    if(rc ==0):
        print ("Connection returned success")
    else:
        print ("Connection returned failed")

def dataCollect(pid):
    deviceName = "mqtt_smart_lightsensor_" + str(pid)
    client_id = deviceName
    appId = "app_1"
    topic = deviceName+"/data/post"
    password = ""
    client = mqtt.Client()
    client.username_pw_set(deviceName, password) 
    client.connect(HOST, PORT, 60)
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

def main(argv):
    concurrentNum = 5
    collectCycle = 10
    try:
        opts, args = getopt.getopt(argv, "hn:c:", ["concurrentNum=", "collectCycle="])
    except getopt.GetoptError:
        print('smart_multi_sensor.py -n <concurrentNum> -c <collectCycle>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('smart_multi_sensor.py -n <concurrentNum> -c <collectCycle>')
            sys.exit()
        elif opt in ("-n", "--concurrentNum"):
            concurrentNum = int(arg)
        elif opt in ("-s", "--collectCycle"):
            collectCycle = int(arg)

    for pid in range(concurrentNum):
        p = Process(target = dataCollect,args=(pid,))
        p.start()

if __name__ == "__main__":
    main(sys.argv[1:])
