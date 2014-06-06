# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from psutil_producer import PsutilsKafkaProducer
import kafka
import unittest
import threading
import time
import logging

class NullHandler(logging.Handler):
    def emit(self, record):
        pass

class PsutilsKafkaProducerTest(unittest.TestCase):

    def setUp(self):
        self.url = "192.168.86.10:9092"
        self.topic = "psutil-kafka-topic"

        self.producer = PsutilsKafkaProducer(self.url, self.topic, reportInterval=5)
        self.consumer = kafka.SimpleConsumer(kafka.KafkaClient(self.url), "group1", self.topic)

        self.thread = threading.Thread(target=self.producer.start)
        self.thread.daemon = True

    def testStart(self):
        self.thread.start()
        time.sleep(5)
        self.producer.stop()

        message = self.consumer.get_message()
        assert message is not None


if __name__ == '__main__':
    logging.getLogger("kafka").addHandler(NullHandler())
    unittest.main()