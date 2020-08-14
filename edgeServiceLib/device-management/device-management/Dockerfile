FROM python:3.6-alpine
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN pip install flask
WORKDIR /app
COPY *.py ./
CMD ["python","app.py"]~