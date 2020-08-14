import sys
import time
import modbus_tk
import modbus_tk.defines as cst
from modbus_tk import modbus_tcp


def main():
    try:
        server = modbus_tk.modbus_tcp.TcpServer(port=503)         
        print("Modbus slave temperature sensor is running...")
        server.start()
        slave = server.add_slave(1)
        address = 0
        length = 1
        tempValue = 25
        slave.add_block('temperature', cst.HOLDING_REGISTERS, address, length)   #address length
        slave.set_values("temperature", address, tempValue)                      #address value
        while True:
            tempValue = int(input("Please enter a temperature value:"))
            slave.set_values("temperature", address, tempValue)
            print("New temperature value:"+str(tempValue) +", timestamp:"+str(time.time()))
    finally:
        server.stop()


if __name__ == "__main__":
    main()