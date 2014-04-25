import os
import sys
import getopt
import simplejson
import time

from kafka.client import KafkaClient
from kafka.producer import SimpleProducer, Producer
import kafka.producer
import kafka.client
import psutil


class PsutilsKafkaProducer:
    def __init__(self, url, topic,
                 configLocation=os.path.dirname(os.path.realpath(__file__)) + os.sep + 'config.json',
                 reportInterval=15,
                 async=False,
                 sendInBatch=False,
                 batchSize=kafka.producer.BATCH_SEND_MSG_COUNT,
                 batchTimeout=kafka.producer.BATCH_SEND_DEFAULT_INTERVAL):
        self.topic = topic
        self.configLocation = configLocation
        self.reportInterval = reportInterval

        kafka = KafkaClient(url)
        self.producer = SimpleProducer(kafka, async, Producer.ACK_AFTER_LOCAL_WRITE,
                                       Producer.DEFAULT_ACK_TIMEOUT, sendInBatch, batchSize,
                                       batchTimeout)

    def start(self):
        while True:
            metricsConfiguration = simplejson.load(open(self.configLocation))
            report = {}
            for metric in metricsConfiguration:
                metricName = metric['name']
                if metric['enabled']:
                    del metric['name']
                    del metric['enabled']
                    arguments = metric.values()
                    metricCallable = getattr(psutil, metricName)

                    if type(metricCallable) is None:
                        pass

                    report[metricName] = apply(metricCallable, arguments)

            self.producer.send_messages(self.topic, simplejson.dumps(report))
            time.sleep(self.reportInterval)

    def stop(self):
        self.producer.stop()


class OptionsConfiguration:
    url = ''
    topic = ''
    async = False
    sendInBatch = False
    batchSize = kafka.producer.BATCH_SEND_MSG_COUNT
    batchTimeout = kafka.producer.BATCH_SEND_DEFAULT_INTERVAL
    configLocation = os.path.dirname(os.path.realpath(__file__)) + os.sep + 'config.json'
    reportInterval = 15

    def __init__(self, argv):
        opts, args = getopt.getopt(argv, "",
                                   ["url=", "topic=", "configLocation=", "reportInterval=", "async", "sendInBatch",
                                    "batchSize=", "batchTimeout="])
        for option, arg in opts:
            if option == '--url':
                self.url = arg
            elif option == '--topic':
                self.topic = arg
            elif option == '--async':
                self.async = True
            elif option == '--sendInBatch':
                self.sendInBatch = True
            elif option == '--batchSize':
                self.batchSize = arg
            elif option == '--batchTimeout':
                self.batchTimeout = arg
            elif option == '--configLocation':
                self.configLocation = arg
            elif option == 'reportInterval':
                self.reportInterval = arg


def main(argv):
    try:
        options = OptionsConfiguration(argv)
        producer = PsutilsKafkaProducer(options.url, options.topic,
                                        options.configLocation,
                                        options.reportInterval,
                                        options.async,
                                        options.sendInBatch,
                                        options.batchSize,
                                        options.batchTimeout)
        producer.start()
    except getopt.GetoptError, e:
        raise Exception(e)


if __name__ == "__main__":
    main(sys.argv[1:])