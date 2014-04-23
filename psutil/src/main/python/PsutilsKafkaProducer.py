import sys
import getopt
import simplejson
import time

from kafka.client import KafkaClient
from kafka.producer import SimpleProducer, Producer
import kafka.producer
import kafka.client
import psutil


def main(argv):
    try:
        settings = OptionsConfiguration(argv)
        kafka = KafkaClient(settings.url)
        producer = SimpleProducer(kafka,
                                  settings.async,
                                  Producer.ACK_AFTER_LOCAL_WRITE,
                                  Producer.DEFAULT_ACK_TIMEOUT,
                                  settings.sendInBatch,
                                  settings.batchSize,
                                  settings.batchTimeout)
        while True:
            metricsConfiguration = simplejson.load(open(settings.configLocation))
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

            producer.send_messages(settings.topic, simplejson.dumps(report))
            time.sleep(settings.reportInterval)
    except getopt.GetoptError:
        print "usage: --url <url> --topic <topic> [--configLocation <path>] [--reportInterval <seconds>] [--async <true|false>] [--sendInBatch <true|false>] [--batchSize <numeric>] [--batchTimeout <numeric>]"
        sys.exit(2)


class OptionsConfiguration:
    url = ''
    topic = ''
    async = False
    sendInBatch = False
    batchSize = kafka.producer.BATCH_SEND_MSG_COUNT
    batchTimeout = kafka.producer.BATCH_SEND_DEFAULT_INTERVAL
    configLocation = 'config.json'
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

if __name__ == "__main__":
    main(sys.argv[1:])