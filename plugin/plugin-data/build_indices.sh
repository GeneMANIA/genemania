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


if [ -z $4 ]
then
    echo Usage: $(basename "$0") db.cfg /path/to/netmania/build colours.txt data-id [ profile ]
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

echo $BASE

JAVA_OPTS="-Xmx1500M -cp ${BASE}/distribution/target/genemania-cytoscape-plugin-*.jar"

echo $JAVA_OPTS

# db.cfg
DB_CONFIG=$(abspath "$1")

# Path to root of database build (the parent directory of generic_db)
DB_ROOT=$(abspath "$2")

COLOURS=$(abspath "$3")

# Dataset identifier
DATA_ID=$4

if [ -z $5 ]
then
    PROFILE=all
else
    PROFILE=$5
fi


GM_DATA=gmdata-${DATA_ID}

BUILDDIR=${SCRIPTS}/build
INDEXDIR=${BUILDDIR}/index
DISTDIR=${BUILDDIR}/dist
DATADIR=${DISTDIR}/${GM_DATA}

rm -rf "${INDEXDIR}"

mkdir -p "${BUILDDIR}"
mkdir -p "${INDEXDIR}"
mkdir -p "${DISTDIR}"
mkdir -p "${DATADIR}"

# Build Lucene indices
pushd "${INDEXDIR}"
echo Building Lucene indices in `pwd`
echo java ${JAVA_OPTS} org.genemania.mediator.lucene.exporter.Generic2LuceneExporter "${DB_CONFIG}" "${DB_ROOT}" "${COLOURS}" "${PROFILE}"
java ${JAVA_OPTS} org.genemania.mediator.lucene.exporter.Generic2LuceneExporter "${DB_CONFIG}" "${DB_ROOT}" "${COLOURS}" "${PROFILE}"
popd

echo "running IndexPostProcessor in `pwd`"
echo java ${JAVA_OPTS} org.genemania.plugin.apps.IndexPostProcessor --target "${INDEXDIR}" --source "${DB_ROOT}/lucene_index"
java ${JAVA_OPTS} org.genemania.plugin.apps.IndexPostProcessor --target "${INDEXDIR}" --source "${DB_ROOT}/lucene_index"
