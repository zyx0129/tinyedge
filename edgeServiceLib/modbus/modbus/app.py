import time
import json
from deviceClient import DeviceClient

if __name__=="__main__":
    deviceClient = DeviceClient()
    deviceClient.initClient()
    deviceClient.run()
