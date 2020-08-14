import time
import json
from deviceClient import DeviceClient

if __name__=="__main__":
    deviceClient = DeviceClient()
    deviceClient.initClient()
    deviceClient.run()
# time.sleep(15)
# myClient = Producer()

# HOST = "edge-broker"
# PORT = 1883
# #client_id = datetime.datetime.now()
# #username = "TinyLink"
# username = "21821196"
# password = "123456"


# def on_connect(client, userdata, flags, rc):
#     if(rc == 0):
#         print("Connection returned success")
#     else:
#         print("Connection returned failed")


# def on_message_come(client, userdata, msg):
#     print(msg.topic + " " + ":" + str(msg.payload))
#     message = json.loads(msg.payload)
#     routeMsg = Message()
#     try:
#         routeMsg.dataType = "data"
#         routeMsg.deviceName = message["deviceName"]
#         routeMsg.data = message["data"]
#     except Exception as e:
#         print("wrong msg:"+str(msg.payload))
#     myClient.publish(routeMsg)
#     %$*cache%{#cache
#     myClient.updateCache(routeMsg)
#     %}

# def on_subscribe():
#     #brokerClient.subscribe("+/data", 1)
#     brokerClient.subscribe("+/+/post", 1)
#     #brokerClient.subscribe("/hello/data", 1)
#     brokerClient.on_message = on_message_come  # 消息到来处理函数

# brokerClient = mqtt.Client()
# #client.username_pw_set(username, password)
# brokerClient.connect(HOST, PORT, 60)
# brokerClient.on_connect = on_connect
# brokerClient.loop_start()
# on_subscribe()


# # commandHandle(deviceName,command,params)

# consumer = KafkaConsumer(
#     bootstrap_servers=['edge-kafka:9092'], metadata_max_age_ms=1000)
# consumer.subscribe(topics="command_mqtt")
# for msg in consumer:
#     print("msg come")
#     value = msg.value
#     print(value)
#     value = str(value, "utf-8")
#     value = json.loads(value)
#     deviceName = value["deviceName"]
#     command = value["command"]
#     params = value["params"]
#     topic = deviceName + "/service/set"
#     msg = {"command": command, "params": params}
#     brokerClient.publish(topic, payload=json.dumps(msg), qos=0)

# while 1:
#     time.sleep(15)
