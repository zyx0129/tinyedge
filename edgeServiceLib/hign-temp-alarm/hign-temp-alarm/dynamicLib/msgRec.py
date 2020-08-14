import json
import time

def on_message_come(client,msg):
    print (msg.deviceName)
    print (msg.data)
    data = client.read(msg.deviceName,"temperature",4)
    print(data)