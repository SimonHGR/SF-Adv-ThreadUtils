package atomics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

// To see the JIT compiler "busy"
// -XX:+PrintCompilation

public class Counter {
//  public static /*volatile*/ long count = 0;
//  public static AtomicLong count = new AtomicLong(0);
//  public static LongAdder count = new LongAdder();
  public static LongAccumulator count = new LongAccumulator(Long::sum, 0);

  public static Object rendezvous = new Object();
  public static ReentrantLock lock = new ReentrantLock();

  public static void main(String[] args) throws Throwable {
    Runnable r = () -> {
      for (int i = 0; i < 100_000; i++) {
        // Uncontended synchronization, or immediately re-sychronizing
        // in the same thread, are MUCH FASTER than
        // contended synchronization, but still MUCH SLOWER
        // than not being sychronized (but if not being
        // synchronized is just plain wrong...!!)
//        lock.lock();
//        try {
//        synchronized (rendezvous) {
          // Read (memory into CPU), Increment (in CPU), Write (memory)
//          count++;
//          count.incrementAndGet();
//        count.increment();
        count.accumulate(1);
//        } finally {
//          lock.unlock();
//        }
      }
    };

    System.out.println("count before " + count);

    long start = System.nanoTime();

//    for (var i = 0; i < 2_000; i++) {
//      r.run();
//    }

    List<Thread> lt = new ArrayList<>();
    for (int i = 0; i < 2_000; i++) {
      Thread t = new Thread(r);
      lt.add(t);
      t.start();
    }

    for (Thread t : lt) {
      t.join();
    }
    long time = System.nanoTime() - start;
//    100,000 * 2,000 => 200,000,000
//    System.out.println("count after " + count);
//    System.out.println("count after " + count.get());
    System.out.println("count after " + count.longValue());
    System.out.println("time was " + (time / 1_000_000_000.0));
  }
}

/*
synchronized/contended: 15 seconds
ReentrantLock/contended: 4
AtomicLong/contended: 1.5
LongAdder/contended: 0.5
LongAccumulator/contended: 0.5
But "sequential" mode with a single thread
is FAR FASTER -- how do we choose?
 */