#!/bin/sh

JAVA_PATH=$(type -p java)
if [ -z "$JAVA_PATH" ]; then
    sudo apt-get -y update
    sudo apt-get install -y software-properties-common python-software-properties
    sudo add-apt-repository -y ppa:webupd8team/java
    sudo apt-get -y update
    sudo /bin/echo debconf shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
    sudo apt-get -y install oracle-java7-installer oracle-java7-set-default
fi

export START_KAFKA_SCRIPT=https://raw2.github.com/stealthly/docker-kafka/master/start-broker.sh
curl -Ls $START_KAFKA_SCRIPT | bash /dev/stdin 1 9092 localhost

export START_RIEMANN_SCRIPT=https://raw2.github.com/stealthly/docker-riemann/master/start-riemann.sh
curl -Ls $START_RIEMANN_SCRIPT | bash /dev/stdin localhost

sudo docker run --name psutil --link broker1:localhost -d -v $(pwd)/psutil/src/main/python:/psutil stealthly/docker-python python /psutil/psutil_producer.py --topic psutil-kafka-topic --url localhost:9092
sudo docker run -d -v /etc/localtime:/etc/localtime:ro --net=host --name kamon-grafana-dashboard kamon/grafana_graphite