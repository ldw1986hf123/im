package com.kuailu.im.server.adapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PerformTest {

    public static void main(String[] args) throws InterruptedException {
        int threadSize = 100;  //开启的线程数
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        long start = System.currentTimeMillis();
        //让线程池中的每一个线程都开始工作
        for (int j = 0; j < threadSize; j++) {
            //执行线程
            executorService.execute(new TestPerformance(threadSize));
        }
        //等线程全部执行完后关闭线程池
        executorService.shutdown();
        executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        long end = System.currentTimeMillis();
        System.out.println("测试次数：" + TestPerformance.atomicInteger.get());
        System.out.println("用时：" + (end - start));
        System.out.println("速度：" + TestPerformance.atomicInteger.get() * 1000 / (end - start) + "次/秒");
    }


}
