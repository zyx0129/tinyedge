FROM python:3.6-alpine
RUN pip install kafka-python -i http://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
RUN pip install requests -i http://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
RUN pip install redis -i http://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
RUN pip install flask -i http://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
RUN pip install paho-mqtt -i http://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~
