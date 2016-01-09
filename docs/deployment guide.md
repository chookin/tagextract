
# 编译

    cd ~/project/ub-lab/tagbase/src/utils && mvn install -DskipTests \
    && cd ~/project/ub-lab/tagbase/src/etl-commons && mvn install -DskipTests \
    && cd ~/project/ub-lab/tagbase/src/tagextract && mvn clean package -DskipTests
    
    cd ~/project/ub-lab/tagbase/src/tagextract && mvn package -DskipTests

# 测试
`java -cp tagextract-1.0-SNAPSHOT.jar:tagextract-1.0-SNAPSHOT-tests.jar:lib/junit-4.11.jar org.junit.runner.JUnitCore cmri.tagbase.extractor.web.youku.video.VideoDetailPageProcessorTest`

# 部署
## 环境变量
修改配置文件 ~/.bash_profile

    # Get the aliases and functions
    if [ -f ~/.bashrc ]; then
          . ~/.bashrc
    fi
    
    # User specific environment and startup programs
    
    PATH=$PATH:$HOME/bin
    
    export PATH
    
    export PYTHON_HOME=$HOME/local/python
    export PATH=${PYTHON_HOME}/bin:$PATH
    
    export JAVA_HOME=$HOME/local/jdk
    export CLASSPATH="$JAVA_HOME/lib:$JAVA_HOME/jre/lib:$CLASSPATH"
    export PATH=$JAVA_HOME/bin:$PATH
    
    export HADOOP_HOME=$HOME/hadoop-installed/hadoop
    export PATH=$HADOOP_HOME/bin:$PATH
    
    export MYSQL_HOME=$HOME/local/mysql
    export PATH=$MYSQL_HOME/bin:$PATH
    
    export MONGO_HOME=$HOME/local/mongodb
    export PATH=$MONGO_HOME/bin:$PATH
    
    export PHANTOM_HOME=$HOME/local/phantomjs
    export PATH=$PHANTOM_HOME/bin:$PATH
    
    export CASPER_JS_HOME=$HOME/local/casperjs
    export PATH=$CASPER_JS_HOME/bin:$PATH
    
    # set up a UTF-8 environment if the default encoding is not utf-8
    # ref: http://perlgeek.de/en/article/set-up-a-clean-utf8-environment
    export LC_ALL=en_US.UTF-8
    export LANG=en_US.UTF-8
    export LANGUAGE=en_US.UTF-8

## MooseFS
启动

    myname=`whoami`
    cd /home/$myname/local/mfs
    sbin/mfsmetalogger start
    sbin/mfsmaster start -a
    sbin/mfschunkserver start
    python sbin/mfscgiserv
挂载（以root帐户执行）
    
    /home/mtag/local/mfs/bin/mfsmount /home/mtag/share 
如果报错：

    fuse: bad mount point `/home/mtag/share': Transport endpoint is not connected
    see: /home/mtag/local/mfs/bin/mfsmount -h for help
则先取消挂载，再执行挂载命令

    umount -l /home/mtag/share
    /home/mtag/local/mfs/bin/mfsmount /home/mtag/share 
如果moosefs的版本较高，系统自带的mount会不支持moosefs的一些选项，并在执行挂载命令时报错：

    /bin/mount: unrecognized option `--no-canonicalize'
解决办法，编译得到一个较高版本的mount，并临时替换系统自带的mount文件。

    cp -f /bin/mount.mfs /bin/mount
    cp -f /bin/mount.fs /bin/mount

## Mongo配置
准备数据路径

    myname=`whoami`
    mkdir -p /data/${myname}/mongodb/data
    mkdir -p /data/${myname}/mongodb/logs
启动

    myname=`whoami`
    # mongodb default port is 27017
    mongod --dbpath=/data/${myname}/mongodb/data --logpath=/data/${myname}/mongodb/logs/mongod.log --port=27017 --logappend --fork
    netstat -lanp | grep mongo
访问mongo

    # 连接mongo时，指定启动时所设定的端口号
    mongo --host=mongomaster--port=27017
