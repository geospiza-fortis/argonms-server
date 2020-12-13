#!/bin/bash

set -e

script_dir=$(dirname $0)
name=${1?"name must be specified"}
password=${2?"password must be specified"}
gm=${3:-0}

echo "INSERT IGNORE INTO accounts (name, password, gm)
VALUES ('${name}', '${password}', ${gm});
" | ${script_dir}/run_sql.sh
