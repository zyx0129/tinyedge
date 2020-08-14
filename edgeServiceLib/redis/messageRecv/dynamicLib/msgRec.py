import json
from base import Redis

def on_message_come(msg):
    print (msg.deviceName)
    print (msg.data)
    redis = Redis()
    if redis.connect():
        deviceName = msg.deviceName
        data = msg.data
        for key,value in data.items():
            print(key)
            print(value)
            redis.updateData(deviceName,key,value)