# zipkin-spring-example
基于spring boot 1.5.9的zipkin集成示例

# 主要内容
项目包含Spring WebMvc、RestTemplate、Schedule、自定义Span、Span注解的使用等

# 集成 Sleuth + Zipkin

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

# 主要配置说明
- server.port：web访问端口
- spring.application.name：应用名称
- spring.sleuth.sampler.percentage：服务跟踪数据采样率，0-1之间的浮点数
- spring.zipkin.sender.type：数据上报的方式web（使用http上报），kafka（使用kafka上报，依赖相关kafka配置）
- spring.zipkin.baseUrl：zipkin服务端地址
-- backend.url：web服务后端地址

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

### 单进程启动
web前端和后端在同一个进程

```
java -jar zipkin-spring-example-1.5.9.RELEASE.jar --server.port=8888 --spring.application.name=zipkin-test --spring.zipkin.baseUrl=http://127.0.0.1:9411/ --backend.url=http://127.0.0.1:8888
```

### 多进程启动
web前端和后端分别对应一个进程，可以模拟多进程的调用

```
java -jar zipkin-spring-example-1.5.9.RELEASE.jar --server.port=8888 --spring.application.name=zipkin-test-front --spring.zipkin.baseUrl=http://127.0.0.1:9411/ --backend.url=http://127.0.0.1:8889

java -jar zipkin-spring-example-1.5.9.RELEASE.jar --server.port=8889 --spring.application.name=zipkin-test-backend  --spring.zipkin.baseUrl=http://127.0.0.1:9411/ --backend.url=http://127.0.0.1:8889
```

## 服务调用示例

backend会40%的概率随机抛出异常来模拟错误的情况，运气好的话你会看到一个

```
curl -i 'http://127.0.0.1:8888/zipkin/front/ruan'

HTTP/1.1 200
Content-Type: text/plain;charset=UTF-8
Content-Length: 37
Date: Mon, 11 Feb 2019 14:14:30 GMT

hi ruan: Mon Feb 11 22:14:30 CST 2019
```

### rest接口调用处理流程

```graph
graph TD;
    Frontend:callBackend-->Frontend:frontCustomNewSpan;
    Frontend:frontCustomNewSpan-->Frontend:random-sleep;
    Frontend:random-sleep-->Frontend:random-throw-exception;
    Frontend:random-throw-exception-->Backend:printDate;
    Backend:printDate-->Backend:random-sleep;
```

