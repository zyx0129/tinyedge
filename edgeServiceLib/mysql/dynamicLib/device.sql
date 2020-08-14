DROP database IF EXISTS `edge_device`;

create database `edge_device` default character set utf8 collate utf8_general_ci;

use edge_device;

%$@device-management%{
DROP TABLE IF EXISTS `device`;


CREATE TABLE `device` ( 
   `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
   `name` varchar(100) DEFAULT NULL, 
   `created` datetime DEFAULT NULL, 
   `changed` datetime DEFAULT NULL,
   `status` varchar(20) DEFAULT "unactivated",
   `connector` varchar(20) DEFAULT "http",        #可以根据connector数量做判断
   `address` varchar(40) DEFAULT NULL, 
   %$*deviceAuth%{`password` varchar(100) DEFAULT NULL,%}
   UNIQUE(`name`),
   PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


%$#device-management.ObjectModel.property%{
DROP TABLE IF EXISTS `property`;

CREATE TABLE `property` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `device_id` int(11) unsigned NOT NULL COMMENT 'Device id in device table',
    `identifier` varchar(100) DEFAULT NULL,
    `datatype` varchar(20) DEFAULT 'string',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
%}

%$#device-management.virtualSensor%{
CREATE TABLE `virtual_sensor`(
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(100) DEFAULT NULL COMMENT 'virtual sensor name',
    `input` varchar(200) DEFAULT NULL COMMENT 'data input source, e.g. device1:acc,gyro;device2:mac',
    `labels` varchar(200) DEFAULT NULL COMMENT 'e.g. open,close',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `sample`(
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `vs_id` int(11) unsigned NOT NULL COMMENT 'Sensor id in virtual sensor table',
    `label` varchar(200) DEFAULT NULL COMMENT 'e.g. open',
    `start` int DEFAULT NULL,
    `end` int DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`vs_id`) REFERENCES `virtual_sensor` (`id`) ON DELETE CASCADE,
    INDEX (`vs_id`,`label`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
%}

%}