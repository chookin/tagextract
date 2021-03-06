# 启动任务方式
## 命令行方式执行，指定命令参数
以下两种方式均可以：

1. 通过 -D<参数名=参数值>的方式修改配置参数
1. 通过 --<参数名=参数值>的方式修改配置参数

有两种执行方式：

1. executing by calling class cmri.tagbase.TagCli
<pre>
java -cp conf/:lib/:tagextract.jar cmri.tagbase.TagCli --class=cmri.tagbase.read.BaiduYueduCollection --task=collect-categories --proxy.enable=false --download.concurrent.num=1 --download.sleepMilliseconds=1000 -Daction=read.baidu-categories
</pre>
1. executing by directly calling job class
<pre>
java -cp conf/:lib/:tagextract.jar cmri.tagbase.read.BaiduYueduCollection --task=collect-categories -Dproxy.enable=false --download.concurrent.num=1 --download.sleepMilliseconds=1000 --action=read.baidu-categories
</pre>

## 本进程内启动
在已有进程内执行，不创建新的进程。通过json字符串指定任务参数。
  
    #采集百度阅读的分类信息
    {class:cmri.tagbase.read.BaiduYueduCollection,task:collect-categories,proxy.enable:false,download.concurrent.num:1,download.sleepMilliseconds:1000}
    #采集百度阅读的图书信息
    {class:cmri.tagbase.read.BaiduYueduCollection,task:collect-kinds,scheduler:cmri.etl.scheduler.RedisPriorityScheduler,proxy.enable:false,download.concurrent.num:1,download.sleepMilliseconds:1000,all:true,since:2970-01-02|000000}
# 参数
## 通用

- task 任务类型，具体取值有：
    - collect-categories，采集分类信息
    - collect-kinds，采集具体物品
- action 设定操作名称，主要用于调整日志文件名称，仅命令行方式执行时有效
- proxy.enable
    
## 采集分类信息

没有自己的参数
## 采集具体物品（商品、图书、音乐、动漫等等）
- since 若某分类下的商品自该时刻起还没有被采集过，则采集该分类下的商品，例如，“--since=2970-01-02/00:00:00”意味着所有的都要重新采集
- all, if false, only collect categories with tag, default false.
- category 限定采集指定分类下的物品，例如：--category={\"site\":\"jd\",\"code\":\"1320-1585-9434\"}

# 具体操作
## 采集分类信息

    --class=cmri.tagbase.read.BaiduYueduCollection --task=collect-categories -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000
    --class=cmri.tagbase.read.DoubanCollection --task=collect-categories -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
## 采集具体物品

    --class=cmri.tagbase.read.BaiduYueduCollection --task=collect-kinds --all=true --since=2970-01-02|000000 -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
    --class=cmri.tagbase.read.DoubanCollection --task=collect-kinds --all=true --since=2970-01-02|000000 -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
