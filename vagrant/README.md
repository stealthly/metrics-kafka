# Apache Kafka #

Using Vagrant to get up and running.

1) Install Vagrant [http://www.vagrantup.com/](http://www.vagrantup.com/)  
2) Install Virtual Box [https://www.virtualbox.org/](https://www.virtualbox.org/)
3) gradle :psutil:installDependencies to install psutil dependencies

In the main kafka folder

1) vagrant up

once this is done 
* Zookeeper will be running 192.168.50.5
* Broker 1 on 192.168.50.10
* Broker 2 on 192.168.50.20
* Broker 3 on 192.168.50.30

When you are all up and running you will be back at a command brompt.  

If you want you can login to the machines using vagrant ssh <machineName> but you don't need to.

You can access the brokers and zookeeper by their IP

e.g.

bin/kafka-console-producer.sh --broker-list 192.168.50.10:9092,192.168.50.20:9092,192.168.50.30:9092 --topic sandbox

bin/kafka-console-consumer.sh --zookeeper 192.168.50.5:2181 --topic sandbox --from-beginning
