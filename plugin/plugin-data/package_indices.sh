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
    echo Usage: $(basename "$0") data-id profile
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

# Dataset identifier
DATA_ID=$1

PROFILE=$2

GM_DATA=gmdata-${DATA_ID}

BUILDDIR=${SCRIPTS}/build
DISTDIR=${BUILDDIR}/dist
DATADIR=${DISTDIR}/${GM_DATA}
INDEXDIR=${BUILDDIR}/index

# Packaging Lucene indices
echo Packaging Lucene indices...
pushd "${INDEXDIR}/lucene_index"
for INDEX in [0-9]*
do
    # Apache on MAC OS X doesn't seem to like spaces in the path name so we'll
    # URL encode them.  Not sure if this hack works on other OSes...
    BASENAME=${DATADIR}/$(echo "${INDEX}" | sed -e "s/ /+/g")
    ZIP=${BASENAME}.zip
    zip -r "${ZIP}" "${INDEX}"
    cp "${INDEX}/metadata.xml" "${BASENAME}.xml"
done
popd
