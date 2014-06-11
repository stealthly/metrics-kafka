Kafka CodaHale Metrics Reporter + Riemann metrics consumer
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

1) sudo ./bootstrap.sh
2) ./gradlew test
3) sudo ./shutdown.sh

once this is done
* Zookeeper will be running localhost:2181
* Broker 1 on localhost:9092
* Riemann on localhost:5555
* Riemann dash on localhost:4567
* All the tests in metrics/src/test/java/* and riemann/src/test/scala/* should pass

You can access the brokers, zookeeper, riemann and riemann-dash by their IP from your local without having to go into vm.

e.g.

bin/kafka-console-producer.sh --broker-list localhost:9092 --topic <get his from the random topic created in test>

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic <get his from the random topic created in test> --from-beginning