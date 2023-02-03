package com.sparrow.lesson.thread.aqs.condition;

public class ParkTest {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    if(Thread.currentThread().isInterrupted()) {
                        System.out.println("do business ....");
                        System.out.println("closing  ....");
                        break;
                        // LockSupport.park();
                    }
                    System.out.println("end...." + Thread.currentThread().isInterrupted());
                }
            }
        });
        thread.start();
        thread.interrupt();
    }
}
