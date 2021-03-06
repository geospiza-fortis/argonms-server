# argonms

This is a fork of argonms.

## quickstart (localhost)

Obtain a localhost v62 client and modify `127.0.0.1` to redirect to `localhost`
with a hex editor.

Use [Harepacker](https://github.com/lastbattle/Harepacker-resurrected) to dump
XML from the wz files in the client installation directory. Use
[argon-data](https://github.com/geospiza-fortis/argonms-data/tree/docker) to
create the server KVJ files.

Before starting the server, copy `.env.template` to `.env` and edit the values.

```bash
DATA_DIR="/path/to/kvj/"
```

Alternatively, export these variables to the current environment. Then run the
servers with the default testing settings.

```bash
docker-compose up
```

To insert a new account:

```bash
docker-compose run --rm center bin/insert_account.sh <NAME> <PASSWORD> <GM_LEVEL>
```

To make a GM account, set `GM_LEVEL > 100` e.g. 200. To make a normal account,
set it to 0 or omit it from the command. See
[`argonms.common.UserPriviledges`](https://github.com/geospiza-fortis/argonms-server/blob/master/src/argonms/common/UserPrivileges.java)
for more details.

[`adminer`](https://www.adminer.org/) is shipped by default on port 8080. This
can be used to modify the database by hand. To run this when the server is down:

```bash
docker-compose run --rm  --service-ports adminer
```

After exiting docker-compose, remove any dangling services:

```bash
docker-compose down
```

The data for the server is persisted inside of a
[volume](https://docs.docker.com/storage/volumes/). To inspect the contents:

```bash
docker volume inspect argonms-server_argonms-volume
```

To delete all persisted data:

```bash
docker-compose down -v
```

## hosting configuration

Set `ARGONMS_HOST` and modify the localhost client to your public IP
address. Forward the appropriate ports from the docker-compose file (8383 for
the center server, 8484 for the login server, and 7575 for the game server).

If you are using Docker for Windows using WSL, you should use the IPv4 address
of the WSL interface (`Ethernet adapter vEthernet (WSL)`). Use `ipconfig` to
find the interface.

## debugging

### `/usr/bin/env: ‘bash\r’: No such file or directory`

Window line endings were introduced when saving these files. To fix this, run
dos2unix.

```bash
# launch any container where the bin/ directory is mounted
docker-compose run --rm center bash

apt install dos2unix
dos2unix bin/*

# bring down the containers
docker-compose down
```
