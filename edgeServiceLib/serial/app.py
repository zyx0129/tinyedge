#!/usr/bin/python
# -*-coding: utf-8 -*-

import serial
import threading
import time 
import binascii
import json
#serialPort = '/dev/ttyUSB0'  # 串口
serialPort = '/dev/pts/20'  # 串口
baudRate = 115200  # 波特率
is_exit=False
data_bytes=bytearray()

class SerialPort:
    def __init__(self, port, buand):
        self.port = serial.Serial(port, buand)
        self.port.close()
        if not self.port.isOpen():
            self.port.open()

    def port_open(self):
        if not self.port.isOpen():
            self.port.open()

    def port_close(self):
        self.port.close()

    def send_data(self):
        self.port.write('')

    def read_data(self):
        global is_exit
        global data_bytes
        while not is_exit:
            count = self.port.inWaiting()
            if count > 0:
                try:
                    rec_str = self.port.read(count)
                    print(rec_str.strip())
                    data = json.loads(rec_str.strip())
                    print ("action:"+data["action"])
                    print ("device:"+data["device"])
                    print ("data:"+json.dumps(data["data"]))
                except:
                    print ("json format wrong")
                #b = bytes(rec_str, encoding = "utf8")
                #print(b)
                #data_bytes
                # print(type(rec_str))
                # print(rec_str[0])
                # hex = binascii.b2a_hex(rec_str)
                # print(hex)
                # print(rec_str[0]==0x68)
                #data_bytes=data_bytes+rec_str

                #print(data_bytes)
                #print('当前数据接收总字节数：'+str(len(data_bytes))+' 本次接收字节数：'+str(len(rec_str)))
                #print(str(datetime.now()),':',binascii.b2a_hex(rec_str))




if __name__ == '__main__':
    #打开串口
    mSerial = SerialPort(serialPort, baudRate)
    t1 = threading.Thread(target=mSerial.read_data)
    t1.setDaemon(True)
    t1.start()
    while 1:
        time.sleep(10)