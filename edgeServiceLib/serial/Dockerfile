FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install pyserial
ENV PYTHONUNBUFFERED=0
WORKDIR /app
COPY *.py ./
ENTRYPOINT ["tail","-f","/dev/null"]
#CMD ["python","app.py"]~