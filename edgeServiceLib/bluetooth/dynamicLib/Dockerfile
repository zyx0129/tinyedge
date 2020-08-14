FROM python:2.7
RUN mkdir ~/.pip
COPY pip.conf ~/.pip/ 
RUN apt-get update
RUN apt-get -yq install python-dev ipython 
RUN apt-get -yq install bluetooth libbluetooth-dev
COPY requirements.txt /
RUN pip install -r ./requirements.txt
COPY *.py /app/
CMD ["python","/app/app.py"]~