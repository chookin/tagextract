#!/usr/bin/env bash

# usage example:
# bash export-domain.sh book
# bash export-domain.sh goods
# bash export-domain.sh music

if [ $# -ne 1 ];then
 echo "No action."
 echo "Usage: bash export-domain.sh collection_name"
 exit -1
fi

source ./config.sh

collection=$1
if [[ ${collection} != db.* ]]; then
    mycollection="db.${collection}"
else
    mycollection=${collection}
fi

out="${out_dir}/${collection}.domains.json"
mongo ${mongo_host}:${mongo_port}/${mongo_db} --eval "var collection=${mycollection}" ../js/export-domain.js > ${out}