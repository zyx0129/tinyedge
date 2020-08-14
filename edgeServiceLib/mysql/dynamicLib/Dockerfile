FROM mysql:5.7

ENV MYSQL_ALLOW_EMPTY_PASSWORD yes

COPY setup.sh /mysql/setup.sh
COPY device.sql /mysql/device.sql
COPY privileges.sql /mysql/privileges.sql

CMD ["sh", "/mysql/setup.sh"]