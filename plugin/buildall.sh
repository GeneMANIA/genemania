#!/bin/bash

pushd ..
for PROJECT in common engine
do
    pushd ${PROJECT}
    mvn clean install || exit
    popd
done
popd

mvn clean install || exit
