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

from test_base import t35t_case, check_output, run_command, CommandLineTestBase, make_setup_module, teardown_module, make_dirs, log

from os.path import join, split

setup_module = make_setup_module(__name__)

def get_output_path(base_path, suffix):
    return join(base_path, 'test_data-%s' % suffix)

def create_t35t(file, name):
    @t35t_case(name)
    def test_func(self):
        self.run_query(self.config.baseline_data_path, file, 'baseline')
        self.run_query(self.config.data_path, file, 'current')
        
        baseline = parse_scores(self.get_query_output_path(file, 'baseline'))
        subject = parse_scores(self.get_query_output_path(file, 'current'))
        compare_scores(name, baseline, subject)
        
    test_func.__name__ = name
    return test_func

def parse_scores(path):
    scores = {}
    for line in open(path, 'rU'):
        data = line.strip().split('\t')
        if len(data) == 2:
            scores[data[0]] = float(data[1])
    return scores

def compare_scores(name, baseline, subject):
    log('Analyzing %s...' % name)
    total = 0
    matches = 0
    header_printed = False
    for id in baseline:
        if id in subject:
            matches += 1
            subject_score = subject[id]
            baseline_score = baseline[id]
            error = subject_score / baseline_score - 1
            if error > 0:
                if not header_printed:
                    log('\tID\tBaseline\tActual\tError')
                    header_printed = True
                log('\t%s\t%f\t%f\t%f' % (id, baseline_score, subject_score, error))
        total += 1
    
    recall = float(matches) / total
    log('\tRecall: %.2f%%' % (recall * 100))
    assert recall == 1

class Tests(CommandLineTestBase):
    def __init__(self):
        super(Tests, self).__init__()
    
    def setup(self):
        super(Tests, self).setup()
        make_dirs(get_output_path(self.output_path, 'current'))
        make_dirs(get_output_path(self.output_path, 'baseline'))
        self.args += [
            'org.genemania.plugin.apps.QueryRunner',
            '--out', 'scores'
        ]
    
    def run_query(self, data_path, file, output_type):
        run_command(self.args + [ 
            '--data', data_path,
            '--results', get_output_path(self.output_path, output_type),
            self.get_data(file),
        ])
    
    def get_query_output_path(self, path, suffix):
        file_name = split(path)[-1]
        return join(get_output_path(self.output_path, suffix), '%s-results.scores.txt' % file_name)
    
for organism in [
    'arabidopsis',
    'worm',
    'fly',
    'human',
    'mouse',
    'yeast',
    'rat',
]:
    name = 'test_%s' % organism
    query = join('data', '%s.qr' % organism)
    setattr(Tests, name, create_t35t(query, name))
