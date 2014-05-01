#!/usr/bin/python
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

from configobj import ConfigObj
from xml.sax.saxutils import escape
from os.path import join, walk, split, exists, getsize
from zipfile import ZipFile, ZIP_DEFLATED

def generate_dataset(id, db_config, build_dir):
    '''Generate all the bits and pieces needed to create a plugin data
       distribution site.'''
    
    config = ConfigObj(db_config)
    
    temp_dir = join(build_dir, 'temp')
    if not exists(temp_dir):
        os.makedirs(temp_dir)
    
    generate_dataset_xml(id, temp_dir)
    generate_dataset_links(temp_dir)
    generate_data_list(id, build_dir, config)
    package_dataset(id, build_dir, temp_dir)

def generate_data_list(id, build_dir, config):
    '''Generate the XML that describes the data available for download for this
       data set.  Currently, the data is broken up by organism but we're free
       to slice this whatever way we want.'''
    
    xml = join(build_dir, 'dist', 'gmdata-%s.xml' % id)
    output = open(xml, 'w')
    
    print >>output, '''<?xml version="1.0" encoding="utf-8"?>
<data-set version="%s">''' % escape(id)
    
    for organism_code in config['Organisms']['organisms']:
        organism = config[organism_code]
        path = '%s' % (organism['gm_organism_id'])
        name = organism['short_name']
        
        organism_prefix = path.replace(' ', '+')
        size = compute_size(organism_prefix, id, build_dir) / 1024.0 / 1024.0
        
        description = '%s (%d MB)' % (organism['common_name'].capitalize(), size)
        print >>output, '''    <data path="%s">
        <name>%s</name>
        <description>%s</description>
    </data>''' % (path, name, description)

    print >>output, '</data-set>'
    output.close()

def compute_size(organism_prefix, id, build_dir):
    base_path = join(build_dir, 'dist', 'gmdata-%s' % id)
    suffixes = ['%s.zip', '%s.cache.zip']
    paths = map(lambda x: join(base_path, x % organism_prefix), suffixes)
    sizes = map(getsize, paths)
    return reduce(lambda x, y: x + y, sizes)

def package_dataset(id, build_dir, temp_dir):
    data_dir = join(build_dir, 'dist', 'gmdata-%s' % id)
    if not exists(data_dir):
        os.makedirs(data_dir)
        
    zip_path = join(build_dir, 'dist', 'gmdata-%s.zip' % id)
    zipfile = ZipFile(zip_path, 'w')
    base_index = join(build_dir, 'index', 'base')
    
    walk(temp_dir, process_zip, (zipfile, temp_dir, id))
    walk(base_index, process_zip, (zipfile, parent_path(base_index), id))
    
    zipfile.close()

def parent_path(path):
    return join(*split(path)[0:-1])

def process_zip(options, dirname, files):
    zipfile, base, id = options
    prefix = 'gmdata-%s' % id
    for file in files:
        path = join(dirname, file)
        zipname = join(prefix, make_relative(base, path))
        print '%s\t%s' % (path, zipname)
        zipfile.write(path, zipname, ZIP_DEFLATED)

def make_relative(base, path):
    '''Compute the path to path relative to base;
       e.g. make_relative('a/b', 'a/b/foo/bar') returns 'foo/bar'.
       This isn't smart enough to make paths involving '..'.'''
    if path.startswith(base):
        path = path[len(base):]
        if path.startswith('/'):
            return path[1:]
    return base

def generate_organism_metadata(id, config):
    pass

def generate_dataset_xml(id, path):
    xml = join(path, 'genemania.xml')
    output = open(xml, 'w')
    print >>output, '''<?xml version="1.0" encoding="utf-8"?>
<genemania>
    <type>org.genemania.data.lucene.LuceneDataSet</type>
    <data-version>%s</data-version>
    <access-mode>compact</access-mode>
</genemania>''' % escape(id)
    output.close()

def generate_dataset_links(path):
    filename = join(path, 'external.properties')
    output = open(filename, 'w')
    print >>output, '''ensembl = http://www.ensembl.org/${2}/geneview?gene=${1}
entrez = http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=search&term=${1}
refseq = http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=${1}
taird = http://arabidopsis.org/servlets/TairObject?type=locus&name=${1}
uniprot = http://www.uniprot.org/uniprot/${1}'''
    output.close()

if __name__ == '__main__':
    id = sys.argv[1]
    db_config = sys.argv[2]
    build_dir = sys.argv[3]
    generate_dataset(id, db_config, build_dir)
