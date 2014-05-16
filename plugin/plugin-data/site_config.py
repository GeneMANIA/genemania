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

base_url = 'http://www.genemania.org/plugin/data'

data_sets = [
    {
        'name': '2013-10-15',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': '15 October 2013',
        'internal_version': 'r8',
    },
    {
        'name': '2012-08-02',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': '19 July 2012',
        'internal_version': 'r6',
    },
    {
        'name': '2012-05-25',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': '21 December 2011',
        'internal_version': 'r5b2.3',
        # This data set is a replacement for 2012-01-06.
    },
    {
        'name': '2012-01-06',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': 'N/A',
        'internal_version': 'r5b2.3',
        # This data set has unsparsified co-expression networks so although
        # it's based on r5b2.3, it has no equivalent website version.
    },
    {
        'name': '2011-08-11',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': '3 August 2011',
        'internal_version': 'r3b2.6',
    },
    {
        'name': '2011-03-07',
        'profiles': ['', 'core', 'open_license'],
        'equivalent_website_version': '3 March 2011',
        'internal_version': 'r2b4.8',
    },
    {
        'name': '2010-12-01',
        'profiles': [''],
        'equivalent_website_version': '23 September 2010',
        'internal_version': 'r1b25',
    },
    {
        'name': '2010-08-25',
        'profiles': [''],
        'equivalent_website_version': 'N/A',
        'internal_version': 'r1b24',
    },
]

schema_compatibility = {
    'dev': [
        '2013-10-15',
        '2012-08-02',
        '2012-05-25',
        '2012-01-06',
        '2011-08-11',
        '2011-03-07',
        '2010-12-01',
        '2010-08-25',
    ],
    '1.1': [
        '2013-10-15',
        '2012-08-02',
        '2012-05-25',
        '2012-01-06',
        '2011-08-11',
        '2011-03-07',
        '2010-12-01',
        '2010-08-25',
    ],
    '1.0': [
        '2010-12-01',
        '2010-08-25',
    ],
}
