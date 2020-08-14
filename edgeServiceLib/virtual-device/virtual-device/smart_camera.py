import requests
import time
import sys
import getopt


def main(argv):
    target = "edge"
    port=5000
    service = "object-recognition"
    try:
        opts, args = getopt.getopt(argv, "ht:p:s:", ["target=", "port=","service="])
    except getopt.GetoptError:
        print('virtual_camera.py -t <target> -p <port> -s <service> ')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('virtual_camera.py -t <target> -s <service>')
            sys.exit()
        elif opt in ("-t", "--target"):
            target = arg
        elif opt in ("-s", "--service"):
            service = arg
        elif opt in ("-p", "--port"):
            port = arg

    image = open("pic/people.jpg", "rb")
    files = {"image":image}
    time1 = time.time()
    if target=="edge":
        r = requests.post("http://edge-object-recognition:"+str(port)+"/model/predict",files=files)
        print (r.text)
    elif target=="cloud":
        r = requests.post("http://47.96.155.111:12351/model/predict",files=files)
        print (r.text)
    elif target=="local":
        r = requests.post("http://localhost:"+str(port)+"/model/predict",files=files)
        print (r.text)
    time2 = time.time()
    print ("Delay: "+ str(time2-time1))
   

if __name__ == "__main__":
    main(sys.argv[1:])
