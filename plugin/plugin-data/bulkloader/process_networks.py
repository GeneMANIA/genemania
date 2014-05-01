#!/usr/bin/env python
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


import sys
import os

from os.path import join

batch_file_name = sys.argv[1]
network_dir = sys.argv[2]

organisms = {}
organism_ids = {}
organism_data = {}
organisms_by_group = {}

genes_file = open('GENES.txt', 'w')
nodes_file = open('NODES.txt', 'w')
gene_data_file = open('GENE_DATA.txt', 'w')

organism_id = 1
naming_source_id = 1

group_id = 1
groups = {}

network_id = 1

group_file = open('NETWORK_GROUPS.txt', 'w')
colour_file = open('colours.txt', 'w')
network_file = open('NETWORKS.txt', 'w')
metadata_file = open('NETWORK_METADATA.txt', 'w')
organism_file = open('ORGANISMS.txt', 'w')

try:
    os.mkdir('INTERACTIONS')
except:
    pass

def handle_ids(naming_source_id, organism_id, id_file_name):
    ids = []
    id_map = {}
    for line in open(id_file_name, 'rU'):
        ids.append(line.strip())
    for index, symbol in zip(range(len(ids)), ids):
        node_id = index + 1
        print >> nodes_file, '%(node_id)d\t%(symbol)s\t%(node_id)d\t%(organism_id)d' % locals()
        print >> genes_file, '%(node_id)d\t%(symbol)s\tN/A\t%(naming_source_id)d\t%(node_id)d\t%(organism_id)d\t0' % locals()
        print >> gene_data_file, '%(node_id)d\t%(symbol)s\t\t' % locals()
        id_map[symbol] = node_id
    return id_map

def handle_organism(data):
    global organism_id
    global naming_source_id
    
    ids = handle_ids(naming_source_id, organism_id, data[1])
    organism_ids[organism_id] = ids
    organism_data[organism_id] = data
    organisms[data[2]] = organism_id
    organism = [str(organism_id), data[2], data[3], data[4], '-1', data[5]]
    print >> organism_file, '\t'.join(organism)
    organism_id += 1
    
def handle_group(data):
    global group_id
    global group_file
    global colour_file
    global groups
        
    group = [str(group_id), data[1], data[2], data[3], str(organisms[data[5]])]
    print >> group_file, '\t'.join(group)
    print >> colour_file, '%s\t%s' % (data[2], data[4])
    groups[data[2]] = group_id
    organisms_by_group[group_id] = int(organisms[data[5]])
    group_id += 1

def count_interactions(organism_id, file_name):
    symbols = organism_ids[organism_id]
    interactions = 0
    for line in open(file_name, 'rU'):
        data = line.strip().split('\t')
        if data[0] in symbols and data[1] in symbols:
            interactions += 1
    return interactions

def write_network(organism_id, file_name, network_id):
    symbols = organism_ids[organism_id]
    output_file_name = '%d.%d.txt' % (organism_id, network_id)
    output_file = open(join('INTERACTIONS', output_file_name), 'w')
    
    for line in open(file_name, 'rU'):
        data = line.strip().split('\t')
        for i in [0, 1]:
            data[i] = str(symbols[data[i]])
        print >> output_file, '\t'.join(data)        
    
def handle_network(data):
    global network_id
    global network_file
    global metadata_file
    global groups
    
    group_id = groups[data[4]]
    organism_id = organisms_by_group[group_id]
    network = [str(network_id), data[2], str(network_id), data[3], '0', str(group_id)]
    print >> network_file, '\t'.join(network)
    
    file_name = join(network_dir, data[1].strip())
    interactions = count_interactions(organism_id, file_name)
    
    metadata = [str(network_id), '', '', '', '', '', '', '', '', '', str(interactions), '', '', '0', '', '', '', '', '']
    print >> metadata_file, '\t'.join(metadata)
    
    write_network(organism_id, file_name, network_id)
    network_id += 1

def write_configuration(file_name, organism_data):
    output_file = open(file_name, 'w')
    print >> output_file, '''[FileLocations]
generic_db_dir = .'''

    organisms = ['org_%d' % id for id in organism_data.keys()]
    print >> output_file, '''[Organisms]
organisms = %s''' % (','.join(organisms),)

    for id, data in organism_data.items():
        print >> output_file, '''[org_%d]
gm_organism_id = %d
short_name = %s
common_name = %s
''' % (id, id, data[2], data[3])

def write_naming_sources():
    output_file = open('GENE_NAMING_SOURCES.txt', 'w')
    print >> output_file, '''1	Other	1	Other
2	Entrez Gene ID	0	Entrez Gene ID'''

for line in open(batch_file_name, 'rU'):
    data = line.strip().split('\t')
    if data[0] == 'organism':
        handle_organism(data)
    if data[0] == 'group':
        handle_group(data)
    if data[0] == 'network':
        handle_network(data)
    write_naming_sources()
    write_configuration('db.cfg', organism_data)

