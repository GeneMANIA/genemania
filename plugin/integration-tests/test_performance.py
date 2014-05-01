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

from test_base import t35t_case, check_output, run_command, CommandLineTestBase, make_setup_module, teardown_module, make_dirs

from os.path import join

setup_module = make_setup_module(__name__)

def get_output_path(base_path):
    return join(base_path, 'test_performance')

def create_t35t(file, name):
    @t35t_case(name)
    def test_func(self):
        self.args += [
            '--results', get_output_path(self.output_path),
            self.get_data(file),
        ]
        run_command(self.args)
    test_func.__name__ = name
    return test_func
    
class Tests(CommandLineTestBase):
    def __init__(self):
        super(Tests, self).__init__()
    
    def setup(self):
        super(Tests, self).setup()
        make_dirs(get_output_path(self.output_path))
        self.args += [
            'org.genemania.plugin.apps.QueryRunner',
            '--data', self.config.data_path,
        ]
    
for organism in [
    'arabidopsis',
    'worm',
    'fly',
    'human',
    'mouse',
    'yeast',
]:
    name = 'test_%s' % organism
    query = join('performance', '%s.qr' % organism)
    setattr(Tests, name, create_t35t(query, name))
