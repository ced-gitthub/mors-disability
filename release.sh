#!/bin/sh
# Usage: ./release.sh <alpha|beta|release> <version, e.g. 0.3.0-beta.1+26.2>
set -e
ch="$1"; ver="$2"
case "$ch" in alpha|beta|release) ;; *) echo "channel must be alpha, beta or release"; exit 1;; esac
sed -i "s/^version=.*/version=$ver/" gradle.properties
./gradlew build --console=plain -q
mkdir -p "releases/$ch"
cp "build/libs/mors-disability-$ver.jar" "releases/$ch/"
echo "Saved releases/$ch/mors-disability-$ver.jar"
