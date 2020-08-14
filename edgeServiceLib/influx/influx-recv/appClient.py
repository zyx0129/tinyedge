from kafka import KafkaConsumer
from client import Client,Message
from msgRec import on_message_come
import time
import os

class AppClient():
    def __init__(self):
        self.__consumer = None
        self.initConsumer()

    def initConsumer(self):
        try:
            self.__consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000,group_id = os.environ["MODULE_NAME"])
        except Exception as e:
            print("Waiting for service kafka to initialize...")
            time.sleep(2)
            self.initConsumer()
    
    def startListen(self):   
        configClient = Client()
        subTuple = configClient.getSubTuple()
        self.__consumer.subscribe(topics = subTuple)
        print("Connect to kafka client successfully,start to subscribe messages")
        for msg in self.__consumer:
            value = msg.value
            value = str(value,"utf-8")
            print("New message come: "+ str(value)+ " , timestamp:"+str(time.time()))
            routeMsg = Message(value)
            on_message_come(configClient,routeMsg)

