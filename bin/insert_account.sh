#!/bin/bash

set -e

name=${1:-testing}
password=${2:-password}
gm=${3:-0}

echo "INSERT IGNORE INTO accounts (name, password, gm)
VALUES ('${name}', '${password}', ${gm});
" | mysql -h db --password=testing argonms
