#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

# This might look a bit strange, but it saves space on the device by not having
# to install Maven on there. Maven also errors out on the device every time
# dependencies are being fetched, so build locally instead and then upload the artifact.

# Usage: deploy.sh <Resin.io Git remote URL>
build_dir="/tmp/coffee-panic-hardware-build-$$/"
trap "rm -rf \"$build_dir\"" EXIT
echo "Building in $build_dir"

version="1.0-SNAPSHOT"
mvn package

mkdir -p "$build_dir"
cp "target/m10-scale-${version}-jar-with-dependencies.jar" "${build_dir}/"

# Generate the Dockerfile here to avoid confusion - we should never
# build directly from the source directory, so the file should not
# exist there.
cat <<EOF > "$build_dir/Dockerfile"
FROM resin/raspberrypi-openjdk:8-jre

COPY . /app

WORKDIR /app
CMD [ "java", "-jar", "./m10-scale-${version}-jar-with-dependencies.jar" ]
EOF

cd "$build_dir"
git init
git add .
git commit --message="Deploying"
git remote add resin $@
git push --force resin master # force since it is a new repository
