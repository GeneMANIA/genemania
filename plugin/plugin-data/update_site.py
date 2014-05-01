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


import site_config
import sys
import os
import re
import stat

from os.path import join, walk

def get_data_set(id):
    for data_set in site_config.data_sets:
        if data_set['name'] == id:
            return data_set
    return None

def get_data_set_names(data_set):
    ids = []
    for profile in data_set['profiles']:
        if len(profile) == 0:
            ids.append(data_set['name'])
        else:
            ids.append('%s-%s' % (data_set['name'], profile))
    return ids

def get_url(data_name):
    return '%s/gmdata-%s' % (site_config.base_url, data_name)

class Counter(object):
    def __init__(self):
        self.count = 0

    def add(self, value):
        self.count += value

def handler(counter, root, names):
    for name in names:
        path = join(root, name)
        info = os.lstat(path)
        if stat.S_ISREG(info.st_mode):
            counter.add(info.st_size)

def compute_size(path):
    counter = Counter()
    walk(path, handler, counter)
    return counter.count

def process_schemas(schemas, data_path='.'):
    for version, ids in schemas.items():
        file_name = join(data_path, 'schema-%s.txt' % version)
        out = open(file_name, 'w')
        try:
            for base_id in ids:
                data_set = get_data_set(base_id)
                if not data_set:
                    print >> sys.stderr, 'Warning: Data set "%s" not found.' % base_id
                    continue
                for data_name in get_data_set_names(data_set):
                    print >> out, get_url(data_name)
        finally:
            out.close()

def process_sizes(data_sets, data_path='.'):
    file_name = join(data_path, 'sizes.txt')
    out = open(file_name, 'w')
    try:    
        for data_set in data_sets:
            for data_name in get_data_set_names(data_set):
                path = join(data_path, 'gmdata-%s' % data_name)
                size = compute_size(path) / 1024
                print >> out, '%s = %d' % (data_name, size)
    finally:
        out.close()

def process_descriptions(data_sets, data_path='.'):
    file_name = join(data_path, 'descriptions.txt')
    out = open(file_name, 'w')
    try:    
        for data_set in data_sets:
            for data_name in get_data_set_names(data_set):
                print >> out, '%s = %s # %s' % (
                    data_name,
                    data_set['equivalent_website_version'],
                    data_set['internal_version'])
    finally:
        out.close()

if __name__ == '__main__':
    data_path = sys.argv[1]
    
    process_schemas(site_config.schema_compatibility, data_path)
    process_sizes(site_config.data_sets, data_path)
    process_descriptions(site_config.data_sets, data_path)
