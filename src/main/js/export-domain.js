// 导出domain集合
// 参考命令：
//      mongo 127.0.0.1:27017/ecomm --eval "var collection=db.music" export-domain.js > music.domains.json

// result format:
//[
//    {
//        "site" : "music.baidu",
//        "domains" : [
//            "music.baidu.com",
//            "y.baidu.com"
//        ]
//    },
//    {
//        "site" : "music.migu",
//        "domains" : [
//            "music.migu.cn"
//        ]
//    },
//    {
//        "site" : "music.qq",
//        "domains" : [
//            "s.plcloud.music.qq.com",
//            "y.qq.com"
//        ]
//    }
//]

var table = collection;
printjson(table);
var result = table.group({
    key: {
        "site": true
    },
    cond: {
        "url": {
            "$ne": null
        }
    },
    reduce: function (obj, prev) {
        var url = obj.url;
        if(url == "") return;
        var reg=new RegExp("(^[a-zA-Z]+://)?(www.)?([\\w\\.]+)/*");
        var matches = reg.exec(url);
        var domain = matches[3];

        if (prev.domains.indexOf(domain) == -1) {
            prev.domains.push(domain);
        }
    },
    initial: {
        domains: []
    }
});
printjson(result);
