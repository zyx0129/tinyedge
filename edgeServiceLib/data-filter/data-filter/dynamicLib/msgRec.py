import json

def on_message_come(client,msg):
    print (msg.deviceName)
    print (msg.data)
    if "temperature" in msg.data:
        if msg.data["temperature"]>25:
            client.publish(msg)