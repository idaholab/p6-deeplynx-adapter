#!/bin/bash

git pull;

# stop the running container
dzdo podman stop $(dzdo podman ps -f name=p6 --format "{{.ID}}");

# remove old container
dzdo podman container rm p6;

# build the new container
dzdo podman build -f Dockerfile-dev -t p6adapter:latest --no-cache;

# run
dzdo podman run -p 8080:80 --name=p6 -v ./sqlite:/var/app/sqlite --env-file=.env p6adapter:latest;
