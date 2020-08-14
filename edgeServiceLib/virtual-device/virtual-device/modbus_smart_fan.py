import sys
import time
import modbus_tk
import modbus_tk.defines as cst
from modbus_tk import modbus_tcp


def main():      
    server = modbus_tk.modbus_tcp.TcpServer(port=504)         
    server.start()
    print("Modbus slave fan is running...")
    slave = server.add_slave(1)
    address = 0
    length = 1
    fanStatus = 0
    slave.add_block('fan', cst.HOLDING_REGISTERS, address, length)   #address length
    slave.set_values("fan", address, fanStatus)


if __name__ == "__main__":
    main()