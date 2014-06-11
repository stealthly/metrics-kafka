#!/bin/sh
docker kill riemann
docker kill zkserver
docker kill broker1

docker rm riemann
docker rm zkserver
docker rm broker1
