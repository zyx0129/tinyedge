FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install kafka-python
RUN pip install requests
RUN pip install redis
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~
