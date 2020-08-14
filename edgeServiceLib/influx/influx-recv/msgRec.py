import json
from base import Database

def on_message_come(client,msg):
    database = Database()
    if database.connect():
        deviceName = msg.deviceName
        data = msg.data
        time = int(msg.time)
        database.write(deviceName,data,time)
        print("Data saved successfully")