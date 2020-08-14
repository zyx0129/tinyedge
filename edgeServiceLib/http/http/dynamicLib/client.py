from kafka import KafkaConsumer
from kafka import KafkaProducer
import json 

class Producer:
    def __init__(self):
        self.__pubJson = {}
        with open("/app/config/router.json",'r') as load_f:
            self.__pubJson = json.load(load_f)["http"]["pub"]
        self.__producer = KafkaProducer(bootstrap_servers='edge-kafka:9092')
    def publish(self, msg):
        msg = json.loads(msg)
        deviceName = msg["deviceName"]
        topic = ""
        if deviceName in self.__pubJson:
            topic = self.__pubJson[deviceName]
        elif "default" in self.__pubJson:
            topic = self.__pubJson["default"]
        else:
            print("lack default router")
            return
        print("topic:"+topic+" msg:"+str(msg))
        self.__producer.send(topic,bytes(json.dumps(msg), encoding = "utf8"))

