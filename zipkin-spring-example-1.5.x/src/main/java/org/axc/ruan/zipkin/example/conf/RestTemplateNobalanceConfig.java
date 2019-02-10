package org.axc.ruan.zipkin.example.conf;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 非负载均衡的RestTemplate：不能根据服务名使用，需要具体的ip地址
 * @author ruan
 * @Date 2018年8月10日上午10:58:11
 *
 */
@Configuration("rest.template.nobalance.config")
public class RestTemplateNobalanceConfig {

	@Value("${rest.template.connection.timeout:10000}")
	private int connectionTimeout;

	@Value("${rest.template.read.timeout:30000}")
	private int readTimeout;

	/**
	 * REST接口
	 * @param builder 接口构建器
	 * @return REST接口
	 */
	@Bean("rest.template.nobalance")
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate restTemplate = builder.setConnectTimeout(connectionTimeout).setReadTimeout(readTimeout).build();
		restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(Charset.defaultCharset()));

		return restTemplate;
	}

}
