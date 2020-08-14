FROM python:3.6
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install flask
RUN pip install requests
RUN pip install gevent==1.4.0
RUN pip install pillow==6.1.0
RUN pip install tensorflow==1.14.0
RUN pip install keras==2.2.4
RUN pip install uwsgi==2.0.18
COPY model /root/.keras/models/
#COPY tensorflow-1.14.0-cp35-none-linux_armv7l.whl /home
#RUN pip install tensorflow-1.14.0-cp35-none-linux_armv7l.whl

ENV PYTHONUNBUFFERED=0
COPY *.py /app/
CMD ["python","/app/app.py"]~
#CMD ["uwsgi", "--http", ":5211", "--wsgi-file", "app.py", "--callable", "app", "--master", "--processes", "1", "--threads", "1"]
