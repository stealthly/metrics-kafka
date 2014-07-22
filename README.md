Metrics Kafka
=============
The goals of this repo is to have an end to end working environment to provide the ability for systems (applications
and infrastructure) to send their metrics/sensor data to Kafka and then to report on that data for both alerts and charts.

More about this project in this blog post [http://allthingshadoop.com/2014/04/18/metrics-kafka/](http://allthingshadoop.com/2014/04/18/metrics-kafka/).

Quick up and running
====================

Use Vagrant to get up and running.

1) Install Docker [http://docs.docker.com/installation/](http://docs.docker.com/installation/)    
2) Install Virtual Box [https://www.virtualbox.org/](https://www.virtualbox.org/)

In the main metrics-kafka folder

    vagrant up --provider=virtualbox
    vagrant ssh
    cd /vagrant 
    sudo ./bootstrap.sh 
    ./gradlew test
    ./gradlew :psutil:installDependencies
    ./gradlew :metrics-test:run
    python psutil/src/main/python/psutil_producer.py --url=localhost:9092 --topic=psutil-kafka-topic --reportInterval=3


Kafka Yammer Metrics reporting
==============================

In order to assemble jar for metrics yammer do the following steps:    
1) ./gradlew :yammer:jar    
2) put the jar from metrics-yammer/build/libs to libs dir in root kafka folder    
3) add "kafka.metrics.reporters=ly.stealth.kafka.metrics.KafkaBrokerReporter" to config/server.properties in kafka root folder    