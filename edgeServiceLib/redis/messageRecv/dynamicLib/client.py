from kafka import KafkaConsumer
import json
from msgRec import on_message_come 
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


def Consumer():
    consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000)
    consumer.subscribe(topics = "cache_redis")
    print("start subscribe")
    for msg in consumer:
        print("msg come")
        value = msg.value
        value = str(value,"utf-8")
        print(value)
        routeMsg = Message(value)
        on_message_come(routeMsg)