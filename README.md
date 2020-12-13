# argonms

This is a fork of argonms.

## quickstart

Obtain a localhost v62 client and modify `127.0.0.1` to redirect to `localhost`
with a hex editor.

Before starting the server, copy `.env.template` to `.env` and edit the values.

```bash
DATA_DIR="/path/to/kvj"
```

Alternatively, export these variables to the current environment. Then run the
servers with the default testing settings.

```bash
docker-compose up
```

To insert a new account:

```bash
docker-compose run --rm center bin/insert_account.sh <NAME> <PASSWORD>
```

After exiting docker-compose, remove any dangling services:

```bash
docker-compose down
```

The data for the server is persisted inside of a
[volume](https://docs.docker.com/storage/volumes/). To inspect the contents:

```bash
docker inspect argonms-server_argonms-volume
```

To delete all persisted data:

```bash
docker-compose down -v
```
