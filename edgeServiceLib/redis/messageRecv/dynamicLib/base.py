import redis

class Redis:
    def __init__(self):
        self.__client = None

    def connect(self):
        try:
            pool = redis.ConnectionPool(host='localhost', port=6379, decode_responses=True)   # host是redis主机，需要redis服务端和客户端都起着 redis默认端口是6379
            self.__client = redis.Redis(connection_pool=pool)
            return True
        except:
            return False

    def updateData(self, deviceName, property, value):
        self.__client.set(deviceName+":"+property, value) 