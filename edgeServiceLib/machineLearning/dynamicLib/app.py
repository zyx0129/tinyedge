#!/usr/bin/python
# -*- coding: UTF-8 -*-
import flask
import json
from flask import make_response, request
from sklearn.svm import SVC
import numpy as np

# initialize our Flask application and the Keras model
app = flask.Flask(__name__)
app.debug = True

def featureSelect(x):
    #最大值
    max_value = max(x)
    #最小值
    min_value = min(x)
    #平均值
    mean_value = np.mean(x)
    mean_value = round(mean_value, 2)
    #中位数
    median = np.median(x)
    median = round(median, 2)
    #25百分数
    p25 = np.percentile(x, 25)
    p25 = round(p25, 2)
    #75百分数
    p75 = np.percentile(x, 75)
    p75 = round(p75, 2)
    #方差
    x = np.array(x)
    var = x.var()
    var = round(var, 2)
    #标准差
    std = np.std(x)
    std = round(std, 2)
    #极差
    magnitude = max_value - min_value
    magnitude = round(magnitude, 2)
    #峰值数量
    pc = peak_count(x)
    return [max_value, min_value, mean_value, median, p25, p75, var, std, magnitude, pc]

def preprocessDataset(dataSet):
    newSets = []
    #2维
    if np.array(dataSet).ndim==2:
        for data in dataSet:
            newSets.append(featureSelect(data))
    if np.array(dataSet).ndim==3:
        for data in dataSet:
            sensor = []
            for x in data:
                sensor.append(featureSelect(x))
            newSets.append(sensor)
        app.logger.info(newSets)
        newSets = np.array(newSets)
        nsamples, nx, ny = newSets.shape
        newSets = newSets.reshape((nsamples, nx * ny))
        app.logger.info(newSets)
    return newSets

def peak_count(data):
    '''Counts the number of peaks in given timesries data'''
    count = 0
    std = np.std(data)
    for idx in range(len(data) - 2):
        if data[idx + 1] > std * 2 and (data[idx + 1] - data[idx]) * (data[idx + 2] - data[idx + 1]) < 0:
            count = count + 1
    return count

@app.route("/machineLearning/svm/predict", methods=["POST"])
def svmPredict():
    response = {}
    response["code"] = 0
    response["message"] = "success"
    trainX = request.json["trainX"]
    trainY = request.json["trainY"]
    testX = request.json["testX"]
    app.logger.info(np.array(trainX).ndim)
    trainX =preprocessDataset(trainX)
    testX = preprocessDataset(testX)
    app.logger.info(trainX)
    app.logger.info(trainY)
    app.logger.info(testX)
    clf = SVC(gamma='auto')
    clf.fit(trainX, trainY)
    predict = clf.predict(testX).tolist()
    app.logger.info(type(predict))
    result ={
        "predict": predict
    } 
    response["data"] = result
    rst = make_response(flask.jsonify(response))
    rst.headers['Access-Control-Allow-Origin'] = '*'
    return rst


# if this is the main thread of execution first load the model 
# and then start the server
if __name__ == "__main__":
    app.run(host='0.0.0.0',port=80)