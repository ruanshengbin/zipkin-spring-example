package org.axc.ruan.zipkin.example.service.impl;

import java.time.Duration;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.axc.ruan.zipkin.example.config.KakfaConfiguration;
import org.axc.ruan.zipkin.example.service.KafkaService;
import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.kafka.clients.KafkaTracing;

@Component
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    private KakfaConfiguration conf;

    @Autowired
    private Tracing tracing;

    @Autowired
    private ZipkinTestUtil zipkinTestUtil;

    @Value("${spring.zipkin.service.name:${spring.application.name:default}}")
    private String serviceName;

    private KafkaTracing kafkaTracing;

    private Producer<String, String> tracingProducer;

    private Consumer<String, String> tracingConsumer;

    @PostConstruct
    public void init() {
        kafkaTracing = KafkaTracing.newBuilder(tracing).writeB3SingleFormat(true).remoteServiceName(serviceName)
                .build();

        if (conf.getProducer() != null && !conf.getProducer().isEmpty()) {
            KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(conf.getProducer());
            tracingProducer = kafkaTracing.producer(kafkaProducer);
        }

        if (conf.getConsumer() != null && !conf.getConsumer().isEmpty()) {
            KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(conf.getConsumer());
            tracingConsumer = kafkaTracing.consumer(kafkaConsumer);
            startTracingConsumer();
        }
    }

    @Override
    public Producer<String, String> getTracingProducer() {
        return tracingProducer;
    }

    @Override
    public Consumer<String, String> getTracingConsumer() {
        return tracingConsumer;
    }

    public void startTracingConsumer() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                tracingConsumer.subscribe(
                        Arrays.asList(conf.getConsumer().getOrDefault("test.topic", "zipkin-test").toString()));
                while (true) {
                    ConsumerRecords<String, String> records = tracingConsumer.poll(Duration.ofSeconds(10));
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println("收到消息：" + record.value());
                        Span span = kafkaTracing.nextSpan(record).name("consumer-process-message").start();
                        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
                            tracing.tracer().currentSpan().annotate("kafka consumer start");
                            zipkinTestUtil.randomSleep(2);
                            tracing.tracer().currentSpan().annotate("kafka consumer finish");
                        } catch (Exception e) {
                            span.error(e);
                        } finally {
                            span.finish();
                            tracingConsumer.commitSync();
                        }
                    }
                }
            }
        }, "test-kafka-consumer");
        t.start();
    }

}
