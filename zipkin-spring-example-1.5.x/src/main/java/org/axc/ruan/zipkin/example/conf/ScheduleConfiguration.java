package org.axc.ruan.zipkin.example.conf;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class ScheduleConfiguration implements SchedulingConfigurer {

    @Value("${schedule.job.concurrency:6}")
    private int concurrency;

    @Autowired
    private Tracer tracer;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private TraceKeys traceKeys;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(concurrency);
        return new TraceableScheduledExecutorService(threadPool, tracer, traceKeys, new SpanNamer() {
            
            @Override
            public String name(Object object, String defaultValue) {
                return String.format("%s-schedule-example", appName);
            }
        });
    }

}
