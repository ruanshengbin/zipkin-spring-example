package org.axc.ruan.zipkin.example;

import java.util.Date;

import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zipkin")
public class Backend {

    @Autowired
    private ZipkinTestUtil zipkinTestUtil;

    @Autowired
    private Tracer tracer;

    @RequestMapping("/backend/{name}")
    public String printDate(@PathVariable("name") final String name) {
        backendCustomContinueSpan();
        return String.format("hi %s: %s", name, new Date().toString());
    }

    private void backendCustomContinueSpan() {
        Span continuedSpan = tracer.continueSpan(tracer.getCurrentSpan());
        try {
            continuedSpan.logEvent("backendCustomContinueSpan-start");
            continuedSpan.tag("backendCustomContinueSpan-test", "test");
            zipkinTestUtil.randomSleep(3);
        } finally {
            continuedSpan.logEvent("backendCustomContinueSpan-finish");
            tracer.detach(continuedSpan);
        }
    }

}
