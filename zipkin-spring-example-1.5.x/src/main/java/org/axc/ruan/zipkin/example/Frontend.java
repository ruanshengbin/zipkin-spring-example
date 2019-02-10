package org.axc.ruan.zipkin.example;

import javax.annotation.Resource;

import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/zipkin")
@CrossOrigin // So that javascript can be hosted elsewhere
public class Frontend {

    @Resource(name = "rest.template.nobalance")
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ZipkinTestUtil zipkinTestUtil;

    @Value("${backend.url}")
    private String backendUrl;

    @GetMapping("/front/{name}")
    public String callBackend(@PathVariable("name") final String name) {
        frontCustomNewSpan();
        return restTemplate.getForObject(String.format("%s/zipkin/backend/%s" , backendUrl, name), String.class);
    }

    private void frontCustomNewSpan() {
        Span newSpan = tracer.createSpan("frontCustomNewSpan");
        try {
            newSpan.logEvent("frontCustomNewSpan-start");
            newSpan.tag("frontCustomNewSpan-test", "test");
            zipkinTestUtil.randomSleep(3);
            zipkinTestUtil.randomThrowException(0.4F);
        } finally {
            newSpan.logEvent("frontCustomNewSpan-finish");
            tracer.close(newSpan);
        }
    }

}
