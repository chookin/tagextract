# 操作说明
## 采集分类信息

参数：
* collect-categories
* --action

## 采集具体商品（或图书、音乐、动漫）
* collect-kinds
* --since 若某分类下的商品自该时刻起还没有被采集过，则采集该分类下的商品，例如，“--since=2970-01-02/00:00:00”意味着所有的都要重新采集
* --all, if false, only collect categories with tag, default false.
* --category --category={\"site\":\"jd\",\"code\":\"1320-1585-9434\"}

# 具体操作
## 采集分类信息
<pre>
# 命令行方式执行，指定命令参数
# 通过 -D<参数名=参数值>的方式修改配置参数。
--task=collect-categories --class=cmri.tagbase.read.BaiduYueduCollection -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
# 任务方式启动（在已有进程内执行，不创建新的进程），指定任务的参数
{task:collect-categories,class:cmri.tagbase.read.BaiduYueduCollection,proxy.enable:false,download.concurrent.num:1,download.sleepMilliseconds:1000}

--task=collect-categories --class=cmri.tagbase.read.DoubanCollection -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
</pre>
## 采集具体商品（或图书、音乐、动漫）
<pre>
--task=collect-kinds --class=cmri.tagbase.read.BaiduYueduCollection --all=true --since=2970-01-02|000000 -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
{task:collect-kinds,class:cmri.tagbase.read.BaiduYueduCollection,scheduler:cmri.etl.scheduler.RedisPriorityScheduler,proxy.enable:false,download.concurrent.num:1,download.sleepMilliseconds:1000,all:true,since:2970-01-02|000000}

--task=collect-kinds --class=cmri.tagbase.read.DoubanCollection --all=true --since=2970-01-02|000000 -Dproxy.enable=false -Ddownload.concurrent.num=1 -Ddownload.sleepMilliseconds=1000 
</pre>