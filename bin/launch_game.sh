#!/bin/bash

##
## ArgonMS MapleStory server emulator written in Java
## Copyright (C) 2011-2013  GoldenKevin
##
## This program is free software: you can redistribute it and/or modify
## it under the terms of the GNU Affero General Public License as
## published by the Free Software Foundation, either version 3 of the
## License, or (at your option) any later version.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU Affero General Public License for more details.
##
## You should have received a copy of the GNU Affero General Public License
## along with this program.  If not, see <http://www.gnu.org/licenses/>.
##

set -e

cd "$(dirname "${BASH_SOURCE[0]}")"/..
prefix="conf/testing"
data_dir=${DATA_DIR:-wz/}
script_dir=${SCRIPT_DIR:-scripts/example/}

export MAVEN_OPTS="-Xmx600m"
mvn exec:java -Dexec.mainClass="argonms.game.GameServer" \
    -Dargonms.game.serverid=0 \
    -Dargonms.game.config.file=$prefix/game.properties \
    -Djava.util.logging.config.file=$prefix/logging.properties \
    -Dargonms.db.config.file=$prefix/db.properties \
    -Dargonms.ct.macbanblacklist.file=$prefix/macbanblacklist.txt \
    -Dargonms.data.dir=$data_dir \
    -Dargonms.scripts.dir=$script_dir
