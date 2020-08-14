import json
import time

def on_message_come(client,msg):
    print (msg.deviceName)
    print (msg.data)
    #data = client.read("tempSensor","temp",4)
    print(data)