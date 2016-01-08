#!/usr/bin/env bash

source ./config.sh

mongo ${mongo_host}:${mongo_port}/${mongo_db} ../js/export-music-tag-albums.js

echo "use mongoexport to export mapreduce results to csv"
date=$(date +%Y%m%d%H%M%S)
out="${out_dir}/music-tag-albumNames-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection music_tag_albums --csv --out ${out} --fields value.tag,value.domain,value.names -q '{"value.names":{"$ne":null}}'
sed -i "s/value.tag,value.domain,value.names/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.names -rl ${out}`

out="${out_dir}/music-tag-albumIds-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection music_tag_albums --csv --out ${out} --fields value.tag,value.domain,value.ids -q '{"value.ids":{"$ne":null}}'

sed -i "s/value.tag,value.domain,value.ids/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.ids -rl ${out}`