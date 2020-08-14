FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN apk add --update gcc libc-dev fortify-headers linux-headers && rm -rf /var/cache/apk/*
RUN pip install kafka-python
RUN pip install requests
RUN pip install redis
RUN pip install psutil
ENV PYTHONUNBUFFERED=0
COPY *.py /app/
CMD ["python","/app/app.py"]~