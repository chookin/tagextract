/**
 * Created by zhuyin on 4/24/15.
 *
 * First do map reduce:
 * mongo 127.0.0.1:27017/ecomm --eval "var collection='book'" export-tag-ids.js
 *
 * Then do mongoexport:
 * mongoexport --host localhost --db ecomm --collection tag_ids --csv --out book-tag-ids.csv --fields value.tag,value.domain,value.ids -q '{"value.ids":{"$ne":null},type:'book'}'
 */

var inputCollection = collection; // pass it from --eval
printjson(inputCollection);
var outCollection = "tag_ids";

m = function() {
    for (var i = 0; i < this.tag.length; ++i) {
        var item = this.tag[i];
        var idArr = [];
        idArr = [this.code];
        emit(this.domain + "||" + item,
            {
                ids: idArr
            });
    }
};

r = function(key, values){
    var idArr = [];
    for(var i = 0; i< values.length; ++i){
        var value = values[i];
        for(var j= 0; j < value.ids.length; ++j){
            var id = value.ids[j];
            idArr.push(id);
        }
    }
    return {
        ids: idArr
    };
};

f = function(key, value){
    var ids = "";

    for(var i = 0; i < value.ids.length; ++i){
        var id = value.ids[i];
        ids = ids + "|" + id;
    }
    if(ids.indexOf("|") == 0){
        ids = ids.substring(1);
    }

    var keyArr = key.split("||");
    return {
        domain: keyArr[0],
        tag: keyArr[1],
        ids: ids,
        type: this.type
    };
};

db.tag_ids.remove({type: inputCollection});

result = db.runCommand({
    mapreduce: this.inputCollection,
    map: m,
    reduce: r,
    query: {
        "tag": {
            "$ne": null
        },
        "code": {
            "$ne": null
        }
    },
    out: this.outCollection,
    finalize:f,
    scope:{type: this.inputCollection},
    verbose: true
});
printjson(result);