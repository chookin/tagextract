/**
 * Created by zhuyin on 4/24/15.
 */
// export albums classified by tag.

m = function() {
    for (var i = 0; i < this.tag.length; ++i) {
        var item = this.tag[i];
        var albumIdsArr = [];
        if(this.properties.albumId != null){
            albumIdsArr = [this.properties.albumId];
        }
        emit(this.domain + "||" + item,
            {
                names: [this.properties.album],
                ids: albumIdsArr
            });
    }
};

r = function(key, values){
    var albumNamesArr = [];
    var albumIdsArr = [];
    for(var i = 0; i< values.length; ++i){
        var value = values[i];
        for(var j= 0; j < value.names.length; ++j){
            var name = value.names[j];
            if(albumNamesArr.indexOf(name) == -1){
                albumNamesArr.push(name);
            }
        }
        for(var j= 0; j < value.ids.length; ++j){
            var id = value.ids[j];
            if(albumIdsArr.indexOf(id) == -1){
                albumIdsArr.push(id);
            }
        }
    }
    return {
        names:albumNamesArr,
        ids: albumIdsArr
    };
};

f = function(key, value){
    var albumNames = "";
    var albumIds = "";
    for(var i = 0; i < value.names.length; ++i) {
        var name = value.names[i];
        albumNames = albumNames + "|" + name;
    }
    for(var i = 0; i < value.ids.length; ++i){
        var id = value.ids[i];
        albumIds = albumIds + "|" + id;
    }
    if(albumNames.indexOf("|") == 0){
        albumNames = albumNames.substring(1);
    }
    if(albumIds.indexOf("|") == 0){
        albumIds = albumIds.substring(1);
    }

    var keyArr = key.split("||");
    return {
        domain: keyArr[0],
        tag: keyArr[1],
        names: albumNames,
        ids: albumIds
    };
};

db.music_tag_albums.drop()
result = db.runCommand({
    mapreduce: "music",
    map: m,
    reduce: r,
    query: {
        "properties.album": {
            "$ne": null
        },
        "tag": {
            "$ne": null
        }
    },
    out: "music_tag_albums",
    finalize:f,
    verbose: true
});
printjson(result);
