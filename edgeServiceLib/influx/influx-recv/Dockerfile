FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install kafka-python
RUN pip install requests
RUN pip install redis
RUN pip install flask
RUN pip install influxdb -i https://pypi.doubanio.com/simple/ --trusted-host pypi.douban.com
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~