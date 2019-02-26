package org.axc.ruan.zipkin.example.controller;

import java.util.Date;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.axc.ruan.zipkin.example.config.KakfaConfiguration;
import org.axc.ruan.zipkin.example.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import brave.Tracing;

/**
 * test spring webmvc and kafka connect
 * @author ruan
 * @Date 2019年2月26日下午4:53:42
 *
 */
@RestController
@RequestMapping("/zipkin")
public class RestKafkaConnectController {

    @Autowired
    private KafkaService kafkaSrv;

    @Autowired
    private KakfaConfiguration conf;

    @Autowired
    private Tracing tracing;

    @GetMapping("/kafka/{name}")
    public String test(@PathVariable("name") final String name) {
        tracing.tracer().currentSpan().tag("tracker_id", name);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(
                conf.getProducer().getOrDefault("test.topic", "zipkin-test").toString(), name);
        kafkaSrv.getTracingProducer().send(producerRecord, new Callback() {

            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println("kafka发送成功：" + name);
            }
        });

        return String.format("hi %s: %s, send kafka message ：%s", name, new Date().toString(), name);
    }

}
