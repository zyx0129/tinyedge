# import flask
# from flask import make_response, request
# import time
from appClient import AppClient

if __name__=="__main__":
    appClient = AppClient()
    appClient.startListen()


#myClient.start_consume()
# print("end")
# i= 0
# while 1:
#     print("running"+str(i))
#     i=i+1
#     time.sleep(10)
# myClient.subscribe(topic ,1)
# myClient.subscribe(topic2 ,1)

# app = flask.Flask(__name__)
# app.debug = True
#database = Database()

# while 1:
#     time.sleep(30)
# @app.route("/data/store", methods=["POST"])
# def store():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"
#     if database.connect():
#         readings=request.json["readings"]
#         time=request.json["time"]
#         deviceName=request.json["deviceName"] 
#         app.logger.info(deviceName)
        
#         app.logger.info(readings)
#         database.write(deviceName,readings,time)
#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/data/readlast", methods=["GET"])
# def readlast():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"
#     if database.connect():
#         deviceName=request.args["deviceName"]
#         identifier=request.args["property"]
#         try:
#             result = database.readlast(deviceName, identifier)
#             response["data"] = result
#         except Exception as e:
#             response["code"] = 1
#             response["message"] = str(e)
#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst

# @app.route("/data/readDataByTime", methods=["GET"])
# def readDataByTime():
#     response = {}
#     response["code"] = 0
#     response["message"] = "success"
#     if database.connect():
#         deviceName=request.args["deviceName"]
#         identifier = request.args["property"]
#         start = request.args["start"]
#         end = request.args["end"]

#         try:
#             result = database.readDataByTime(deviceName, identifier , start ,end)
#             response["data"] = result
#         except Exception as e:
#             response["code"] = 1
#             response["message"] = str(e)
#     rst = make_response(flask.jsonify(response))
#     rst.headers['Access-Control-Allow-Origin'] = '*'
#     return rst


# if __name__ == "__main__":
#     app.run(host='0.0.0.0', port=80)
