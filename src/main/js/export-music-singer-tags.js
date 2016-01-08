/**
 * Created by zhuyin on 4/27/15.
 */

m = function() {
    for (var i = 0; i < this.properties.singer.length; ++i) {
        var singer = this.properties.singer[i];
        emit(singer.name,
            {
                tags: this.tag,
                id: singer.id,
                mid: singer.mid,
                url: singer.url,
                domain: this.domain,
                site: this.site,
                count: 1
            });
    }
};

r = function(key, values){
    var tags = [];
    var count = 0;
    for(var i = 0; i< values.length; ++i){
        var value = values[i];
        for(var j= 0; j < value.tags.length; ++j){
            var tag = value.tags[j];
            if(tags.indexOf(tag) == -1){
                tags.push(tag);
            }
        }
        count = count + 1;
    }
    var first = values[0];
    return {
        tags: tags,
        id: first.id,
        mid: first.mid,
        url: first.url,
        domain: first.domain,
        site: first.site,
        count: count
    };
};

f = function(key, value){
    var tags = "";
    for(var i = 0; i < value.tags.length; ++i) {
        var tag = value.tags[i];
        tags = tags + "|" + tag;
    }
    if(tags.indexOf("|") == 0){
        tags = tags.substring(1);
    }

    return {
        tags: tags,
        id: value.id,
        mid: value.mid,
        url: value.url,
        domain: value.domain,
        site: value.site,
        count: value.count
    };
};

db.music_singer_tags.drop()
result = db.runCommand({
    mapreduce: "music",
    map: m,
    reduce: r,
    query: {
        "properties.singer": {
            "$ne": null
        },
        "tag": {
            "$ne": null
        }
    },
    out: "music_singer_tags",
    finalize:f,
    verbose: true
});
printjson(result);
