package threads;

public class ThreadDemo {
	public void initializeDemo(){
		System.out.println("Initializing Thread Demos");
		
		Thread t1 = new MyThread("Thread-1");
		t1.start();
		
		Thread t2 = new Thread(new MyRunnable("Thread-2"));
		t2.start();
		
		// Using Lambda Expression
		new Thread(()->{
			String threadName = "Thread-3";
			System.out.println(threadName + " Started:");
			for(int i = 1; i<=5;i++){
				System.out.println(threadName + " Operation: " + i);
				try{
					Thread.sleep(500);
				}catch(Exception e){
					System.out.println(threadName + " stopped due to an exception" +e.getMessage());
				}
			}
			System.out.println(threadName + " Finished");
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				String threadName = "Thread-4";
				System.out.println(threadName + " Started:");
				for(int i = 1; i<=5;i++){
					System.out.println(threadName + " Operation: " + i);
					try{
						Thread.sleep(500);
					}catch(Exception e){
						System.out.println(threadName + " stopped due to an exception" +e.getMessage());
					}
				}
				System.out.println(threadName + " Finished");
			}
		}).start();
	}
}

class MyThread extends Thread {
	private String threadName;

	public MyThread(String name) {
		this.threadName = name;
	}

	@Override
	public void run() {
		System.out.println(threadName + " Started:");
		for (int i = 1; i <= 5; i++) {
			System.out.println(threadName + " Operation: " + i);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				System.out.println(threadName + " stopped due to an exception" + e.getMessage());
			}
		}
		System.out.println(threadName + " Finished");
	}

}

class MyRunnable implements Runnable {
	private String threadName;

	public MyRunnable(String name) {
		this.threadName = name;
	}

	@Override
	public void run() {
		System.out.println(threadName + " Started:");
		for (int i = 1; i <= 5; i++) {
			System.out.println(threadName + " Operation: " + i);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				System.out.println(threadName + " stopped due to an exception" + e.getMessage());
			}
		}
		System.out.println(threadName + " Finished");
	}

}