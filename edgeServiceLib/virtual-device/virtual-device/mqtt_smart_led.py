import sys
import getopt
import paho.mqtt.client as mqtt
import time
import random
import json


HOST = "edge-broker"
PORT = 1883
deviceName = "mqtt_smart_led"
client_id = deviceName
appId = "app_2"
dataTopic = deviceName+"/data/post"
serviceTopic = deviceName+"/service/set"
password = ""
led = 0

def main(argv):
    target = "edge"
    led = 0
    try:
        opts, args = getopt.getopt(argv, "ht:l:", ["target=", "led="])
    except getopt.GetoptError:
        print('virtual_mqtt_sensor.py -t <target> -l <led>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('virtual_mqtt_sensor.py -t <target> -l <led>')
            sys.exit()
        elif opt in ("-t", "--target"):
            target = arg
        elif opt in ("-l", "--led"):
            led = arg

    

    def on_connect(client, userdata, flags, rc): 
        if(rc ==0):
            print ("Connection returned success")
        else:
            print ("Connection returned failed")

    def on_message_come(client, userdata, msg):
        print("New message come: "+ str(msg.payload)+ " , timestamp:"+str(time.time()))
        #print(msg.topic + " " + ":" + str(msg.payload))
        message = json.loads(msg.payload)
        if "service" in message:
            service = message["service"]
            if service == "turnOnLed":               
                led = 1
                print("Led turnOn success")
                message ={
                    "deviceName":deviceName,
                    "appId":appId,
                    "data":{
                        "led":led,
                    }
                }
                client.publish(dataTopic,payload = json.dumps(message),qos = 0)
            elif service == "turnOffLed":
                led = 0
                print("Led turnOff success")
                message ={
                    "deviceName":deviceName,
                    "appId":appId,
                    "data":{
                        "led":led,
                    }
                }
                client.publish(dataTopic,payload = json.dumps(message),qos = 0)
            else:
                print("Undefined service:"+service)
    

    def on_subscribe():
        client.subscribe(serviceTopic, 1)
        client.on_message = on_message_come 

    client = mqtt.Client()
    client.username_pw_set(deviceName, password) 
    client.connect(HOST, PORT, 60)
    client.on_connect = on_connect  
    client.loop_start()
    on_subscribe()
    message ={
        "deviceName":deviceName,
        "appId":appId,
        "data":{
            "led":led,
        }
    }
    client.publish(dataTopic,payload = json.dumps(message),qos = 0)

    while 1:
        pass


if __name__ == "__main__":
    main(sys.argv[1:])