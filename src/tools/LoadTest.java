package tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.xrtb.common.HttpPostGet;

public class LoadTest {

	public static final int MAX  = 500000;
	public static void main(String [] args) throws Exception {
		
		int limit = 4;
		
		CountDownLatch slatch = new CountDownLatch(1);
		CountDownLatch latch = new CountDownLatch(limit);
		
		List<Runner> tasks = new ArrayList();
		
		for (int i=0;i<limit;i++) {
			tasks.add(new Runner(i,"SampleBids/nexage.txt","http://localhost:8080/rtb/bids/nexage",slatch,latch));
		}
		
		slatch.countDown();
		latch.await();
		
		int k = 0;
		double d = 0;
		for (Runner task : tasks) {
			d += task.now;
			k += task.count;
		}
		d /= 1000;
		System.out.println("Fully loaded bps: " + (k/d));
	}
	
}

class Runner implements Runnable {

	public long start;
	public long now;
	
	public long outm;
	public long inm;
	
	Thread me;
	HttpPostGet post = new HttpPostGet();;
	String data;
	String host;
	CountDownLatch slatch;
	CountDownLatch latch;
	int who;
	int count;
	
	public Runner(int who, String data, String host, CountDownLatch slatch, CountDownLatch latch) throws Exception {
		this.data = new String(Files.readAllBytes(Paths.get(data)),StandardCharsets.UTF_8);
		this.host = host;
		this.latch = latch;
		this.slatch = slatch;
		this.who = who;
		me = new Thread(this);
		me.start();
	}

	public void run() {
		try {
			slatch.await();
			System.out.println("Runner " + who + " Starting...");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		start = System.currentTimeMillis();
			try {
				for (count =0; count<LoadTest.MAX;count++) {
					post.sendPost("http://localhost:8080/rtb/bids/nexage", data, 5000, 5000);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		now = System.currentTimeMillis() - start;		
		latch.countDown();
		System.out.println("Runner " + who + " Done at " + count + "...");
	}
	
}
