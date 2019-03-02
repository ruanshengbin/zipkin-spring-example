# zipkin-spring-example
基于spring boot 2.1.2的zipkin集成示例

# 主要内容
项目包含Spring WebMvc、Kafka Connector、Kafka Stream、Jdbc、Schedule、自定义Span、Span注解的使用等

# 集成 Sleuth + Zipkin

brave库依赖：服务跟踪数据收集上报
```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-bom</artifactId>
            <version>5.6.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

使用zipkin进行服务跟踪数据上报
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

sleuth依赖
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

## jdbc服务跟踪

服务跟踪消息拦截：需要注意mysql-connector-java的版本，对应的拦截器版本是不一样的，jdbc的url需要添加url参数queryInterceptors=brave.mysql8.TracingQueryInterceptor&exceptionInterceptors=brave.mysql8.TracingExceptionInterceptor，[详细说明参考](https://github.com/openzipkin/brave/tree/release-5.6.1/instrumentation/mysql8)
```
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-mysql8</artifactId>
</dependency>
```

## kafka connector服务跟踪

服务跟踪消息拦截：kafka clients的版本必须是0.11.0以上（支持消息头）
```
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-kafka-clients</artifactId>
</dependency>
```

## kafka streams服务跟踪

服务跟踪消息拦截：kafka streams的版本必须是2.0.0以上（支持消息头）
```
<dependency>
    <groupId>io.zipkin.brave</groupId>
    <artifactId>brave-instrumentation-kafka-streams</artifactId>
</dependency>
```

# 主要配置说明
- server.port：web访问端口
- spring.application.name：应用名称
- spring.sleuth.sampler.percentage：服务跟踪数据采样率，0-1之间的浮点数
- spring.zipkin.sender.type：数据上报的方式web（使用http上报），kafka（使用kafka上报，依赖相关kafka配置）
- spring.zipkin.baseUrl：zipkin服务端地址
- spring.datasource.url：mysql地址
- spring.datasource.username：mysql用户名
- spring.datasource.password：mysql密码
- alibaba druid config：[参考官网介绍](https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)
- kafka producer config:kafka connector生产者相关配置，test.src.topic是发送的topic（stream监听）
- kafka stream config：kafka streams相关配置，test.src.topic是接收的topic配置，test.end.topic是输出的topic配置
- kafka consumer config：kafka connector消费者相关配置，test.end.topic是接收的topic配置（stream的输出topic）


# 自定义Span使用
如果没有用sleuth提供的已经实现的拦截器（webmvc、Schedule等）就需要自己来管理Span进行指标收集，以下示例的tracer（org.springframework.cloud.sleuth.Tracer）可以使用注解引入

[详细介绍参考sleuth文档](https://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/2.0.0.RELEASE/single/spring-cloud-sleuth.html#_local_tracing)
```java
When tracing local code, you can run it inside a span, as shown in the following example:

@Autowired Tracer tracer;

Span span = tracer.newTrace().name("encode").start();
try {
  doSomethingExpensive();
} finally {
  span.finish();
}


TIn the preceding example, the span is the root of the trace. In many cases, the span is part of an existing trace. When this is the case, call newChild instead of newTrace, as shown in the following example:

@Autowired Tracer tracer;

Span span = tracer.newChild(tracer.currentTraceContext().get()).name("encode").start();
try {
  doSomethingExpensive();
} finally {
  span.finish();
}
```

# 运行
## 启动zipkin server
[详细介绍参考zipkin server文档](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)

使用kafka进行数据上报需要注意kafka的版本，zipkin-server-2.11.x使用的kafka-client是2.x版本的

kafka Broker 0.10.2+对kafka-clients兼容性较好（[详细兼容信息参考](https://cwiki.apache.org/confluence/display/KAFKA/Compatibility+Matrix)），kafka Broker建议升级到0.10.2+

kafka-clients 0.11.0.0+版本之上的kafka-clients的消息可以互通，client建议升级到0.11.0.0+

## 打包
```
mvn clean package
```

## 启动服务

部分配置可以在打包的时候内置，也可以使用外置配置文件或者spring cloud的配置中心
```
java -jar zipkin-spring-example-2.1.2.RELEASE.jar --server.port=9998
```

## 服务调用示例

### rest接口与Jdbc调用示例
```
curl -i 'http://127.0.0.1:9998/zipkin/jdbc/ruan'

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 159
Date: Sat, 02 Mar 2019 14:40:22 GMT

hi ruan: Sat Mar 02 22:40:22 CST 2019!
 mysql return：[{"Host":"%","User":"root"},{"Host":"localhost","User":"mysql.sys"},{"Host":"localhost","User":"root"}]

```

### Rest接口与kafka调用示例

```
curl -i 'http://127.0.0.1:9998/zipkin/kafka/ruan'

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 107
Date: Sat, 02 Mar 2019 14:42:28 GMT

hi ruan: Sat Mar 02 22:42:28 CST 2019!
 send message to kafka: topic= zipkin-kafka-test-src, messages=ruan
```
