import requests
import time
import sys
import getopt
from multiprocessing import Process

def objectRecognition(target):
    image = open("pic/dog.jpg", "rb")
    files = {"image":image}
    time1 = time.time()
    if target=="edge":
        r = requests.post("http://edge-object-recognition/predict",files=files)
        print (r.text)
    elif target=="cloud":
        r = requests.post("http://47.96.155.111:12351/predict",files=files)
        print (r.text)
    time2 = time.time()
    print ("Delay: "+ str(time2-time1))
    #time.sleep(2)

def main(argv):
    target = "edge"
    service = "object-recognition"
    try:
        opts, args = getopt.getopt(argv, "ht:s:", ["target=", "service="])
    except getopt.GetoptError:
        print('virtual_camera.py -t <target> -s <service>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('virtual_camera.py -t <target> -s <service>')
            sys.exit()
        elif opt in ("-t", "--target"):
            target = arg
        elif opt in ("-s", "--service"):
            service = arg

    for pid in range(10):
        p = Process(target = objectRecognition,args=(target,))
        p.start()

if __name__ == "__main__":
    main(sys.argv[1:])
