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


# Creates an HTML file containing all the recent plugin builds, starting with
# the most recent.

import os
import sys
import re

def genindex(root):
    print '''<html>
<head>
<title>GeneMANIA Cytoscape Plugin Builds</title>
</head>
<body>
<h1>GeneMANIA Cytoscape Plugin Builds</h1>
<ul>
'''

    entries = []
    filenames = {}
    for filename in os.listdir(root):
        matches = re.match('genemania-cytoscape-plugin-(.*?)(-(\d+[.]\d+))?.jar', filename)
        if matches:
            milestone = matches.group(1)
            version = matches.group(3)
            if version:
                entry = 'Build %s (v%s)' % (milestone, version)
            else:
                entry = 'Build %s' % milestone
            entries.append(entry)
            filenames[entry] = filename
    
    sorted = natsort(entries)
    sorted.reverse()
    for entry in sorted:
        print '<li><a href="%s">%s</a></li>' % (filenames[entry], entry)
        
    print '''
</ul>
</html>'''

# Taken from http://code.activestate.com/recipes/285264-natural-string-sorting/
def natsort(list_):
    # decorate
    tmp = [ (int(re.search('\d+', i).group(0)), i) for i in list_ ]
    tmp.sort()
    # undecorate
    return [ i[1] for i in tmp ]

if __name__ == '__main__':
    if len(sys.argv) < 2:
        root = '.'
    else:
        root = sys.argv[1]
    genindex(root)
