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