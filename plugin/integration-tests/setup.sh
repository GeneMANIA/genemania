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


if [ -z ${2} ]
then
    echo Usage: $(basename "$0") path/to/parent/of/generic_db path/to/unpacked-gmdata
    exit
fi

unlink gmdata-current
rm -rf gmdata-baseline

# Path to root of database build (the parent directory of generic_db).
# This data will be used as the baseline data set.
DB_BUILD="$1"

# Path to a deployed plugin data set.  This will be used as the primary data
# set.
PLUGIN_DATA="$2"

./link.sh "${DB_BUILD}" baseline
ln -s "${PLUGIN_DATA}" gmdata-current
