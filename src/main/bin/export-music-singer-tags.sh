#!/usr/bin/env bash

source ./config.sh

mongo ${mongo_host}:${mongo_port}/${mongo_db} ../js/export-music-singer-tags.js

## follow analysis
# db.music_singers.find({'value.site':'music.qq','value.count':{$gt:30}})
# db.music_singers.find().sort({'value.count': -1})