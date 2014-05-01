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
    echo Usage: $(basename "$0") db.cfg /path/to/netmania/build data-id [ cache-dir ]
    exit
fi

function abspath {
    # OS X's version of readlink doesn't support "-f" so we have to work around
    # it this way
    python -c 'import os, sys; print os.path.realpath(sys.argv[1])' "$1"
}

RESOURCES=$(dirname "$0")
RESOURCES=$(abspath "${RESOURCES}")

# db.cfg
DB_CONFIG=$(abspath "$1")

# Path to root of database build (the parent directory of generic_db)
DB_ROOT=$(abspath "$2")

# Dataset identifier
DATA_ID="$3"

if [ -z $4 ]
then
    CACHEDIR="${DB_ROOT}/network_cache"
else
    CACHEDIR="$4"
fi

GM_DATA=gmdata-${DATA_ID}

BUILDDIR=${RESOURCES}/build
DISTDIR=${BUILDDIR}/dist
DATADIR=${DISTDIR}/${GM_DATA}

mkdir -p ${BUILDDIR}
mkdir -p ${DISTDIR}
mkdir -p ${DATADIR}

# Packaging network cache
echo Packaging network caches...
pushd "${DATADIR}"
${RESOURCES}/package_networks.py "${DB_CONFIG}" "${CACHEDIR}"
popd
