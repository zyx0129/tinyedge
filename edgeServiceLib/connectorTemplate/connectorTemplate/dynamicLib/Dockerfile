FROM python:3.6
#FROM python:3.7-slim-buster
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install kafka-python
RUN pip install requests
RUN pip install redis
RUN pip install paho-mqtt
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~