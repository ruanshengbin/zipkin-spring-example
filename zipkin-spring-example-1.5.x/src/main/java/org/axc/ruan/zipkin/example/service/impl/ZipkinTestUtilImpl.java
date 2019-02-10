package org.axc.ruan.zipkin.example.service.impl;

import java.util.Random;

import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ZipkinTestUtilImpl implements ZipkinTestUtil {

    private static final int SEED = 3000;

    private Random random = new Random();

    @Autowired
    private Tracer tracer;

    @Override
    @NewSpan// 注解的方式使用span，需要放到spring bean容器托管
    public void randomSleep(int maxSecond) {
        try {
            tracer.getCurrentSpan().logEvent("start");
            tracer.addTag("sleep_second", "" + maxSecond);
            int millis = this.random.nextInt(maxSecond * 1000);
            Thread.sleep(millis);
        } catch (Exception e) {
            //
        } finally {
            tracer.getCurrentSpan().logEvent("finish");
        }
    }

    @Override
    @NewSpan
    public void randomThrowException(float percentage) {
        try {
            tracer.getCurrentSpan().logEvent("start");
            int n = new Random().nextInt(SEED);
            tracer.addTag("throw_exception", "sample exception message");
            if (n < SEED * percentage) {
                throw new RuntimeException("sample exception message");
            }
        } finally {
            tracer.getCurrentSpan().logEvent("finish");
        }
    }

    @Override
    @Scheduled(fixedRateString = "60000") // 60 second
    public void scheduleExample() {
        try {
            int maxSecond = 1;
            tracer.getCurrentSpan().logEvent("start");
            tracer.addTag("sleep_second", "" + maxSecond);
            int millis = this.random.nextInt(maxSecond * 1000);
            Thread.sleep(millis);
        } catch (Exception e) {
            //
        } finally {
            tracer.getCurrentSpan().logEvent("finish");
        }
    }

}
