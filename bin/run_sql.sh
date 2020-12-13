#!/bin/bash
# Run sql against the database specified by environment variables
# Example:
#   echo "select name from accounts" | bin/run_sql.sh

set -e

user=${MYSQL_USER?"db user is not set in the environment"}
password=${MYSQL_PASSWORD?"db password is not set in the environment"}
database=${MYSQL_DATABASE?"db name is not set in the environment"}

cat - | mysql --host db --user=${user} --password=${password} ${database}
