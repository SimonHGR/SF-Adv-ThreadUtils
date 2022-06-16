package concurrent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Loader implements Runnable {
  private int start;
  private Map<Integer, String> mis;
  public Loader(int start, Map<Integer, String> mis) {
    this.start = start;
    this.mis = mis;
  }

  @Override
  public void run() {
    for (int i = start; i < start + 1_000; i++) {
      mis.put(i, "value " + i);
    }
    System.out.println("Loader at " + start + " finished");
  }
}

class Reader implements Runnable {
  private int start;
  private Map<Integer, String> mis;
  public Reader(int start, Map<Integer, String> mis) {
    this.start = start;
    this.mis = mis;
  }

  @Override
  public void run() {
    for (int i = start; i < start + 1_000; i++) {
      String s;
      long miscount = 0;
      while ((s = mis.get(i)) == null) {
        miscount++;
        if (miscount % 1_000_000 == 0) {
          System.out.println("missing " + i);
        }
      }

      if (!s.equals("value " + i)) {
        System.out.println("**** ERROR at value " + i + " => " + s);
      }
    }
    System.out.println("Reader at " + start + " finished");
  }
}

public class UseAMap {
  public static void main(String[] args) throws Throwable {
    // Adjust thread count to suit your platform
    final int THREAD_COUNT = 100;
//    final int THREAD_COUNT = 32768;

//    Map<Integer, String> mis = new HashMap<>();
//    Map<Integer, String> mis = Collections.synchronizedMap(new HashMap<>());
    Map<Integer, String> mis = new ConcurrentHashMap<>();

    // BEWARE OF "micro benchmarks"!!!
    long start = System.nanoTime();

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < THREAD_COUNT; i++) {
      Thread t = new Thread(new Loader(1_000 * i, mis));
      threads.add(t);
      t.start();
    }
    for (int i = 0; i < THREAD_COUNT; i++) {
      Thread t = new Thread(new Reader(1_000 * i, mis));
      threads.add(t);
      t.start();
    }

    System.out.println("all started");
    for (Thread t : threads) {
      t.join();
    }
    long time = System.nanoTime() - start;
    System.out.println("Using " + mis.getClass().getName()
        + " \nand count of " + THREAD_COUNT
        + "\nelapsed time " + (time / 1_000_000_000.0));
  }
}