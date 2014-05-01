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


if [ -z $1 ]
then
    echo Usage: $(basename "$0") db.cfg [ data-id ]
    exit
fi

function abspath {
    # OS X's version of readlink doesn't support "-f" so we have to work around
    # it this way
    python -c 'import os, sys; print os.path.realpath(sys.argv[1])' "$1"
}

SCRIPTS=$(dirname "$0")
SCRIPTS=$(abspath "${SCRIPTS}")

# db.cfg
DB_CONFIG=$(abspath "$1")

# Dataset identifier
if [ -z $2 ]
then
    DATA_ID=$(date "+%Y-%m-%d")
else
    DATA_ID=$2
fi

BUILDDIR=${SCRIPTS}/build

mkdir -p ${BUILDDIR}

# Packaging core data set
echo Packaging core data set...
${SCRIPTS}/generate_dataset.py ${DATA_ID} "${DB_CONFIG}" "${BUILDDIR}"
