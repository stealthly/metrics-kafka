Meta:

Narrative:
As a user
I want to send metrics via Reporter to Kafka topic
So that I can read this data via kafka consumer

Scenario: Metrics reporter sends data to kafka topic and kafka consumer should be able to read it.
Given Kafka broker is up and 'metrics' topic is created.
When KafkaReporter sends data to Kafka topic.
Then Kafka consumer should be able to read this data.