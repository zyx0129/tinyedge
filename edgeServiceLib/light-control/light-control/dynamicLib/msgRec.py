import json
import time

def on_message_come(client,msg):
    print (msg.deviceName)
    print (msg.data)
    if msg.deviceName == "lightSensor1":
        if msg.data["light"]>200:
            #client.publish(msg)
            client.callDeviceService("led1","turnOff","")   #deviceName serviceName params
        elif msg.data["light"]<100:
            client.callDeviceService("led1","turnOn","")   #deviceName serviceName params