import json
from base import Database

database = Database()

def on_message_come(client,msg):
    if database.connect():
        deviceName = msg.deviceName
        data = msg.data
        time = float(msg.time)
        database.write(deviceName,data,time)
        print("Data saved successfully")