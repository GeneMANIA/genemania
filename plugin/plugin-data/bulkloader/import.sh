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


GENEMANIA_JAR="$1"
NETWORK_DIR="$2"

mkdir temp
pushd temp

for f in ONTOLOGY_CATEGORIES.txt ONTOLOGIES.txt TAGS.txt NETWORK_TAG_ASSOC.txt INTERACTIONS.txt
do
    touch $f
done

cp ../batch.txt ../ids.txt .
python ../process_networks.py batch.txt "${NETWORK_DIR}"

popd

mkdir dataset
pushd dataset

java -cp ${GENEMANIA_JAR} org.genemania.mediator.lucene.exporter.Generic2LuceneExporter ../temp/db.cfg ../temp ../temp/colours.txt

java -cp ${GENEMANIA_JAR} org.genemania.engine.apps.CacheBuilder -cachedir cache -indexDir . -networkDir ../temp/INTERACTIONS

popd

cp genemania.xml dataset
