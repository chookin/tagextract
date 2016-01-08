/**
 * Created by zhuyin on 4/24/15.
 */
// update domain field.
// 参考命令：
//      mongo 127.0.0.1:27017/ecomm --eval "var collection=db.book" update-domain.js

var table = collection;
printjson(table);
var c = table.find({tag:{$ne: null}, url:{$ne: null}});
while (c.hasNext()) {
    var obj = c.next();
    var url = obj.url;
    if(url == "") continue;
    // \w匹配字母或数字或下划线或汉字等
    var reg=new RegExp("(^[a-zA-Z]+://)?(www.)?([\\w\\.]+)/*");
    var matches = reg.exec(url);
    var domain = matches[3];
    table.update({_id: obj._id}, {$set: {domain:domain}});
}