from client import Client,Message

import time
import random
import json
import modbus_tk
import modbus_tk.defines as cst
from modbus_tk import modbus_tcp

port = 503
collectCycle = 5
deviceName = "modbus_temperature_sensor"
client_id = deviceName
appId = "app_1"
address = 0
length = 1

if __name__ == "__main__":

    configClient = Client()
    config = configClient.getConfig()
    if config:
        if "port" in config:
            port = config["port"]
        if "collectCycle" in config:
            collectCycle = config["collectCycle"]
        if "deviceName" in config:
            deviceName = config["deviceName"]
        if "appId" in config:
            appId = config["appId"]
        if "address" in config:
            address = config["address"]
        if "length" in config:
            length = config["length"]

    try:
        server = modbus_tk.modbus_tcp.TcpServer(port=port)         
        print("Modbus slave temperature sensor is running...")
        server.start()
        slave = server.add_slave(1)
        tempValue = 25
        slave.add_block('temperature', cst.HOLDING_REGISTERS, address, length)   #address length
        slave.set_values("temperature", address, tempValue)                      #address value
        while True:
            tempValue = random.randint(20,40)
            slave.set_values("temperature", address, tempValue)
            print("New temperature value:"+str(tempValue) +", timestamp:"+str(time.time()))
            time.sleep(collectCycle)
    finally:
        server.stop()
