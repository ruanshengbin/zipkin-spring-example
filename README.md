# zipkin-spring-example
基于spring boot的zipkin集成示例

# 概述
随着业务发展，系统拆分导致系统调用链路愈发复杂一个前端请求可能最终需要调用很多次后端服务才能完成，当整个请求变慢或不可用时，很难得知该请求是由某个或某些后端服务引起的，这时就需要解决如何快速定位服务故障点，于是就有了分布式系统调用跟踪的诞生。zipkin就是开源分布式系统调用跟踪的佼佼者，本项目主要介绍基于spring boot 1.5.x和2.x中集成zipkin的相关使用方法

在分布式系统调用跟踪系统中zipkin扮演数据存储和展示的角色，Spring Cloud Sleuth结合Brave实现数据收集和上报功能

从Spring Cloud Sleuth 2.0.0开始数据收集工作使用openzipkin的brave的相关库，因此2.0.0前后的版本的使用方式区别较大，项目给出了两个版本对应的示例

- [zipkin官网地址](https://zipkin.io/)
- [Spring Cloud Sleuth最新发布版文档地址](https://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/2.1.0.RELEASE/single/spring-cloud-sleuth.html)
- [openzipkin项目地址](https://github.com/openzipkin)
- [brave项目地址](https://github.com/openzipkin/brave)
