#!/bin/bash

set -e

script_dir=$(dirname $0)
name=${1?"name must be specified"}
password=${2?"password must be specified"}
gm=${3:-0}

# Properly salt passwords. It's also possible to insert passwords in plaintext
# by setting the salt column to null, but it's good hygiene to hash passwords.

# generate a random salt: https://gist.github.com/earthgecko/3089509
salt=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 16 | head -n 1)

# hash the password using sha512
hashed_password=$(python -c "from hashlib import sha512; \
    print(sha512('${password}${salt}').hexdigest())")

echo "INSERT IGNORE INTO accounts (name, password, salt, gm)
VALUES ('${name}', UNHEX('${hashed_password}'), '${salt}', ${gm});
" | ${script_dir}/run_sql.sh
