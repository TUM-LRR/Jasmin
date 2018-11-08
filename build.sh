#! /bin/bash

set -e

mkdir -p build

pushd src/
javac -d ../build/ -sourcepath ./ jasmin/Main.java
javac -d ../build/ -sourcepath ./ jasmin/commands/*.java
javac -d ../build/ -sourcepath ./ jasmin/gui/*.java
popd

pushd build/
cp -r ../src/jasmin/gui/resources ./jasmin/gui/
cp -r ../help ./

jar cfe ../jasmin-dev.jar jasmin.Main ./
popd

