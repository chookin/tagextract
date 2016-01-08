#!/usr/bin/env bash

# usage example:
# bash update-domain.sh db.book
# bash update-domain.sh db.goods
# bash update-domain.sh db.music

if [ $# -ne 1 ];then
 echo "No action."
 echo "Usage: bash update-domain.sh collection_name"
 exit -1
fi

source ./config.sh

collection=$1
collection=$1
if [[ ${collection} != db.* ]]; then
    mycollection="db.${collection}"
else
    mycollection=${collection}
fi

mongo ${mongo_host}:${mongo_port}/${mongo_db} --eval "var collection=${mycollection}" ../js/update-domain.js