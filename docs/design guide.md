
# 数据存储
- 标签映射存储于mysql中。
- 采集的分类信息、物品信息等存储于mongo中。

## 标签映射存储
标签映射信息存储于mysql数据库tagbase.tagmap中.  
考虑到标签映射中，一个分类可以有多个标签，一个标签可以关联多个分类，它们是多对多的关系，因此，不能选用“网站名+分类”即“site　+ category + categoryId”作为表的主键。
数据表的设计如下：

<pre>
create table if not exists tagmap(
  id INT NOT NULL AUTO_INCREMENT COMMENT 'record id',
  site varchar(32) NOT NULL COMMENT 'site name',
  category varchar(32) NOT NULL COMMENT 'category name',
  categoryId VARCHAR(32) COMMENT 'category id, could be null',
  tag varchar(64) NOT NULL COMMENT 'tag name',
  updateTime timestamp default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间，记录创建和更新时都会自动刷新', 
  PRIMARY KEY (id)
);
CREATE INDEX i_tagmap_site on tagmap (site);
</pre>

## 分类与物品的存储
数据存储主要由两类集合：

- category，不区分电商、阅读、音乐等类型，统一存储分类信息；
- 物品集合，存储电商、阅读、音乐等具体物品，不同类型的物品存储到不同的物品集合中；

目前，数据存储到mongo的ecomm数据库中。

### _id设计

为了提升mongo的检索效率，对“_id”进行重点设计。
“_id”由三部分组成：
<pre>site + code + name(,9]</pre>

- site，是网站名，自定义，如百度阅读的site为”read.baidu”；
- code，是分类、商品等的id，如果id不存在（例如某些分类就不没有id），则忽略；
- name，是分类、商品等的名称，最多取名称的前9个字符作为”_id”的一部分。

生成”_id”的代码：

<pre>
/// class TagEntity<T>
    public String getId() {
        StringBuilder strb = new StringBuilder(site);
        if (code != null) {
            strb.append("-").append(code);
        }
        strb.append("-").append(name.substring(0, Math.min(9, name.length())));
        return strb.toString();
    }
</pre>

## 分类集合
分类信息统一存储到集合ecomm.category中。
   * _id 请看“_id”设计部分
   * name 分类名称
   * site 网站名
   * code 分类id
   * level 分类所处层级，一级分类为1
   * url 该分类的网页url
   * leaf 是否叶子分类
   * p_name 父级分类的名字
   * p_code 父级分类的id
   * crawTime 该分类下面的具体物品信息爬取完毕的时间
   * time 该分类信息的爬取时间。

<pre>
> db.category.findOne()
{
        "_id" : "chinaz-357-招商加盟",
        "name" : "招商加盟",
        "site" : "chinaz",
        "code" : "357",
        "level" : 3,
        "url" : "http://top.chinaz.com/list.aspx?t=357",
        "leaf" : true,
        "crawlTime" : ISODate("2015-03-18T10:52:49.348Z")
}
</pre>

## 物品集合
不同类型的物品存储到不同的物品集合中。

### 电商
goods集合：

- _id 请看“_id”设计部分
- name
- site
- code
- url
- time 采集时间
- category
- c_code
- tag []
- properties
   * detailedTime 品牌等详情信息的获取时间
   * desc
   * scoreNum
   * scoreValue
   * price
   * 品牌
   * 店铺
   * 上架时间
   * 类别
   * 种类
   * 功效
   * 规格
   * 适用
   * 商品产地
   * 商品毛重
   * 适用人群

<pre>
> db.goods.findOne({site:'jd'})
{
        "_id" : "jd-1258337521-考研数学标准模拟试",
        "name" : "考研数学标准模拟试卷与精解(附光盘数学1)/2015考研专家指导丛书",
        "site" : "jd",
        "code" : "1258337521",
        "url" : "http://m.jd.com/product/1258337521.html",
        "time" : ISODate("2015-03-27T02:28:21.016Z"),
        "category" : "考研大纲",
        "c_code" : "1713-3290-6604",
        "properties" : {
                "detailedTime" : ISODate("2015-04-15T05:35:04.043Z"),
                "price" : 23.8,
                "scoreNum" : 0,
                "上架时间" : ISODate("2014-11-24T07:52:46Z"),
                "商品毛重" : "500.00g",
                "店铺" : "后浪出版公司图书专营店",
                "货号" : "9787511425676"
        }
}
<pre>

### 音乐
music集合：

