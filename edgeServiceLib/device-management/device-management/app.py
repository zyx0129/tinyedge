# encoding:utf-8

# USAGE
# Start the server:
# python app.py
# Submit a request via cURL:
# curl -X POST -H "Content-Type:application/json" -d   '"action":"client_connected","username":"myled"' localhost

# import the necessary packages
import flask
import json
from flask import make_response, request
#from multiprocessing import Process
import time
#import psutil
import hashlib
import datetime
from base import JsonObj

# initialize our Flask application and the Keras model
app = flask.Flask(__name__)
#app.debug = True
jsonObj = JsonObj();
deviceInfo = jsonObj.read()


@app.route("/device/register", methods=["POST"])
def registerDevice():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    
    deviceName = request.form["deviceName"]
    connector = request.form["connector"]
    #%$*deviceAuth%{
    deviceSecret = hashlib.md5(deviceName.encode()).hexdigest()
    #%}
    if deviceName in deviceInfo:
        response["code"] = 1
        response["message"] = "The device name " + deviceName + " has been registered"
    else:
        deviceInfo[deviceName] = {}
        #%$*deviceAuth%{
        deviceInfo[deviceName]["deviceSecret"] = deviceSecret
        #%}
        deviceInfo[deviceName]["connector"] = connector
        deviceInfo[deviceName]["createTime"] = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        deviceInfo[deviceName]["status"] = "unactivated"
        deviceInfo[deviceName]["address"] = ""
        deviceInfo[deviceName]["changed"] = ""
        if connector == "mqtt":
            deviceInfo[deviceName]["topics"] = {}
            deviceInfo[deviceName]["topics"]["dataTopic"] = deviceName+"/data/post"
            deviceInfo[deviceName]["topics"]["eventTopic"] = deviceName+"/event/post"
            deviceInfo[deviceName]["topics"]["serviceTopic"] = deviceName+"/service/set"
            deviceInfo[deviceName]["topics"]["dataSetTopic"] = deviceName+"/data/set"
        jsonObj.write(deviceInfo)
    # sql = "select id from device where name = '{}'".format(device_name)
    # response["data"] = {
    #     "deviceName": device_name,  # 设备名
    #     "deviceSecret": password,  # 设备密钥
    #     "deviceId": device_id  # 设备id
    # }

    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

@app.route("/auth", methods=["POST"])
def auth():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    deviceName = request.form["deviceName"]
    password = request.form["password"]
    match_password = deviceInfo[deviceName]["deviceSecret"]
    if password == match_password:
        return make_response(flask.jsonify(response))
    else:
        response["code"] = 1
        response["message"] = "device " + deviceName + "password not match"
        return make_response(flask.jsonify(response))


@app.route("/device/updateStatus", methods=["POST"])
def updateDeviceStatus():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    deviceName = request.form["deviceName"]
    status = request.form["status"]
    updateStatus(deviceName,status)
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

@app.route("/device/mqtt/updateStatus", methods=["POST"])
def updateMqttStatus():
    data = request.get_data()
    app.logger.info(data)
    data = json.loads(data)
    action = data["action"]
    deviceName = data["username"]
    status = ""
    if action=="client_connected":
        status="online"
        address = data["ipaddress"]
        if deviceName in deviceInfo:
            deviceInfo[deviceName]["address"] = address
    elif action=="client_disconnected":
        status="offline"
    updateStatus(deviceName,status)
    return "200"

@app.route("/device/updateAddress", methods=["POST"])
def updateAddress():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    deviceName = request.form["deviceName"]
    address = request.form["address"]
    deviceInfo[deviceName]["address"] = address
    jsonObj.write(deviceInfo)
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

@app.route("/device/offLineDevice", methods=["POST"])
def offLineDevice():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    onelineDiviceList = getOnlineDevice("http")
    for deviceName in onelineDiviceList:
        updateStatus(deviceName,"offline")
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

def updateStatus(deviceName,status):
    if deviceName in deviceInfo:
        deviceInfo[deviceName]["status"] = status
        deviceInfo[deviceName]["changed"] = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        jsonObj.write(deviceInfo)

@app.route("/device/listDevices", methods=["GET"])
def listDevices():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    response["data"] = "No device has been registered"
    result = []
    for key, value in deviceInfo.items():
        info = {
            "deviceName":key,
            "status":value["status"],
            "connector": value["connector"],
            "address": value["address"],
        }
        result.append(info)
        response["data"] = result
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst



