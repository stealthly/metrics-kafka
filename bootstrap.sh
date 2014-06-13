#!/bin/sh

apt-get -y update
apt-get install -y software-properties-common python-software-properties
add-apt-repository -y ppa:webupd8team/java
apt-get -y update
/bin/echo debconf shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
apt-get -y install oracle-java7-installer oracle-java7-set-default

export START_KAFKA_SCRIPT=https://raw2.github.com/stealthly/docker-kafka/master/start-broker.sh
curl -Ls $START_KAFKA_SCRIPT | bash /dev/stdin 1 9092 localhost

export START_RIEMANN_SCRIPT=https://raw2.github.com/stealthly/docker-riemann/master/start-riemann.sh
curl -Ls $START_RIEMANN_SCRIPT | bash /dev/stdin localhost
