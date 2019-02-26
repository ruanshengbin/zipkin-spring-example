package org.axc.ruan.zipkin.example.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaService {

    Producer<String, String> getTracingProducer();

    Consumer<String, String> getTracingConsumer();

}
