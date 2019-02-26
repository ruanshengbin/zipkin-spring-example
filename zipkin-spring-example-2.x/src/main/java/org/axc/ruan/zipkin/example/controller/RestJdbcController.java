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
    public String test(@PathVariable("name") final String name) {
        tracing.tracer().currentSpan().tag("tracker_id", name);
        zipkinTestUtil.randomThrowException(0.3F, "RestJdbcController");

        String sql = "SELECT Host, User FROM user";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        zipkinNewSpan();

        return String.format("hi %s: %s, mysql return：%s", name, new Date().toString(), JSON.toJSONString(list));
    }

    private void zipkinNewSpan() {
        Span span = tracing.tracer().nextSpan().name("restJdbcTestNewSpan").start();
        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
            zipkinTestUtil.randomSleep(2);
            zipkinContinueSpan();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

    private void zipkinContinueSpan() {
        Span span = tracing.tracer().nextSpan().name("restJdbcTestContinueSpan").start();
        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
            zipkinTestUtil.randomThrowException(0.4F, "zipkinContinueSpan");;
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }

}
