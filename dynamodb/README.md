## What is dynamodb

Dynamodb  is a nosql database that is used in AWS, there's some benefits like how they nicely managed the instance for you etc.

## How to run it

Amazon provide a [docker image](https://hub.docker.com/r/amazon/dynamodb-local), which you can run like

```bash
docker run --name dynamo --rm -it -p 8000:8000 amazon/dynamodb-local
```

One nice thing about their setup is you can interact with the db via the aws cli as though it were a normal AWS managed instance.

When using the client, to satisfy some arbitrary access key constraints you can set AWS_ACCESS_KEY_ID=fake;AWS_SECRET_ACCESS_KEY=fake as env variables.

## Some funky stuff in this repo
This repo was actually made to investigate behaviour around issues around initially acquiring connection. As it turns out, delaying this isn't easy in a high level language, so I made delay_client/delay_connect.c, if compiled with

```bash
gcc -shared -fPIC -o delay_connect.so delay_connect.c -ldl
```

Then when you run the server, you can do LD_PRELOAD=/path/to/dot-so-file and it will have outbound connection attempts delayed by 3s.

Delaying latency of actual subsequent requests is of course much simpler, e.g. just have a proxy server (this is what intercepting-server is setup to be) and have it delay requests by some amount before sending upstream.