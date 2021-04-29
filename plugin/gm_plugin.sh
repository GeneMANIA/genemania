#!/bin/bash

# build GeneMANIA plugin for Cytoscape

STAGING_DIR="$1"
PLUGIN_DIR="$2"
SRCDB="$3"

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 staging_dir plugin_dir srcdb"
    echo "Eg   : $0 /gm/db_build/r14.3  /gm/dev/r14.3/src/plugin  /gm/dev/r14.3/db"
    exit 0
fi

echo "Building plugin data"
pushd ${PLUGIN_DIR}/distribution
#mvn clean assembly:single
#if [[ $? -ne 0 ]]; then
#    echo "Plugin assembly failed"
#    exit 1
#fi
popd

pushd ${PLUGIN_DIR}/plugin-data
rm -rf build
echo "Building indices"
./build_indices.sh ${SRCDB}/db.cfg ${STAGING_DIR} colours.txt refresh all
cd build/index/lucene_index

for i in *; do 
    echo "Copying $i to ${STAGING_DIR}/lucene_index"
    cp -r ${i} ${STAGING}/lucene_index/${i}
done
popd

echo "Creating network cache"
pushd ${PLUGIN_DIR}/plugin-data
echo $PWD
echo "SRCDB= ${SRCDB}, Staging_dir= ${STAGING_DIR}"
./build_networks.sh ${SRCDB}/db.cfg ${STAGING_DIR} refresh
./package_indices.sh refresh all
./build_core_data_set.sh ${SRCDB}/db.cfg refresh

pushd build/temp
mkdir -p ${STAGING_DIR}/lucene_index/gmdata-refresh
cp external.properties genemania.xml ${STAGING_DIR}/lucene_index/gmdata-refresh
popd

for profile in "" core open_license; do 
    echo "Packaging profile ${profile}"
    ./package_data.sh ${SRCDB}/db.cfg ${STAGING_DIR} colours.txt auto ${profile}
done

mkdir -p ${STAGING_DIR}/app_data
cp -r build/dist/gmdata-[0-9]* ${STAGING_DIR}/app_data
popd

echo "Done"
