#!/usr/bin/env bash
# 一般情况下，电视剧、综艺、动漫剧等的每一剧集都对应不同的URL，即对应不同的Id，为此，导出标签和剧集Id的映射。
# 导出结果为csv文件，每一行记录为：
# 标签名，网站域名，剧集Id列表（剧集Id以‘|’分割）

# usage example:
# bash export-tag-ids.sh video
# bash export-tag-ids.sh anime

if [ $# -ne 1 ];then
 echo "No action."
 echo "Usage: bash export-tag-series-ids.sh collection_name"
 exit -1
fi

source ./config.sh

collection="$1"
mongo ${mongo_host}:${mongo_port}/${mongo_db} --eval "var collection='${collection}'" ../js/export-tag-series-ids.js

echo "use mongoexport to export mapreduce results to csv"
date=$(date +%Y%m%d%H%M%S)
out="${out_dir}/${collection}-tag-series-ids-${date}.csv"
query={'"value.ids"':{'"$ne"':null},'"value.type"':"\"${collection}\""}
echo -e "\nexport to ${out}"
mongoexport --host ${mongo_host}:${mongo_port} --db ${mongo_db} --collection tag_series_ids --csv --out ${out} --fields value.tag,value.domain,value.ids -q ${query}
sed -i "s/value.tag,value.domain,value.ids/标签树节点名称,domain,key/g" `grep value.tag,value.domain,value.ids -rl ${out}`

echo "Done, save results to ${out}."
