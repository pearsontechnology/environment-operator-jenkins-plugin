#!/bin/bash

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
prefix=$(cat ${script_dir}/../version.txt)

latest_tag=$(git ls-remote --tags origin | cut -f 3 -d '/' | grep "^${prefix}" | sort -t. -k 3,3nr | head -1)

if [ -z ${latest_tag} ]; then
  tag="${prefix}.0"
else
  tag="${latest_tag%.*}.$((${latest_tag##*.}+1))"
fi

git tag ${tag}
git push --tags