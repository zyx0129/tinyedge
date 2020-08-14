
from bluetooth import *


hostMACAddress = '' 
#port = 2
backlog = 1
size = 1024

#server_sock = BluetoothSocket()

def bleListen(server_sock):
	# server_sock = BluetoothSocket()
	# server_sock.bind(("",PORT_ANY))
	# server_sock.listen(backlog)
	# port = server_sock.getsockname()[1]
	#uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
	#advertise_service( server_sock, "SampleServer",uuid)
#         advertise_service( server_sock, "SampleServer",
#                    service_id = uuid,
#                    service_classes = [ uuid, SERIAL_PORT_CLASS ],
#                    profiles = [ SERIAL_PORT_PROFILE ], 
# #                   protocols = [ OBEX_UUID ] 
#                     )
	#print("Waiting for connection on RFCOMM channel %d" % port)
	client, clientInfo = server_sock.accept()
	print ("connect success")
	server_sock.close() 
	try:    
	    while 1:
	        data = client.recv(size)
	        if data:
	            print(data)
	            client.send(data) # Echo back to client
	except: 
	    print("Closing socket")
	    client.close()
	    #server_sock.close() 
	    #bleListen(server_sock)

#server_sock = BluetoothSocket()
#server_sock.bind((hostMACAddress, port))
#server_sock.bind(("",PORT_ANY))
#server_sock.listen(backlog)

#port = server_sock.getsockname()[1]
#bleListen()
# server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
 
# port = 2
# server_sock.bind(("",port))
# server_sock.listen(1)
# print "waitting"
# client_sock,address = server_sock.accept()
# print "Accepted connection from ",address
 
# data = client_sock.recv(1024)
# print "received [%s]" % data
 
# client_sock.close()
# server_sock.close()
