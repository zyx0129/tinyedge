use mysql;

create user edge identified by 'emnetsEdge301';

grant all privileges on *.* to edge@'%' identified by 'emnetsEdge301' with grant option;


SET PASSWORD FOR 'root'@'localhost' = PASSWORD('root');

select host,user from user;
flush privileges;