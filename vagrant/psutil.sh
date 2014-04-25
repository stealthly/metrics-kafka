#!/bin/sh -Eux

#  Trap non-normal exit signals: 1/HUP, 2/INT, 3/QUIT, 15/TERM, ERR
trap founderror 1 2 3 15 ERR

founderror()
{
        exit 1
}

exitscript()
{
        #remove lock file
        #rm $lockfile
        exit 0
}

apt-get install -y python-dev
apt-get install -y python-pip
apt-get install -y git

pip install psutil
pip install simplejson

git clone https://github.com/mumrah/kafka-python
pip install ./kafka-python

exitscript