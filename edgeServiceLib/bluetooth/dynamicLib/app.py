from bluetooth import *
from multiprocessing import Process
import json
import time
import requests

hostMACAddress = ''
# port = 2
backlog = 1
size = 1024

# server_sock = BluetoothSocket()
server_sock = BluetoothSocket()
server_sock.bind(("", PORT_ANY))
server_sock.listen(backlog)
port = server_sock.getsockname()[1]


def receive():
    print("Waiting for connection on RFCOMM channel %d" % port)
    client, clientInfo = server_sock.accept()
    address = clientInfo[0]
    print("connect success,address is " + address)
    bleListen()
    try:
        data = client.recv(size)
        print(data)
        if data:
            try:
                deviceInfo = json.loads(data)
                print(deviceInfo)
                deviceName = deviceInfo["deviceName"]
                print(deviceName)
                %$@device-management%{
                r = requests.post("http://localhost:%#device-management.port/device/updateStatus", data={"deviceName": deviceName, "status": "online"})
                r = requests.post("http://localhost:%#device-management.port/device/updateAddress", data={"deviceName": deviceName, "address": address})
                %}
            except ValueError as e:
                print(str(e))
                print("Closing socket")
                client.close()
        formatData = {}
        formatData["deviceName"] = deviceName
        while 1:
            data = client.recv(size)
            if data:
                print(data)
                formatData["readings"] = data
                formatData["time"] = int(time.time())
                headers = {"Content-Type": "application/json"}
                r = requests.post(
                    "http://localhost:%#datacenter.port/data/store", data=json.dumps(formatData), headers=headers)
                # client.send(data)
    except:
    	r = requests.post("http://localhost:%#device-management.port/device/updateStatus", data={"deviceName": deviceName, "status": "offline"})
        print("Closing socket")
        client.close()


def bleListen():
    p = Process(target=receive)
    p.start()

if __name__ == "__main__":
    bleListen()