## Mysql

    bin/mysqladmin -u root password "biT1G7KknSIMu3lw"
   
    use mysql;
    grant all on *.* to 'mtag'@'%' identified by '0aRNOaK58wgchnm6';
    grant all on *.* to 'mtag'@'localhost' identified by '0aRNOaK58wgchnm6';
    grant all on *.* to 'mtag'@'lab07' identified by '0aRNOaK58wgchnm6';
    flush privileges;
   
    #查看当前用户
    select c.user,c.host,c.password from mysql.user c;
   
## Redis
启动

    cd ~/local/redis/bin && ./redis-server  ~/local/redis/redis.conf
查看所有keys

    # 如果不加--raw，汉字会以16进制串显示，例如:pd-c-music.migu/\xe6\xb3\xb0\xe8\xaf\xad
    redis-cli --raw KEYS "*"
    # 查看名字以”pw”开头的keys
    redis-cli --raw KEYS "pw*

删除keys

    # 删除所有以“pd-c-music.migu”开头的keys，需要-d选项指定分隔符为换行符（xargs默认是以空格为分隔符）
    redis-cli KEYS "pd-c-music.migu*" | xargs -d \\n redis-cli DEL
    redis-cli KEYS "pd-c-youku*" | xargs -d \\n redis-cli DEL
    redis-cli KEYS "youku*" | xargs -d \\n redis-cli DEL
       
## PhantomJs
安装
    yum -y install gcc gcc-c++ make flex bison gperf ruby openssl-devel freetype-devel fontconfig-devel libicu-devel sqlite-devel libpng-devel libjpeg-devel
    wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.0.0-source.zip
   
测试安装

    phantomjs --version
   
## CasperJs
[http://casperjs.org/](http://casperjs.org/)

CasperJS is an open source navigation scripting & testing utility written in Javascript for the PhantomJS WebKit headless browser and SlimerJS (Gecko). It eases the process of defining a full navigation scenario and provides useful high-level functions, methods & syntactic sugar for doing common tasks such as:

- defining & ordering browsing navigation steps
- filling & submitting forms
- clicking & following links
- capturing screenshots of a page (or part of it)
- testing remote DOM
- logging events
- downloading resources, including binary ones
- writing functional test suites, saving results as JUnit XML
- scraping Web contents

Prerequisites

1.	PhantomJS 1.8.2 or greater. 
2.	Python 2.6 or greater

测试安装

   phantomjs --version
   casperjs
   
如果casperjs出现下面这个错误：

    File "/usr/local/bin/casperjs", line 138  
    except OSError as err:
估计是python版本的问题，需要在2.6以上.
   
## 防火墙

    # ssh
    /sbin/iptables -I INPUT -p tcp --dport 21022 -j ACCEPT
    
    # http, svn
    /sbin/iptables -I INPUT -p tcp --dport 80 -j ACCEPT
    # /sbin/iptables -I INPUT -p tcp --dport 3690 -j ACCEPT
    
    # mysql
    /sbin/iptables -I INPUT -p tcp --dport 3306 -j ACCEPT
    /sbin/iptables -I INPUT -p tcp --dport 23306 -j ACCEPT
    
    
    # mongo
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 27017 -j ACCEPT
    
    # moosefs
    # port to listen on for metalogger, masters and supervisors connections (default is 9419)
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 9419 -j ACCEPT
    
    
    # MooseFS master command port (default is 9420)
    # port to listen on for chunkserver connections (default is 9420)
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 9420 -j ACCEPT
    
    
    # port to listen on for client (mount) connections (default is 9421)
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 9421 -j ACCEPT
    
    # port to listen for client (mount) connections (default is 9422)
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 9422 -j ACCEPT
    
    # mfscgiserv是python写的简易的webserver，mfs的web端监控系统的应用程序
    /sbin/iptables -I INPUT -p tcp --dport 9425 -j ACCEPT
    
    # redis
    # Accept connections on the specified port, default is 6379.
    /sbin/iptables -I INPUT -s 192.168.110.0/24 -p tcp --dport 6379 -j ACCEPT
    
    # svn
    /sbin/iptables -I INPUT -p tcp --dport 3960 -j ACCEPT
    
    # vnc
    iptables -I INPUT -p tcp --dport 5901 -j ACCEPT
    
    service iptables save

