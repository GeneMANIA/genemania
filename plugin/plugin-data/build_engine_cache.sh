#!/bin/bash
#
# This file is part of GeneMANIA.
# Copyright (C) 2008-2011 University of Toronto.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
#


if [ -z $2 ]
then
    echo Usage: $(basename "$0") /path/to/netmania/build [ data-id ] [ profile ]
    exit
fi

function abspath {
    # OS X's version of readlink doesn't support "-f" so we have to work around
    # it this way
    python -c 'import os, sys; print os.path.realpath(sys.argv[1])' "$1"
}

SCRIPTS=$(dirname "$0")
SCRIPTS=$(abspath "${SCRIPTS}")
BASE=$(abspath ${SCRIPTS}/..)

JAVA_OPTS="-Xmx1500M -cp ${BASE}/distribution/target/genemania-cytoscape-plugin-*.jar"

# Path to root of database build (the parent directory of generic_db)
DB_ROOT=$(abspath "$1")

# Dataset identifier
if [ -z $2 ]
then
    DATA_ID=$(date "+%Y-%m-%d")
else
    DATA_ID=$2
fi

if [ -z $3 ]
then
    PROFILE=all
else
    PROFILE="$3"
fi

GM_DATA=gmdata-${DATA_ID}

BUILDDIR=${SCRIPTS}/build
DISTDIR=${BUILDDIR}/dist
DATADIR=${DISTDIR}/${GM_DATA}
TEMPDIR=${BUILDDIR}/cache
CACHEDIR=${DB_ROOT}/network_cache
GENERICDB_DIR=${DB_ROOT}/generic_db

LUCENE_INDEX_DIR=${BUILDDIR}/index/lucene_index
GO_CATEGORIES_DIR=${DB_ROOT}/GoCategories

# Test whether we have GNU or BSD find
FIND_OPTS=$(find -E . &> /dev/null && echo " -E")

mkdir -p ${BUILDDIR}
mkdir -p ${DATADIR}

rm -rf ${TEMPDIR}
mkdir -p ${TEMPDIR}

echo Building network caches...
java ${JAVA_OPTS} org.genemania.engine.apps.CacheBuilder -cachedir "${TEMPDIR}" -networkDir "${DB_ROOT}"/generic_db/INTERACTIONS -indexDir "${LUCENE_INDEX_DIR}"

for ORGANISM_PATH in ${TEMPDIR}/CORE/*
do
    echo Processing ${ORGANISM_PATH}...
    ORGANISM_ID=$(basename "${ORGANISM_PATH}")
    # Copy node degrees
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}/nodeDegrees.ser" "${TEMPDIR}/CORE/${ORGANISM_ID}"
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}/DatasetInfo.ser" "${TEMPDIR}/CORE/${ORGANISM_ID}"
    
    # Copy annotation and enrichment analysis data
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}"/*.CoAnnotationSet.ser "${TEMPDIR}/CORE/${ORGANISM_ID}"
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}"/*.GoAnnos.ser "${TEMPDIR}/CORE/${ORGANISM_ID}"
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}"/*.GoIds.ser "${TEMPDIR}/CORE/${ORGANISM_ID}"
    cp "${CACHEDIR}/CORE/${ORGANISM_ID}"/*.categoryIds.ser "${TEMPDIR}/CORE/${ORGANISM_ID}"
    
    # Copy network cache
    pushd ${TEMPDIR}
    find ${FIND_OPTS} . -regex ".*/CORE/${ORGANISM_ID}/[0-9]+.ser" -exec cp "${CACHEDIR}/{}" '{}' ';'
    popd
done

# Build fast weighting
java ${JAVA_OPTS} org.genemania.engine.apps.FastWeightCacheBuilder -indexDir "${LUCENE_INDEX_DIR}" -cachedir "${TEMPDIR}" -qdir "${GO_CATEGORIES_DIR}/"

# Build attribute data
java ${JAVA_OPTS} org.genemania.engine.apps.AttributeBuilder -indexDir "${LUCENE_INDEX_DIR}" -cachedir "${TEMPDIR}" -genericDbDir "${GENERICDB_DIR}"

# Build precombined networks
java ${JAVA_OPTS} org.genemania.engine.apps.NetworkPrecombiner -indexDir "${LUCENE_INDEX_DIR}" -cachedir "${TEMPDIR}"

