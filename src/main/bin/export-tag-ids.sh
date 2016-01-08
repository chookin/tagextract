#!/usr/bin/env bash

# usage example:
# bash export-tag-ids.sh book
# bash export-tag-ids.sh goods
# bash export-tag-ids.sh music
# bash export-tag-ids.sh video
# bash export-tag-ids.sh anime

if [ $# -ne 1 ];then
 echo "No action."
 echo "Usage: bash export-tag-ids.sh collection_name"
 exit -1
fi

source ./config.sh

collection="$1"
mongo ${mongo_host}:${mongo_port}/${mongo_db} --eval "var collection='${collection}'" ../js/export-tag-ids.js

echo "use mongoexport to export mapreduce results to csv"
date=$(date +%Y%m%d%H%M%S)
out="${out_dir}/${collection}-tag-ids-${date}.csv"
query={'"value.ids"':{'"$ne"':null},'"value.type"':"\"${collection}\""}
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection tag_ids --csv --out ${out} --fields value.tag,value.domain,value.ids -q ${query}
sed -i "s/value.tag,value.domain,value.ids/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.ids -rl ${out}`

echo "Done, save results to ${out}."
