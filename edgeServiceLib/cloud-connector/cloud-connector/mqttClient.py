import paho.mqtt.publish as publish
import paho.mqtt.client as mqtt

class MqttClient:
    def __init__(self,host,port,username,password):
        self.__connected = 0
        self.__client = None
        self.__host = host
        self.__port = port
        self.__username = username
        self.__password = password

    def connect(self):
        if not self.__connected:
            self.__client = mqtt.Client()
            self.__client.username_pw_set(self.__username, self.__password)
            self.__client.connect(self.__host, self.__port, 60)
            self.__client.on_connect = on_connect
            self.__client.loop_start()
            self.__connected = 1 

    def publish(self, topic, msg ,qos):
        self.__client.publish(topic,payload = msg,qos = qos)



def on_connect(client, userdata, flags, rc): 
    if(rc ==0):
        print ("Connection returned success")
    else:
        print ("Connection returned failed") 