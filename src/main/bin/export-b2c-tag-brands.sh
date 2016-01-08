#!/usr/bin/env bash

source ./config.sh

mongo ${mongo_host}:${mongo_port}/${mongo_db} ../js/export-b2c-tag-brands2.js

echo "use mongoexport to export mapreduce results to csv"
date=$(date +%Y%m%d%H%M%S)
collection="goods_tag_brands"
out="${out_dir}/goods-tag-brands-${date}.csv"
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection goods_tag_brands --csv --out ${out} --fields value.tag,value.domain,value.brands -q '{"value.brands":{"$ne":null}}'
sed -i "s/value.tag,value.domain,value.brands/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.brands -rl ${out}`