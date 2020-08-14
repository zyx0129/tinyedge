from kafka import KafkaConsumer
from kafka import KafkaProducer
import json 
import requests
import redis
import time

class Message(object):
    def __init__(self,msg = None):
        if msg:
            try:
                msg = json.loads(msg)
                self.dataType = msg["dataType"]
                self.data = msg["data"]
                self.deviceName = msg["deviceName"]
                self.time = msg["time"]
            except Exception as e:
                print("wrong msg:"+str(msg))
        else:
            self.data = None
            self.dataType = None
            self.deviceName = None
            self.time = time.time()
