/**
 * Created by work on 4/29/15.
 */

var inputCollection = collection; // pass it from --eval
printjson(inputCollection);
var outCollection = "tag_series_ids";

m = function() {
    for (var i = 0; i < this.tag.length; ++i) {
        var item = this.tag[i];
        for(var j = 0; j < this.properties.series.length; ++j){
            var series = this.properties.series[j];
            var idArr = [series.id];
            emit(this.domain + "||" + item,
                {
                    ids: idArr
                });
        }
    }
};

r = function(key, values){
    var idArr = [];
    values.forEach(function(e) {
        idArr = idArr.concat(e.ids);
    });
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

db.tag_series_ids.drop()

result = db.runCommand({
    mapreduce: this.inputCollection,
    map: m,
    reduce: r,
    query: {
        "properties.series": {
            "$ne": null
        },
        "tag": {
            "$ne": null
        }
    },
    out: this.outCollection,
    finalize:f,
    scope:{type: this.inputCollection},
    verbose: true
});
printjson(result);