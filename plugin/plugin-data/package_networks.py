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


import glob
import sys
import zipfile

from zipfile import ZipFile, ZIP_DEFLATED
from configobj import ConfigObj
from os.path import join, basename
from os import walk

def main(config_dir, cache_dir):
    config = ConfigObj(config_dir)
    for organism_code in config['Organisms']['organisms']:
        organism = config[organism_code]
        zip_name = ('%s.cache.zip' % (organism['gm_organism_id']))
        
        # Apache on MAC OS X doesn't like spaces in the path name so we'll
        # URL encode them.  Not sure if this hack works for other OSes...
        zip_name = zip_name.replace(' ', '+')
        
        organism_id = organism['gm_organism_id']
        package_organism(zip_name, organism_id, cache_dir)

def package_organism(zip_path, organism_id, cache_dir):
    print zip_path
    out = ZipFile(zip_path, 'w', allowZip64=True)
    for root, dirs, files in walk(join(cache_dir, 'CORE', organism_id)):
        zip_root = root[len(cache_dir) + 1:]
        for name in files:
            write_zip_entry(join(root, name), join(zip_root, name), out)
    out.close()

def add_directory(path, zipfile):
    for path in glob.glob(join(cache_dir, 'CORE', '%s' % organism_id)):
        write_zip_entry(path, out)

def write_zip_entry(real_path, zip_path, zipfile):
    zipfile.write(real_path, zip_path, ZIP_DEFLATED)
    
if __name__ == '__main__':
    config_dir = sys.argv[1]
    cache_dir = sys.argv[2]
    main(config_dir, cache_dir)
