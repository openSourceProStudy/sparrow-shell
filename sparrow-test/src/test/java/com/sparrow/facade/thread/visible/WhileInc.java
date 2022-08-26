package com.sparrow.facade.thread.visible;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Under the new memory model, it is still true that
 * volatile variables cannot be reordered with each other.
 * The difference is that it is now no longer so easy
 * to reorder normal field accesses around them.
 * Writing to a volatile field has the same memory
 * effect as a monitor release,
 * and reading from a volatile field has the same memory
 * effect as a monitor acquire.
 * In effect, because the new memory model places stricter
 * constraints on reordering of volatile field accesses with other field accesses,
 * volatile or not, anything that was visible to thread
 * A when it writes to volatile field f becomes visible
 * to thread B when it reads f.
 */
public class WhileInc {
    private volatile int x = 0;
    private int y = 0;
//    Lock lock=new ReentrantLock();
    public static void main(String[] args) throws InterruptedException {
        WhileInc v=new WhileInc();
        int count=10000;
        for(int i=0;i<count;i++) {
           new Thread(v::inc).start();
        }
        while (v.x!=count) {
            System.out.println(v.x);
            System.out.println(v.y);
//            Thread.sleep(1000);
        }
    }


    public void inc() {
//        lock.lock();
        y ++;
        x ++;
//        lock.unlock();
     }
}