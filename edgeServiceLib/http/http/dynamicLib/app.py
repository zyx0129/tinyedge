import flask
from flask import make_response, request
import requests
import time
import json
from client import Producer

app = flask.Flask(__name__)
app.debug = True
time.sleep(15)
mylient = Producer()


@app.route("/http/upload", methods=["POST"])
def uploadData():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    app.logger.info(request.json)
    readings = request.json
    formatData = {}
    deviceName = request.json["deviceName"]
    readings = request.json["readings"] 
    formatData["deviceName"] = deviceName
    app.logger.info(deviceName)
    %$*deviceAuth%{#认证
    deviceName=request.json["deviceName"]
    password=request.headers.get("password")
    app.logger.info(password)
    r = requests.post("http://edge-device-management/auth", data = {"deviceName":deviceName,"password":password})
    app.logger.info(r.content)
    if(r.json()["code"]==0):
        print ("device "+deviceName+" auth success")
    else:
        response["code"] = 1
        response["message"] = "device "+deviceName+" auth failed"
        rst = make_response(flask.jsonify(response))
        rst.headers['Access-Control-Allow-Origin'] = '*'
        return rst
    %}

    readingValue = list(readings.values())[0]
    if isinstance(readingValue,(int,str)):
        formatData["readings"] = readings
        formatData["time"] = int(time.time())
        app.logger.info(formatData)
        headers = {"Content-Type": "application/json"} 
        mylient.publish(json.dumps(formatData))
    address = request.remote_addr
    r = requests.post("http://edge-device-management/device/updateAddress", data = {"deviceName":deviceName,"address":address} )
    #{"temp":15,"humi":16} => {"fileds":{"temp":15,"humi":16},"time":22223233}
    #{"temp":{"value":15,"time":155555644545},"humi":{"value":15,"time":155555644545}}
    #{"temp":[{"value":17,"time":155555644545},{"value":19,"time":1555556445545}]}
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst


@app.route("/http/stream", methods=["POST"])
def uploadStream():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    app.logger.info(request.json)
    readings = request.json
    formatData = {}
    deviceName = request.json["deviceName"]
    readings = request.json["readings"] 
    formatData["deviceName"] = deviceName
    app.logger.info(deviceName)
    %$*deviceAuth%{#认证
    password=request.headers.get("password")
    app.logger.info(password)
    r = requests.post("http://edge-authentication/auth", data = {"deviceName":deviceName,"password":password})
    app.logger.info(r.content)
    if(r.json()["code"]==0):
        print ("device "+deviceName+" auth success")
    else:
        response["code"] = 1
        response["message"] = "device "+deviceName+" auth failed"
        rst = make_response(flask.jsonify(response))
        rst.headers['Access-Control-Allow-Origin'] = '*'
        return rst
    %}
    
    
    
    for key, value in readings.items():
        for item in value:
            formatData["readings"] = {key: item["value"]}
            formatData["time"] = item["time"]
            headers = {"Content-Type": "application/json"} 
            r = requests.post("http://edge-datacenter/data/store", data = json.dumps(formatData) ,headers = headers)
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=80)