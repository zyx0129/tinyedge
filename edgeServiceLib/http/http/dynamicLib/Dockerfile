FROM python:3.6
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install kafka-python
RUN pip install flask
RUN pip install requests
ENV PYTHONUNBUFFERED=0
COPY *.py /app/
CMD ["python","/app/app.py"]~
