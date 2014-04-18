Kafka CodaHale Metrics Reporter + Riemann metrics consumer
=============
The goals of this repo is to have an end to end working environment to provide the ability for systems (applications
and infrastructure) to send their metrics/sensor data to Kafka and then to report on that data for both alerts and charts.

More about this project in this blog post [http://allthingshadoop.com/2014/04/18/metrics-kafka/](http://allthingshadoop.com/2014/04/18/metrics-kafka/).

Quick up and running
====================

Use Vagrant to get up and running.

1) Install Vagrant [http://www.vagrantup.com/](http://www.vagrantup.com/)    
2) Install Virtual Box [https://www.virtualbox.org/](https://www.virtualbox.org/)

In the main metrics-kafka folder

1) vagrant up    
2) ./gradlew test

once this is done
* Zookeeper will be running 192.168.86.5
* Broker 1 on 192.168.86.10
* Riemann on 192.168.86.55
* All the tests in metrics/src/test/java/* and riemann/src/test/scala/* should pass

If you want you can login to the machines using vagrant ssh <machineName> but you don't need to.

You can access the brokers and zookeeper by their IP from your local without having to go into vm.

e.g.

bin/kafka-console-producer.sh --broker-list 192.168.86.10:9092 --topic <get his from the random topic created in test>

bin/kafka-console-consumer.sh --zookeeper 192.168.86.5:2181 --topic <get his from the random topic created in test> --from-beginning