import subprocess
import time
import re

status,result = subprocess.getstatusoutput('ls')
print(result)
minInterval = 99
maxInterval = 0
count = 0
sumInterval = 0
lastTime = time.time()
quit = 0
while 1:
    service = "edge-mqtt"
    service2 = "edge-cloud-connector"
    command = "docker logs " + service +" --tail 100 --since "+str(lastTime)
    status,result = subprocess.getstatusoutput(command)
    matchObj = re.findall(r'Publish new message(.+?) timestamp:(.+?) , traceId:(.*)', result)
    time.sleep(0.5)
    pubTimeStamp = 0
    print("match:"+str(len(matchObj)))
    command2 = "docker logs "+ service2+ " --tail 10000 --since "+str(lastTime)
    status2,result2 = subprocess.getstatusoutput(command2)
  
    for groups in matchObj:
        pubTimeStamp = groups[1]
        #command2 = "docker logs "+ service2+ " --since "+str(lastTime)
        #status2,result2 = subprocess.getstatusoutput(command2)
        matchObj2 = re.findall(r'New message come:(.+) timestamp:(.+?) , traceId:'+groups[2]+'\n', result2)
        if len(matchObj2)!=1:
            print("error,lenth:"+str(len(matchObj2)))
            break
        for groups2 in matchObj2:
            subTimeStamp = groups2[1]
            interval = float(subTimeStamp)-float(pubTimeStamp)
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
    if matchObj:
        lastTime = float(pubTimeStamp)
    if quit ==1:
        break
    #time.sleep(1)
avgInterval = sumInterval/(count)
print("avg:"+str(avgInterval))
print("max:"+str(maxInterval))
print("min:"+str(minInterval))
