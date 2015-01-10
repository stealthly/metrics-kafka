#!/bin/sh
docker kill psutil
docker kill riemann
docker kill zkserver
docker kill broker1

docker rm psutil
docker rm riemann
docker rm broker1
docker rm zkserver
