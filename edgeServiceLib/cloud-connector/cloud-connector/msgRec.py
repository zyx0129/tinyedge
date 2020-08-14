import json

def on_message_come(mqttClient,client,msg):
    dataTopic = "edge/data/post"
    config = client.getConfig()
    if config:
        if "dataTopic" in config:
            dataTopic = config["dataTopic"]
    mqttClient.publish(dataTopic,json.dumps(msg.__dict__),0)