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
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.To;
import org.axc.ruan.zipkin.example.ConstParam;
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
import brave.kafka.streams.KafkaStreamsTracing;

/**
 * zipkin kafka示例服务实现
 * @author ruan
 * @Date 2019年2月28日下午9:18:22
 *
 */
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

    private KafkaStreamsTracing kafkaStreamsTracing;

    @PostConstruct
    public void init() {
        /**
         * 处理流程：
         * 1. kafka-connector示例通过rest接口调用发送消息到kafka队列SRC_TOPIC_DEF
         * 2. kafka-streams示例监听kafka队列SRC_TOPIC_DEF，处理完发送到kafka队列END_TOPIC_DEF
         * 3. kafka-connector示例监听kafka队列SRC_TOPIC_DEF，处理完结束
         */
        kafkaStreamsTracing = KafkaStreamsTracing.create(tracing);
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

        if (conf.getStreams() != null && !conf.getStreams().isEmpty()) {
            String srcTopic = conf.getStreams().getOrDefault("test.src.topic", ConstParam.SRC_TOPIC_DEF).toString();
            String endTopic = conf.getStreams().getOrDefault("test.end.topic", ConstParam.END_TOPIC_DEF).toString();

            Topology topology = new Topology();
            topology.addSource("SOURCE", srcTopic);
            ProcessorSupplier<String, String> parentProcessor = kafkaStreamsTracing.processor(ConstParam.STREAM_PARENT_PROCESSOR,
                    new StreamProcessorParent());
            topology.addProcessor(ConstParam.STREAM_PARENT_PROCESSOR, parentProcessor, "SOURCE");

            ProcessorSupplier<String, String> endProcessor = kafkaStreamsTracing.processor(ConstParam.STREAM_CHILD_PROCESSOR,
                    new StreamProcessorChild());
            topology.addProcessor(ConstParam.STREAM_CHILD_PROCESSOR, endProcessor, ConstParam.STREAM_PARENT_PROCESSOR);
            topology.addSink("SINK-END", endTopic, ConstParam.STREAM_CHILD_PROCESSOR);

            KafkaStreams kafkaStreams = kafkaStreamsTracing.kafkaStreams(topology, conf.getStreams());

            kafkaStreams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
                throwable.printStackTrace();
                System.out.println(String.format(
                        "kafka stream thread exit uncaught exceptionhandler: thread name= %s, exception= %s",
                        thread.getName(), throwable.getMessage()));
            });

            kafkaStreams.cleanUp();
            kafkaStreams.start();
            Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        }
    }

    @Override
    public Producer<String, String> getTracingProducer() {
        return tracingProducer;
    }

    /**
     * 启动kafka-connector示例监听kafka队列SRC_TOPIC_DEF
     * @Title: startTracingConsumer
     * @return: void
     */
    private void startTracingConsumer() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                String endTopic = conf.getConsumer().getOrDefault("test.end.topic", ConstParam.END_TOPIC_DEF)
                        .toString();
                tracingConsumer.subscribe(Arrays.asList(endTopic));
                while (true) {
                    ConsumerRecords<String, String> records = tracingConsumer.poll(Duration.ofSeconds(10));
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println(String.format("startTracingConsumer recevice message: topic = %s, messages = %s",
                                        endTopic, record.value()));
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

    /**
     * kafka-streams示例监听kafka队列SRC_TOPIC_DEF，处理完流传到StreamProcessorChild
     * @author ruan
     * @Date 2019年2月28日下午9:16:12
     *
     */
    private class StreamProcessorParent implements Processor<String, String> {
        private ProcessorContext context;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
        }

        @Override
        public void process(String key, String value) {
            try {
                Span currentSpan = tracing.tracer().currentSpan();
                currentSpan.annotate("start");
                String data = value;
                System.out.println(String.format("StreamProcessorParent recevice message: topic = %s, messages = %s",
                        context.topic(), data));
                data = String.format("%s=>%s", data, ConstParam.STREAM_CHILD_PROCESSOR);
                context.forward(key, data, To.child(ConstParam.STREAM_CHILD_PROCESSOR));
                System.out.println(String.format(
                        "StreamProcessorParent forward message: next processor = %s, messages = %s", ConstParam.STREAM_CHILD_PROCESSOR, data));
                currentSpan.annotate("finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
            context.commit();
        }

        @Override
        public void close() {

        }

    }

    /**
     * kafka-streams接收StreamProcessorParent的输出，处理完发送到kafka队列END_TOPIC_DEF
     * @author ruan
     * @Date 2019年2月28日下午9:17:15
     *
     */
    private class StreamProcessorChild implements Processor<String, String> {
        private ProcessorContext context;
        private String endTopic;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            endTopic = conf.getStreams().getOrDefault("test.end.topic", ConstParam.END_TOPIC_DEF).toString();
        }

        @Override
        public void process(String key, String value) {
            try {
                Span currentSpan = tracing.tracer().currentSpan();
                currentSpan.annotate("start");
                String data = value;
                System.out.println(String.format("StreamProcessorChild recevice message: topic= %s, messages=%s",
                        context.topic(), data));
                data = String.format("%s=>%s", data, endTopic);
                zipkinTestUtil.randomSleep(2);
                context.forward(key, data, To.child("SINK-END"));
                System.out.println(String.format("StreamProcessorChild forward message: next topic= %s, messages=%s",
                        endTopic, data));
                currentSpan.annotate("finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
            context.commit();
        }

        @Override
        public void close() {

        }

    }

}
