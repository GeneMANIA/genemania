#!/bin/bash

# Note: This script requires UnZip 6.0 or later because some of the data
#       uses ZIP64 extensions.  Using earlier versions will cause an error
#       that says one or more zipfiles are corrupt.

mkdir genemania_plugin
pushd genemania_plugin

#DATA_ID=gmdata-2011-08-11-core
#DATA_ID=gmdata-2011-08-11-open_license
DATA_ID=gmdata-2011-08-11

curl -O http://www.genemania.org/plugin/data/${DATA_ID}.zip

ARABIDOPSIS=1
WORM=2
FLY=3
HUMAN=4
MOUSE=5
YEAST=6
RAT=7

#ORGANISMS="${ARABIDOPSIS} ${WORM} ${FLY} ${HUMAN} ${MOUSE} ${YEAST} ${RAT}"
ORGANISMS="${HUMAN}"

for ORGANISM in ${ORGANISMS}
do
    curl -O http://www.genemania.org/plugin/data/${DATA_ID}/${ORGANISM}.zip
    curl -O http://www.genemania.org/plugin/data/${DATA_ID}/${ORGANISM}.cache.zip
done
  
unzip ${DATA_ID}.zip
pushd ${DATA_ID}
for FILENAME in ../[1-7].zip
do
    unzip ${FILENAME}
done
mkdir cache
cd cache
for FILENAME in ../../[1-7].cache.zip
do
    unzip ${FILENAME}
done
popd

rm ${DATA_ID}.zip
rm [1-7].zip
rm [1-7].cache.zip
popd
