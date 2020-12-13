#!/bin/bash

set -e

user=${MYSQL_USER?"db user is not set in the environment"}
password=${MYSQL_PASSWORD?"db password is not set in the environment"}
database=${MYSQL_DATABASE?"db name is not set in the environment"}

name=${1?"name must be specified"}
password=${2?"password must be specified"}
gm=${3:-0}

echo "INSERT IGNORE INTO accounts (name, password, gm)
VALUES ('${name}', '${password}', ${gm});
" | mysql --host db --user=${user} --password=${password} ${database}
