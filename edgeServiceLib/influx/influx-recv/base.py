from influxdb import InfluxDBClient
from client import Client
import time

class Database:
    def __init__(self):
        self.__client = None
        configClient = Client()
        config = configClient.getConfig()
        self.__user = config["user"]
        self.__password = config["password"]
        self.__database = config["database"]


    def connect(self):
        if self.__client:
            return True
        try:
            self.__client = InfluxDBClient("edge-influx", 8086, self.__user, self.__password, self.__database)
            return True
        except:
            print("connect influx failed")
            time.sleep(1)
            self.connect()            

    def execute_influx(self, query):
        return self.__client.query(query)

    def write(self, deviceName, content_json, time):
        points = [{
            "measurement": "data",
            "tags": {
                "deviceName": deviceName
            },
            "time":int(time),
            "fields": content_json
        }]
        try:
            self.__client.write_points(points)
        except Exception as e:
            self.__client = InfluxDBClient("edge-influx", 8086, self.__user, self.__password, self.__database)
            self.write(deviceName,content_json,time)

    def readlast(self, deviceName, identifier):
        sql = "select {} from data where deviceName = '{}' order by time desc limit 1".format(identifier, deviceName)
        result = self.execute_influx(sql) 
        points = result.get_points()
        json={}
        for point in points:
            json={"time": point["time"], "funcName": identifier, "value": point[identifier]}
        return json

    def readDataByTime(self, deviceName, identifier, start, end):
        sql = "select {} from data and time >= {} and time <= {}".format(identifier,start, end)
        sql = "select {} from data where deviceName = '{}' and time >= {} and time <= {}".format(identifier, deviceName,start, end)
        result = self.execute_influx(sql)
        result = result.get_points()
        results = []
        for it in result:
            results.append(it[identifier])
        return results
