
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
	_id 请看“_id”设计部分
	name 分类名称
	site 网站名
	code 分类id
	level 分类所处层级，一级分类为1
	url 该分类的网页url
	leaf 是否叶子分类
	p_name 父级分类的名字
	p_code 父级分类的id
	crawTime 该分类下面的具体物品信息爬取完毕的时间
	time 该分类信息的爬取时间。

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

