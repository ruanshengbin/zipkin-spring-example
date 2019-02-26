package org.axc.ruan.zipkin.example.service;

public interface ZipkinTestUtil {

    /**
     * 按照指定概率随机抛出异常，比如percentage=0.5就会有50%的概率抛出异常
     * @Title: randomThrowException
     * @param percentage
     * @param desc
     * @return: void
     */
    void randomThrowException(float percentage, String desc);

    /**
     * 随机休眠0-second秒
     * @Title: randomSleep
     * @param second
     * @return: void
     */
    void randomSleep(int second);

    /**
     * spring schedule example 定时一分钟调用一次随机随机休眠0-1秒
     * @Title: scheduleExample
     * @return: void
     */
    void scheduleExample();

}
