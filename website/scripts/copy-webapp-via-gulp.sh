#!/bin/bash

NPM=`which npm` || /usr/local/bin/npm
GULP=`which gulp` || /usr/local/bin/gulp

$NPM install
$GULP website
