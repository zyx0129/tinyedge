FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install kafka-python
RUN pip install requests
RUN pip install redis
RUN pip install paho-mqtt
RUN pip install modbus_tk
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
COPY pic ./pic
ENTRYPOINT ["tail","-f","/dev/null"]
#CMD ["python","app.py"]~
