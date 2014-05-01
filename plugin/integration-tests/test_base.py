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

import os
import errno
import re
import subprocess
import time
import config

from os.path import join, isfile
from functools import wraps

__jar_base_path__ = join('..', 'distribution', 'target')
__jar_pattern__ = re.compile('genemania-cytoscape-plugin-(.*).jar')
__output_path__ = 'output'
__expected_output_path__ = 'expected_output'
__log_file__ = None
__performance_file__ = None

def find_jar(root):
    for name in os.listdir(root):
        match = __jar_pattern__.match(name)
        if match:
            return join(root, name)
    return None

def make_setup_module(name):
    def setup_module():
        make_dirs('output')
        
        global __log_file__
        __log_file__ = open(join(__output_path__, '%s.log' % name), 'w')
        
        global __performance_file__
        __performance_file__ =  open(join(__output_path__, '%s-performance.log' % name), 'w')
    return setup_module

def teardown_module():
    if __log_file__:
        __log_file__.close()

    if __performance_file__:
        __performance_file__.close()

def log(message = '', file=None):
    if not file:
        file = __log_file__
    print >> file, message
    file.flush()

def make_command(args):
    return ' '.join(args)

def t35t_case(name_or_function):
    '''A test case (Spelled in 1337 so it doesn't get picked up by nose).'''
    def decorator(function):
        @wraps(function)
        def wrapped(*args, **kwargs):
            # Figure out the name of the test case
            name = name_or_function
            if name and hasattr(name, '__call__'):
                name = name.__name__
            elif not name:
                name = function.__name__
                
            log('[Test: %s]' % (name))
        
            start = time.time()
            function(*args, **kwargs)
            duration = time.time() - start
            
            log('%s\t%.2f' % (name, duration), __performance_file__)
        return wrapped
    if hasattr(name_or_function, '__call__'):
        return decorator(name_or_function)
    return decorator

def do_check(filename, expected_similarity=1.0):
    expected_path = join(__expected_output_path__, filename)
    if not isfile(expected_path):
        raise Exception
    
    subject_path = join(__output_path__, filename)
    expected = open(expected_path, 'rU').readlines()
    subject = open(subject_path, 'rU').readlines()
    
    matches = 0
    counter = 0
    for index in range(len(expected)):
        if expected[index] == subject[index]:
            matches += 1
        counter += 1
    similarity = float(matches) / counter
    if similarity < expected_similarity:
        log('%s: %.2f < %.2f' % (filename, similarity, expected_similarity))
        assert False
    
def check_output(*files):
    def factory(function):
        @wraps(function)
        def decorator(*args, **kwargs):
            function(*args, **kwargs)
            for filename in files:
                do_check(filename)
        return decorator
    return factory
    
def make_dirs(path):
    try:
        os.makedirs(path)
    except OSError, exc:
        if exc.errno == errno.EEXIST:
            return
        else:
            raise

def run_command(args):
    output = subprocess.Popen(make_command(args), shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    for contents in output:
        log(contents)
    for contents in output:
        assert 'Exception in thread' not in contents

class CommandLineTestBase(object):
    def __init__(self):
        self.config = config
        self.jar_base_path = __jar_base_path__
        self.test_data_path = 'data'
        self.jar_path = find_jar(self.jar_base_path)
        self.output_path = __output_path__
    
        
    def setup(self):
        self.args = [
            'java',
            '-cp', self.jar_path,
            '-Xmx2g',
            '-d64',
        ]
    
    def teardown(self):
        log()

    def get_data(self, name):
        return join(self.test_data_path, name)