- _id 请看“_id”设计部分
- name
- site
- code
- url
- time 采集时间
- category
- c_code
- tag []
- properties
   * album 专辑名称
   * albumId
   * albumUrl
   * desc
   * favorNum 收藏数
   * keyWords [**,**] 该网站所打的标签
   * 是否为歌单，true为是，不存在为否
   * playNum 播放次数
   * scoreNum 评论数
   * scoreValue 评分，满分100
   * singer [{name:**, id:**, url:**}] 歌手列表，包括歌手的名字，id，url
   * type 歌曲类型，对于百度音乐，如果为mv，则值为mv；对于qq音乐，如果为歌单，则为musicList

### 视频
video集合：

- _id 请看“_id”设计部分
- name 视频名称
- site 视频网站，网站名拼音，全为小写，例如优酷的为youku
- code 唯一id
- url 视频的url
- time 采集时间
- category 所属分类的名称
- c_code 所属分类的id
- tag [] 标签库的标签（采集完后手工标注）
- properties 属性集合
   * alias 别名
   * screenTime 上映时间
   * region 地区
   * desc 描述信息
   * keyWords [**,**] 该网站所打的标签（如，类型标签: 剧情 / 军事 / 悬疑 / 言情），另外，分类名也添加到keyWords集合中，在打标签时，根据keyWords中标签。
   * duration 时长，分钟
   * favorNum 收藏数
   * playNum 播放次数
   * downloadTimes 下载次数
   * scoreNum 评论数
   * scoreValue 评分，满分100
   * director  [{name:**, id:**, url:**}] 导演列表，包括名字，id，url
   * star [{name:**, id:**, url:**}] 主演列表，包括名字，id，url
   * presenter   [{name:**, id:**, url:**}] 主持人
   * series 电视剧的聚集 [{name:**, id:**,url:**, label:**,playNum:**, guests: [{name:**, id:**, url:**}]}] 剧集列表，包括剧集的名字，剧集的id，剧集的url，剧集的label(例如03-21期), 播放次数，嘉宾
### 动漫
catoon集合：

- _id 请看“_id”设计部分
- name 名称
- site 网站名，网站名拼音，全为小写，例如优酷的为youku
- code 唯一id
- url 地址
- time 采集时间
- category 所属分类的名称
- c_code 所属分类的id
- tag [] 标签库的标签（采集完后手工标注）
- properties 属性集合
   * desc 描述信息
   * keyWords [**,**] 该网站所打的标签（如，类型标签: 剧情 / 军事 / 悬疑 / 言情）
   * playNum 播放次数
   * flower 鲜花数，或顶的次数
   * egg 鸡蛋数，或踩的次数
   
presenter   [{name:**, id:**, url:**}] 主持人
### 阅读
book集合：

- _id 请看“_id”设计部分
- name
- site
- code
- url
- time 采集时间
- category
- c_code
- tag []
- properties
   * author作者
   * aboutAuthor作者简介
   * bookIntro内容简介
   * editorRecommend编辑推荐
   * keyWords该网站所打的标签
   * price定价,元
   * publicationTime出版时间
   * readCount在读人数
   * scoreNum 评论数
   * scoreValue 评分，满分100

url hash
存储url与本地缓存文件的映射。
本地文件名基于url的md5.

<pre>
> db.urlHash.find()
{ "_id" : "http://s.m.taobao.com/search.htm?q=洁面", "hash" : "s.m.taobao.com/640ef88ece7fbde8495911fe33869e21", "time" : ISODate("2015-04-28T07:18:16.525Z") }

public static String getHash(String url){
        String regex = "(?i)^[a-zA-Z]+://(www.)*([a-zA-Z\\d\\.]+/?[a-zA-Z\\d]+)(/[\\s\\S]*)";
        String domain = StringHelper.parseRegex(url, regex, 2);
        if(domain == null){// such as 'http://www.126.com'
            regex = "(?i)^[a-zA-Z]+://(www.)*([a-zA-Z\\d\\.]+/?[a-zA-Z\\d]+)([\\s\\S]*)*";
            domain = StringHelper.parseRegex(url, regex, 2);
        }
        return domain + "/" + DigestUtils.md5Hex(url);
}
</pre>

举例说明，例如
    http://a.m.taobao.com/items/i42471538944.htm?sid=60338c63d854b9ef&abtest=4&rn=2bdac199debd8eb86b7234fc1d3c712e
的本地文件名为：
    a.m.taobao.com/items/1dee39a9c6bb4e2e24684315f4d8bdf5

# 用户接口
## 标签映射导入
以咪咕音乐的标签映射为例进行说明。
在程序的classpath根路径下（对于开发环境，是resources文件夹；对于生产环境，默认是conf文件夹）创建文件夹tagmap，然后添加文件“music.migu.csv”。注意：文件名的格式为：网站名 + “.csv”。
编辑“music.migu.csv”文件，第一行为标题行。
