#! /bin/bash
path="$(realpath "$(dirname "$0")")"
pushd .
cd ..
cd /dynamodb

for dir in $(ls -d */); do
  echo "directories" $dir
  dir=${dir%%/}
  awslocal dynamodb create-table --cli-input-json file:///dynamodb/"$dir"/schema.json

  for data in $(ls $dir -I schema*); do
    awslocal dynamodb batch-write-item --request-items file:///dynamodb/"$dir"/"$data"
    python "$path"/print-entries.py /dynamodb/"$dir"/"$data"
  done
done
popd
