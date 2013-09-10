import socket,sys
import os 
import select 
import json , time
from multiprocessing import Process


"""
" author : vietanha34
" chuyen danh sach file len client voi dinh dang json
" dung co che select de xu ly non-blocking
" tao luong de gui file song song 
"""

# sua thu muc goc tai day 
PATH = "/Users/vietanha34/Downloads/"



class ListFile:
	"""liet ke danh sach file va folder 
	"""
	def __init__(self, path = PATH):
		self.path = path
		self.data = []
		os.chdir(path)

	def  getInfo(self):
		dirs = self.getDir()
		files = self.getFile()
		self.data.append(self.dirdata)
		self.data.append(self.filedata)
		return json.dumps(self.data)

	def getDir(self):
		self.dirdata = []
		for files in os.listdir("."):
			if os.path.isdir(files):
				filesdata = {}
				filesdata["dirname"] = files
				if os.path.islink(files):
					filesdata["link"] = os.path.realpath(files)
				else:
					filesdata["link"] = ""
				self.dirdata.append(filesdata)


	def getFile(self):
		self.filedata = []
		for files  in os.listdir("."):
			if os.path.isfile(files):
				filesdata = {}
				filesdata["filename"] = files
				filesdata["size"] = str(os.path.getsize(files))
				self.filedata.append(filesdata)
			


class MyServerSocket:
	"""docstring for MyServerSocket
	"	+ lay thong tin danh sach file va thu muc , tao duoi dinh dang json va gui qua client
	"	+ gui file qua client , moi yeu cau download file tao mot luong moi de co the gui nhieu file 
	"""

	def __init__(self, sock = None):
		self.HOST = ""
		self.PORT = 1234
		if sock is None : 
			try:
				self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
				self.sock.bind((self.HOST , self.PORT))
				self.sock.listen(5)

			except socket.error,e:
				print e 
				raise 
			
		else:
			self.sock = sock

		self.sock.setblocking(0)
		self.inputs = [self.sock, sys.stdin]

	
		
		

	def accept_sock(self):
		conn , addr = self.sock.accept()
		conn.setblocking(0)
		self.inputs.append(conn)
		print "co ket noi tu " + str(addr[0])
		

	def send_msg(self , msg):
		totalsent = 0
		MSGLEN = len(msg)
		while totalsent < MSGLEN:
			sent = self.connection.send(msg[totalsent:])
			if sent == 0:
				raise RuntimeError("socket connection broken")
			totalsent = totalsent + sent

	def recv_msg(self , msg):
		msg = ""

	def serve(self):
		running  = 1
		while running:
	 		inputready , outputready , exeptready = select.select(self.inputs, [] , [])
	 		for s in inputready:
	 			
				if s == self.sock: 
	 				self.accept_sock()

	 			elif s == sys.stdin :
	 				junk =  sys.stdin.readline()
	 				running = 0
	 				self.close_socket()
	 				print junk


	 			else :
	 				data = s.recv(1024)
	 				print data
	 				if data:
	 					self.handle_recv_data(data ,s)
	 				else :
	 					s.close()
	 					print "dong ket noi"
						self.inputs.remove(s)
		return running
					

	def close_socket(self):
		self.sock.close()

	def send_data(self , path , s):
		with open(path) as f:
			datafile = f.read(4096)
			while datafile:
				try:
					s.send(datafile)
					datafile = f.read(4096)			
				except socket.error as e:
					if str(e) == "[Errno 35] Resource temporarily unavailable":
						time.sleep(0)
                		continue
                	

        	s.close()
        	print "gui file thanh cong"
        	print "dong ket noi"
            
        	
            

	def handle_recv_data(self,data ,s):
		data =  str(data)
		data = data.split("|")
		"""			
		if data[0] == "read":
			path = os.path.join(PATH , data[1].strip())
			listfile = ListFile(path)
			files = listfile.getInfo()
			s.send(files)
		"""

		if data[0]  == "get" :
			path = os.path.join(PATH , data[1].strip())
			print "gui danh dach file nguon"
			data = ListFile(path)
			datasent = data.getInfo()
			datasent = datasent + '\n'
			s.send(datasent)
		elif data[0] == "download":
			path = os.path.join(PATH , data[1].strip())
			print path
			p = Process(target = self.send_data , args = (path , s ))
			p.start()
			#p.join()

						

def main():
	
	sock = MyServerSocket().serve()	 	
	sock.close_socket()
	
	

if __name__ == '__main__':
	main()


