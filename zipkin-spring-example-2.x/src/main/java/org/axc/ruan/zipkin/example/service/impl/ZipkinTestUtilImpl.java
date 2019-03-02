package org.axc.ruan.zipkin.example.service.impl;

import java.util.Random;

import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import brave.Tracer;

@Service
public class ZipkinTestUtilImpl implements ZipkinTestUtil {

    private static final int SEED = 3000;

    private Random random = new Random();

    @Autowired
    private Tracer tracer;

    @Override
    @NewSpan// 注解的方式使用span，需要放到spring bean容器托管
    public void randomSleepWithSpan(int maxSecond) {
        try {
            tracer.currentSpan().annotate("random sleep start");
            tracer.currentSpan().tag("sleep_second", "" + maxSecond);
            randomSleepNoSpan(maxSecond);
        } catch (Exception e) {
            //
        } finally {
            tracer.currentSpan().annotate("random sleep finish");
        }
    }

    @Override
    public void randomSleepNoSpan(int maxSecond) throws InterruptedException {
        int millis = this.random.nextInt(maxSecond * 1000);
        Thread.sleep(millis);
    }

    @Override
    @NewSpan
    public void randomThrowExceptionWithSpan(float percentage, String desc) {
        try {
            tracer.currentSpan().annotate("random throw exception start");
            int n = new Random().nextInt(SEED);
            tracer.currentSpan().tag("throw_exception", "sample exception message");
            if (n < SEED * percentage) {
                throw new RuntimeException(String.format("%s: %s", desc, "sample exception message"));
            }
        } finally {
            tracer.currentSpan().annotate("random throw exception finish");
        }
    }

    @Override
    @Scheduled(fixedRateString = "60000") // 60 second
    public void scheduleExample() {
        try {
            int maxSecond = 1;
            tracer.currentSpan().annotate("schedule start");
            tracer.currentSpan().tag("sleep_second", "" + maxSecond);
            int millis = this.random.nextInt(maxSecond * 1000);
            Thread.sleep(millis);
        } catch (Exception e) {
            //
        } finally {
            tracer.currentSpan().annotate("schedule finish");
        }
    }

}
