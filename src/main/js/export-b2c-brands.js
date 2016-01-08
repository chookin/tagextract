// 导出品牌集合（每个品牌出现一次，不区分电商）
// 参考命令：
//      mongo 127.0.0.1:27017/ecomm export-b2c-brands.js > b2c-brands.json

var result = db.goods.group({
    key: {
        "properties.品牌": true
    },
    cond: {
        "properties.品牌": {
            "$ne": null
        }
    },
    reduce: function (obj, prev) {
        var tag = obj.tag;
        if (prev.tags.indexOf(tag) == -1) {
            prev.tags.push(tag);
        }
    },
    initial: {
        tags: []
    }
});
printjson(result);
