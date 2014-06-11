#!/bin/sh
export START_KAFKA_SCRIPT=https://raw2.github.com/stealthly/docker-kafka/master/start-broker.sh
curl -Ls $START_KAFKA_SCRIPT | bash /dev/stdin 1 9092 localhost

export START_RIEMANN_SCRIPT=https://raw2.github.com/stealthly/docker-riemann/master/start-riemann.sh
curl -Ls $START_RIEMANN_SCRIPT | bash /dev/stdin localhost