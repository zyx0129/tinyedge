import subprocess
import time
import re

print("start")
minInterval = 99
maxInterval = 0
count = 0
sumInterval = 0
#lastTime = time.time()
quit = 0

service="edge-influx-client"
command = "docker logs " + service +" --tail 4000"
status,result = subprocess.getstatusoutput(command)
matchObj = re.findall(r'handleInterval:([0-9]+\.[0-9]+)', result)

for groups in matchObj:
    print(groups)
    interval = float(groups)
    if interval<minInterval:
        minInterval = interval
    if interval>maxInterval:
        maxInterval = interval
    sumInterval+=interval
    print(interval)
    count+=1
    if count>=5000:
        quit = 1
        break
    #time.sleep(1)
avgInterval = sumInterval/(count)
print("avg:"+str(avgInterval))
print("max:"+str(maxInterval))
print("min:"+str(minInterval))
print(count)
