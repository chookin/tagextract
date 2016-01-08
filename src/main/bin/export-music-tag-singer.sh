#!/usr/bin/env bash

source ./config.sh

mongo ${mongo_host}:${mongo_port}/${mongo_db} ../js/export-music-tag-singer.js

echo "use mongoexport to export mapreduce results to csv"
date=$(date +%Y%m%d%H%M%S)

out="${out_dir}/music-tag-singerId-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection music_tag_singer --csv --out ${out} --fields value.tag,value.domain,value.id -q '{"value.id":{"$ne":null}}'
sed -i "s/value.tag,value.domain,value.id/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.id -rl ${out}`

out="${out_dir}/music-tag-singerMid-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection music_tag_singer --csv --out ${out} --fields value.tag,value.domain,value.mid -q '{"value.mid":{"$ne":null}}'
sed -i "s/value.tag,value.domain,value.mid/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.mid -rl ${out}`

out="${out_dir}/music-tag-singerName-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection music_tag_singer --csv --out ${out} --fields value.tag,value.domain,value.name -q '{"value.name":{"$ne":null}}'
sed -i "s/value.tag,value.domain,value.name/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.name -rl ${out}`