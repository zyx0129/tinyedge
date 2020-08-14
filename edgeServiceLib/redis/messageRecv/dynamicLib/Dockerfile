FROM python:3.6
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/
#COPY requirements.txt /
#RUN pip install -r ./requirements.txt
RUN pip install kafka-python
RUN pip install flask
RUN pip install influxdb -i https://pypi.doubanio.com/simple/ --trusted-host pypi.douban.com
#RUN pip install influxdb -i https://pypi.doubanio.com/simple/ --trusted-host pypi.douban.com
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~