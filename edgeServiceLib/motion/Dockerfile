FROM gcc:latest
RUN mkdir /usr/src/myapp
COPY SenSys_linux/ /usr/src/myapp/
WORKDIR /usr/src/myapp
RUN g++ -std=c++11 OCTree.cpp Limber.cpp SenSys_linux.cpp -o SenSys_linux
CMD ["./SenSys_linux"]