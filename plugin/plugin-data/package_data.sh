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


if [ -z $3 ]
then
    echo Usage: $(basename "$0") db.cfg /path/to/netmania/build colours.txt "( data-id  | 'auto' )" [ profile ]
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

COLOURS=$(abspath "$3")

if [ $4 = 'auto' ]
then
    DATA_ID=$(date "+%Y-%m-%d")
else
    DATA_ID=$4
fi

PROFILE=$5

if [ ${PROFILE} ]
then
    PROFILE=$(abspath "${PROFILE}")
    DATA_ID="${DATA_ID}-$(basename "${PROFILE}")"
else
    PROFILE=all
fi

CACHEDIR="${RESOURCES}/build/cache"

${RESOURCES}/build_indices.sh "${DB_CONFIG}" "${DB_ROOT}" "${COLOURS}" "${DATA_ID}" "${PROFILE}"

if [ ${PROFILE} = 'all' ]
then
    # Just package up existing engine cache as-is
    ${RESOURCES}/build_networks.sh "${DB_CONFIG}" "${DB_ROOT}" "${DATA_ID}"
else
    # Rebuild subset of engine cache based on the selected profile
    ${RESOURCES}/build_engine_cache.sh "${DB_ROOT}" "${DATA_ID}"
    ${RESOURCES}/build_networks.sh "${DB_CONFIG}" "${DB_ROOT}" "${DATA_ID}" "${CACHEDIR}"
fi

${RESOURCES}/package_indices.sh "${DATA_ID}" "${PROFILE}"
${RESOURCES}/build_core_data_set.sh "${DB_CONFIG}" "${DATA_ID}"

echo Done.
