package org.axc.ruan.zipkin.example.service;

import org.apache.kafka.clients.producer.Producer;

/**
 * zipkin kafka示例服务
 * @author ruan
 * @Date 2019年2月28日下午9:10:17
 *
 */
public interface KafkaService {

    /**
     * 获取有服务跟踪功能的Producer
     * @Title: getTracingProducer
     * @Description: TODO
     * @return
     * @return: Producer<String,String>
     */
    Producer<String, String> getTracingProducer();

}
