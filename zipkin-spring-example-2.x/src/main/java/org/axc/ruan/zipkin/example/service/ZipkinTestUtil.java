package org.axc.ruan.zipkin.example.service;

public interface ZipkinTestUtil {

    /**
     * 按照指定概率随机抛出异常，比如percentage=0.5就会有50%的概率抛出异常
     * @Title: randomThrowExceptionWithSpan
     * @param percentage
     * @param desc
     * @return: void
     */
    void randomThrowExceptionWithSpan(float percentage, String desc);

    /**
     * 随机休眠0-second秒：会新建一个span
     * @Title: randomSleepWithSpan
     * @param maxSecond
     * @return: void
     */
    void randomSleepWithSpan(int maxSecond);

    /**
     * 随机休眠0-second秒
     * @Title: randomSleepWithSpan
     * @param second
     * @return: void
     * @throws InterruptedException 
     */
    void randomSleepNoSpan(int maxSecond) throws InterruptedException;

    /**
     * spring schedule example 定时一分钟调用一次随机随机休眠0-1秒
     * @Title: scheduleExample
     * @return: void
     */
    void scheduleExample();

}