@app.route("/device/listDeviceDetail", methods=["GET"])
def listDeviceDetail():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    deviceName = request.args["deviceName"]
    if deviceName in deviceInfo:
        device = deviceInfo[deviceName]
        lastOnlineTime = "-"
        if device["status"] == "offline":
            lastOnlineTime = device["changed"]
        result = {
            "deviceName": deviceName,
            "status": device["status"],
            "lastOnlineTime": lastOnlineTime,
            "createTime": device["createTime"],
            "connector": device["connector"],
            "address": device["address"],
            #%$*deviceAuth%{"deviceSecret": device["deviceSecret"],%}
        } 
        if device["connector"]=="mqtt":
            result["topics"] = device["topics"]
    else:
        response["code"] = 1
        response["message"] = "The device "+deviceName+" is not existed"
    response["data"] = result
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

@app.route("/device/connector", methods=["GET"])
def getConnector():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    deviceName = request.args["deviceName"]
    if deviceName in deviceInfo:
        device = deviceInfo[deviceName]
        connector = device["connector"]
        response["data"] = connector
    else:
        response["data"] = ""
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

def getOnlineDevice(connector = "http"):
    if connector:
        sql = "select name from device where status = 'online' and connector='"+connector+"'"
    else:
        sql = "select name from device where status = 'online'"
    onelineDiviceList = []
    for key,value in deviceInfo.items():
        if value["status"] == "online" and (not connector or connector == value["connector"]):
            onelineDiviceList.append(key)
    return onelineDiviceList

# %$#device-management.ObjectModel.property%{
# @app.route("/device/createProperty", methods=["POST"])
# def createProperty():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))
#     deviceId = request.form["deviceId"]
#     identifier = request.form["identifier"]
#     dataType = request.form["dataType"]

#     sql = "insert into property (device_id, identifier, datatype) values ({},'{}','{}')".format(
#         deviceId,
#         identifier,
#         dataType)
#     if not mysql.execute(sql):
#         response["code"] = 2
#         response["message"] = "Fail to write mysql database: {}".format(res)
#         return make_response(flask.jsonify(response))

#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/device/listProperties", methods=["GET"])
# def listProperties():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     response["data"] = []
#     try:
#         deviceId = request.args["deviceId"]
#         sql = 'select id,identifier from property where device_id={}'.format(deviceId)
#         properties = mysql.fetchall(sql)
#         result = [
#             {
#                 "propertyId": item[0],
#                 "propertyName": item[1],
#             } for item in properties]
#         response["data"].append(result)
#     except Exception as e:
#         response["code"] = 2
#         response["message"] = "Fail to read mysql database: {}".format(res)
#         return make_response(flask.jsonify(response))

#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst
# %}   

# %$#device-management.virtualSensor%{
# @app.route("/device/createVirtualSensor", methods=["POST"])
# def createVirtualSensor():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     vsName = request.form["vsName"]
#     input = request.form["input"]
#     labels = request.form["labels"]

#     try:
#         sql = "insert into virtual_sensor(name, input, labels) VALUES ('{}','{}','{}')".format(vsName, input, labels)
#         mysql.execute(sql)
#     except Exception as e:
#         response["code"] = 2
#         response["message"] = "Fail to write mysql database: {}".format(str(e))
#         return make_response(flask.jsonify(response))

#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/device/listVirtualSensors", methods=["GET"])
# def listVirtualSensors():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     try:
#         sql = "select id, name, input, labels from virtual_sensor"
#         sensors = mysql.fetchall(sql)
#         sensors = [{"vsId": sensor[0], "vsName": sensor[1], "input": sensor[2], "labels": sensor[3]} for sensor in
#                    sensors]
#         response["data"] = sensors
#     except Exception as e:
#         response["code"] = 2
#         response["message"] = "Fail to read mysql database: {}".format(e)
#         return make_response(flask.jsonify(response))

#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/device/addSample", methods=["POST"])
# def addSample():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     vsId = request.form["vsId"]
#     label = request.form["label"]
#     start = request.form["start"]
#     end = request.form["end"]

#     try:
#         sql = "insert into sample(vs_id, label, start, end) VALUES ({},'{}','{}','{}')".format(vsId, label, start,end)
#         mysql.execute(sql)
#     except Exception as e:
#         response["code"] = 2
#         response["message"] = "Fail to write mysql database: {}".format(str(e))
#         return make_response(flask.jsonify(response))

#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/device/virtualSensor/predict", methods=["GET"])
# def predict():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"
#     response["data"] = {}
#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     vsId = request.args["vsId"]
#     start = request.args["start"]
#     end = request.args["end"]
#     trainX = []
#     trainY = []
#     testX = []
#     samples = get_samples(vsId);
#     virtual_sensor = get_virtual_sensor(vsId)
#     inputs = virtual_sensor["input"].strip().split(";") #device1:acc,gyro;device2:temp
#     labels = virtual_sensor["labels"].strip().split(",")
#     lableSamples = count_label_samples(vsId,labels)
#     for label in labels:
#         if lableSamples[label] == 0:
#             response["code"] = 1
#             response["message"] = "label "+ label + " doesn't have enough samples"
#             return make_response(flask.jsonify(response))
#     sensors = []
#     for device in inputs:
#         split = device.split(":")
#         device_name = split[0]
#         properties = split[1].split(",")
#         for property in properties:
#             sensors.append({"deviceName":device_name,"property":property})

