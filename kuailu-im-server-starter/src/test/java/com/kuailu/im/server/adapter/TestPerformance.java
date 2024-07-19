package com.kuailu.im.server.adapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试性能.
 */
public class TestPerformance implements Runnable {

    //每个线程的执行次数
    private int size;

    //记录多线程的总执行次数，保证高并发下的原子性
    public static AtomicInteger atomicInteger = new AtomicInteger(0);

    public TestPerformance(int size) {
        this.size = size;
    }

    @Override
    public void run() {

        int count = 0;
        while (count < size) {
            count++;

            atomicInteger.getAndIncrement();

            ///
            //在此写入需要测试性能的代码块
            ///

            System.out.println("线程ID与对应的执行次数：" + Thread.currentThread().getId() + "--->" + count);
        }
    }
}