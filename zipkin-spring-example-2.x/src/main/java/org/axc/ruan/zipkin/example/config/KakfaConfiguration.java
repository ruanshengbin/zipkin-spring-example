package org.axc.ruan.zipkin.example.config;

import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("kafka")
public class KakfaConfiguration {

    private Map<String, Object> producer;

    private Properties streams;

    private Map<String, Object> consumer;

    public Map<String, Object> getProducer() {
        return producer;
    }

    public void setProducer(Map<String, Object> producer) {
        this.producer = producer;
    }

    public Properties getStreams() {
        return streams;
    }

    public void setStreams(Properties streams) {
        this.streams = streams;
    }

    public Map<String, Object> getConsumer() {
        return consumer;
    }

    public void setConsumer(Map<String, Object> consumer) {
        this.consumer = consumer;
    }

}
