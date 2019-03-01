package org.axc.ruan.zipkin.example.controller;

import java.util.Date;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.axc.ruan.zipkin.example.ConstParam;
import org.axc.ruan.zipkin.example.config.KakfaConfiguration;
import org.axc.ruan.zipkin.example.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import brave.Tracing;

/**
 * test spring webmvc and kafka connect and streams
 * @author ruan
 * @Date 2019年2月26日下午4:53:42
 *
 */
@RestController
@RequestMapping("/zipkin")
public class RestKafkaController {

    @Autowired
    private KafkaService kafkaSrv;

    @Autowired
    private KakfaConfiguration conf;

    @Autowired
    private Tracing tracing;

    @GetMapping("/kafka/{name}")
    public String test(@PathVariable("name") final String name) {
        // 添加自定义tag，一边根据tag检索所有相关服务跟踪信息
        tracing.tracer().currentSpan().tag("tracker_id", name);
        String sendTopic = conf.getProducer().getOrDefault("test.src.topic", ConstParam.SRC_TOPIC_DEF).toString();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(sendTopic, name);
        kafkaSrv.getTracingProducer().send(producerRecord, new Callback() {

            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println(String.format("RestKafkaConnectController send message to kafka: topic= %s, messages=%s", sendTopic, name));;
            }
        });

        return String.format("hi %s: %s!\r\n send message to kafka: topic= %s, messages=%s", name,
                new Date().toString(), sendTopic, name);
    }

}