#     #[[[],[]],[[],[]],[[],[]],[[],[]]] 4组,2个传感器
#     for sample in samples:
#         sampleData = []
#         for sensor in sensors:
#             r = requests.get("http://edge-datacenter/data/readDataByTime?deviceName="+sensor["deviceName"]+"&property="+sensor["property"]+"&start="+str(sample["start"])+"&end="+str(sample["end"]))
#             app.logger.info(r.json())
#             sensorData = r.json()["data"]
#             sampleData.append(sensorData)
#         trainX.append(sampleData)
#         trainY.append(sample["label"])

#     app.logger.info(trainX)
#     app.logger.info(trainY)
#     #test
#     sampleData = []
#     for sensor in sensors:
#         r = requests.get("http://edge-datacenter/data/readDataByTime?deviceName="+sensor["deviceName"]+"&property="+sensor["property"]+"&start="+str(start)+"&end="+str(end))
#         sensorData = r.json()["data"]
#         sampleData.append(sensorData)
#     testX.append(sampleData)
#     #response["data"] = "open"
#     #testX = trainX
#     app.logger.info(testX)
#     formatData = {}
#     formatData["trainX"] = trainX
#     formatData["trainY"] =trainY
#     formatData["testX"] = testX
#     headers = {"Content-Type": "application/json"} 
#     r = requests.post("http://edge-ml/machineLearning/svm/predict", data = json.dumps(formatData) ,headers = headers)
#     predict = r.json()["data"]["predict"][0]
#     response["data"]["predict"] = predict
#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/device/virtualSensor/checkSamples", methods=["GET"])
# def check_samples():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"

#     if not mysql.connected:
#         res = mysql.connect()
#         if res is not None:
#             response["code"] = 1
#             response["message"] = "Fail to connect mysql database: {}".format(res)
#             return make_response(flask.jsonify(response))

#     vsId = request.args["vsId"]
#     virtual_sensor = get_virtual_sensor(vsId)
#     labels = virtual_sensor["labels"].strip().split(",")
#     lableSamples = count_label_samples(vsId,labels)
#     response["data"] = lableSamples
#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# def count_label_samples(vsId,labels):
#     info = {}
#     for label in labels:
#         sql = "select count(*) from sample where vs_id = {} and label = '{}'".format(vsId,label)
#         results = mysql.fetchone(sql)
#         info[label] = results[0]
#     app.logger.info(info)
#     return info


# def get_samples(vsId):
#     sql = "select label, start, end from sample where vs_id = {} ".format(vsId)
#     results = mysql.fetchall(sql)
#     results = [{"label": result[0], "start": result[1], "end": result[2]} for result in results]
#     return results

# def get_virtual_sensor(vsId):
#     sql = "select id, name, input, labels from virtual_sensor where id = {}".format(vsId)
#     sensor = mysql.fetchone(sql)
#     sensor = {"vsId": sensor[0], "vsName": sensor[1], "input": sensor[2], "labels": sensor[3]}
#     return sensor
# %}






# def A():
#     time.sleep(20)
#     while 1:
#         time.sleep(30)
#         r = requests.post("http://127.0.0.1/device/offLineDevice")
#         # onelineDiviceList = getOnlineDevice()
#         # for deviceName in onelineDiviceList:
#         #     updateStatus(deviceName,"offline")

# def getMemCpu():
#     data = psutil.virtual_memory()
#     total = data.total  # 总内存,单位为byte
#     print('total',total)
#     free = data.available  # 可用内存
#     print('free',free)
#     memory = "%d" % (int(round(data.percent))) + "%" + " "  # 内存使用情况
#     print('memory',memory)
#     cpu = "%0.2f" % psutil.cpu_percent(interval=1) + "%"  # CPU占用情况
#     print('cpu',cpu)
#     disk = "%0.2f" % psutil.disk_usage('/').percent + "%" + " "  # 内存使用情况
#     print('disk',disk)
#     return cpu,memory,disk


# def monitorSystem():
#     time.sleep(20)
#     while 1:
#         cpu,memory,disk = getMemCpu()
#         #%$@cloud%{
#         msg ={"type":"system","info":{"cpu":cpu,"memory":memory, "disk":disk}}
#         print(msg)
#         headers = {"Content-Type": "application/json"} 
#         r = requests.post("http://edge-cloud/cloud/upload",data = json.dumps(msg),headers = headers) 
#         #%}
#         time.sleep(5)



# if this is the main thread of execution first load the model 
# and then start the server
if __name__ == "__main__":
    # p1 = Process(target = A)
    # p1.start()
    # p2 = Process(target = monitorSystem)
    # p2.start()
    app.run(host='0.0.0.0',port=80)