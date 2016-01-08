-- DROP DATABASE IF EXISTS `stock`;
-- DROP USER `chookin`;

-- 告诉mysql解释器，该段命令是否已经结束了，mysql是否可以执行了，默认情况下，delimiter是分号
-- mysql data base's location: /var/lib/mysql

delimiter ;

# mysql -u root -p

create user 'mtag' identified by '0aRNOaK58wgchnm6';
grant all on *.* to 'mtag'@'localhost' identified by '0aRNOaK58wgchnm6';
grant all on *.* to 'mtag'@'%' identified by '0aRNOaK58wgchnm6';
flush privileges;

# DROP DATABASE tagbase;
create database if not exists `tagbase` default character set utf8;

use tagbase;

# DROP TABLE tagmap;
create table if not exists tagmap(
  id INT NOT NULL AUTO_INCREMENT COMMENT 'record id',
  site varchar(32) NOT NULL COMMENT 'site name',
  category varchar(32) NOT NULL COMMENT 'category name',
  categoryId VARCHAR(32) COMMENT 'category id, could be null',
  tag varchar(64) NOT NULL COMMENT 'tag name',
  updateTime timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间', # 在创建新记录和修改现有记录的时候都对这个时间列刷新
  PRIMARY KEY (id)
);

CREATE INDEX i_tagmap_site on tagmap (site);