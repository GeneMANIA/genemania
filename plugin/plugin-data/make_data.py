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
import os.path

def create_settings(version, cache, driver, url, dialect, username, password):
    return {
        'version': version,
        'cache': cache,
        'driver': driver,
        'url': url,
        'dialect': dialect,
        'username': username,
        'password': password,
    }

def create_xml(path, settings):
    xml = """<?xml version='1.0' encoding='utf-8'?>
<genemania>
    <data-version>%(version)s</data-version>
    <cache-path>%(cache)s</cache-path>
    <connection>
        <driver>%(driver)s</driver>
        <url>%(url)s</url>
        <dialect>%(dialect)s</dialect>
        <username>%(username)s</username>
        <password>%(password)s</password>
    </connection>
</genemania>
""" % settings
    file = open(path,'w')
    print >> file, xml
    
def create_data(version, target_configurations):
    cache = 'cache'
    username = 'genemania'
    password = 'password'
    
    configurations = {
        'mysql': ['com.mysql.jdbc.Driver',
                  'jdbc:mysql://localhost:3306/genemania',
                  'org.hibernate.dialect.MySQLDialect'],
        
        'derby': ['org.apache.derby.jdbc.EmbeddedDriver',
                  'jdbc:genemania:db',
                  'org.genemania.util.DerbyDialect2'],
    }
    
    if len(target_configurations) == 0:
        target_configurations = configurations.keys()
    
    build_configurations = [(backend, config) for (backend, config) in configurations.items() if backend in target_configurations]
    
    for backend, config in build_configurations:
        settings = create_settings(version, cache, config, username, password)
        base_path = '%s-%s' % (version, backend)
        os.makedirs(base_path)
        xml_path = os.path.join(base_path, 'genemania.xml')
        create_xml(xml_path, settings)

if __name__ == '__main__':
    version = sys.argv[1]
    target_configurations = sys.argv[2:]
    create_data(version, target_configurations)
