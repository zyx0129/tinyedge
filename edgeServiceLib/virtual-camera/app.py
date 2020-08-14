import requests
import time

image = open("pic/dog.jpg", "rb")
files = {"image":image}
time1 = time.time()
#r = requests.post("http://localhost:8231/predict",files=files)
#r = requests.post("http://47.96.155.111:12351/predict",files=files)
r = requests.post("http://localhost:32361/predict",files=files)
time2 = time.time()
print (r.text)
print (time2-time1)