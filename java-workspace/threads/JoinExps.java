package threads;


class Task implements Runnable {
    private String threadName;
    private int duration;

    public Task(String name, int duration) {
        this.threadName = name;
        this.duration = duration;
    }

    @Override
    public void run() {
        System.out.println(threadName + " Started:");
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            System.out.println(threadName + " stopped due to an exception: " + e.getMessage());
        }
        System.out.println(threadName + " Finished");
    }

}

public class JoinExps {
    public static void main(String[] args) {
        Thread thread1 = new Thread(new Task("Thread-1", 5000));
        Thread thread2 = new Thread(new Task("Thread-2", 2000));

        thread1.start();
        thread2.start();

        try {
            System.out.println("Waiting for Thread-1 to finish.");
            thread1.join();
            System.out.println("Thread-1 has finished.");

            System.out.println("Waiting for Thread-2 to finish.");
            thread2.join();
            System.out.println("Thread-2 has finished.");
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted: " + e.getMessage());
        }

        System.out.println("All threads have finished execution.");
        System.out.println("-----------------Notes-----------------");
        System.out.println("Join makes the main thread to wait for instance thread to complete before proceeding to next execution.");
        System.out.println("So it's guaranteed that Thread-2 has finished will be the last statement before All threads have finished execution.");
        System.out.println("Memory tip: join is like await in javascript.");

    }
}
