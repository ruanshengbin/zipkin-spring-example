package org.axc.ruan.zipkin.example.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.axc.ruan.zipkin.example.service.ZipkinTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import brave.Span;
import brave.Tracer;
import brave.Tracing;

/**
 * test spring webmvc and jdbc
 * @author ruan
 * @Date 2019年2月25日上午11:46:59
 *
 */
@RestController
@RequestMapping("/zipkin")
public class RestJdbcController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ZipkinTestUtil zipkinTestUtil;

    @Autowired
    private Tracing tracing;

    @GetMapping("/jdbc/{name}")
    public String test(@PathVariable("name") final String name) throws Exception {
        tracing.tracer().currentSpan().tag("tracker_id", name);
        zipkinTestUtil.randomThrowExceptionWithSpan(0.3F, "RestJdbcController");

        String sql = "SELECT Host, User FROM user";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        zipkinNewSpan();

        return String.format("hi %s: %s!\r\n mysql return：%s", name, new Date().toString(), JSON.toJSONString(list));
    }

    private void zipkinNewSpan() throws Exception {
        Span span = tracing.tracer().nextSpan().name("zipkinNewSpan").start();
        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
            span.annotate("zipkinNewSpan start");
            zipkinContinueSpan();
            span.annotate("zipkinNewSpan finish");
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    private void zipkinContinueSpan() throws Exception {
        Span span = tracing.tracer().joinSpan(tracing.currentTraceContext().get());
        try {
            span.annotate("zipkinContinueSpan start");
            zipkinTestUtil.randomSleepNoSpan(1);
            span.annotate("zipkinContinueSpan finish");
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

}
