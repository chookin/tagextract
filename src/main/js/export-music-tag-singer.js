/**
 * Created by work on 4/29/15.
 */

// export tag of singer and singer's infos.
m = function() {
    for (var i = 0; i < this.tag.length; ++i) {
        var item = this.tag[i];
        if(item.indexOf("/歌手/") == -1) {
            continue;
        }
        var singerName = item.substr(item.lastIndexOf("/")+1);
        for (var j = 0; j < this.properties.singer.length; ++j) {
            var singer = this.properties.singer[j];
            if(singer.name != singerName){
                continue;
            }
            emit(this.domain + "||" + item,
                {
                    name: singer.name,
                    id: singer.id,
                    mid: singer.mid,
                    url: singer.url
                });
            return;
        }
    }
};

r = function(key, values){
    return values[0];
};

f = function(key, value){
    var keyArr = key.split("||");
    return {
        domain: keyArr[0],
        tag: keyArr[1],
        name: value.name,
        id: value.id,
        mid: value.mid,
        url: value.url
    };
};

db.music_tag_singer.drop()
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
    out: "music_tag_singer",
    finalize:f,
    verbose: true
});
printjson(result);