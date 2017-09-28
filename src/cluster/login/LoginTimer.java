package cluster.login;

import java.util.Arrays;

public class LoginTimer {
	
	private long[] queue;
	private long time;
	
	/**
	 * Create a login limiter
	 * @param timeout - the range of time that's count to analyze logging
	 * @param threshold - max amount of logins in specified amount of time
	 */
	public LoginTimer(long timeout, int threshold) {
		if(threshold < 0 || timeout < 0) {
			queue = null;
			return;
		}
		
		queue = new long[threshold + 1];
		Arrays.fill(queue, System.currentTimeMillis());
		time = timeout;
	}
	
	/**
	 * Determine that login has occurred
	 */
	public void login() {
		if(queue == null) return;
		
		long[] order = new long[queue.length];
		
		for (int i = 0; i < queue.length; i++) {
			order[i + 1 == queue.length ? 0 : i + 1] = queue[i];
		}
		queue = order;
		queue[0] = System.currentTimeMillis();
	}
	
	/**
	 * Whether to allow logging
	 */
	public boolean allowed() {
		if(queue == null) return true;
		
		long currTime = System.currentTimeMillis();
		
		for (int i = 1; i < queue.length; i++) {
			if(currTime - queue[i] > time) return true;
		}
		return false;
	}
}
