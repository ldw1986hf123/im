package com.kuailu.im.server.client;

public class Test {
    private static final ThreadLocal<String> TL = new ThreadLocal<String>();

    public static void main(String[] args) throws InterruptedException {
        TestThread t1 = new TestThread("a");
        t1.start();
        Thread.sleep(1000);
        TestThread t2 = new TestThread("b");
        t2.start();
    }

    static class TestThread extends Thread {
        private String str;

        public TestThread(String str) {
            this.str = str;
            TL.set(str);
            init();
        }

        public void init() {
            System.out.println("init:" + TL.get());
        }

        @Override
        public void run() {
            System.out.println("run:" + TL.get());
            TL.set(str);
            System.out.println("run2:" + TL.get());
        }
    }
}