// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package general.board;

import java.util.Locale;

public class Clock extends Thread {

	public Clock(String name, ClockDriven driven, double hertz) {
		this(name);
		this.driven = driven;
		speed(hertz);
		start();
	}

	protected Clock(String name) {
		super(name);
		running = false;
		alive = true; 
	}
	
	public void go() {
		if (cycleDuration == 0 ) return;
		running = true;
		interrupt();
	}

	public void pause() {
		running = false;
		interrupt();
		synchronized (this) { /* Just wait for the running loop to pause */ }
	}
	
	public void terminate() {
		alive = false;
		running = false;
		interrupt();
		try {
			join();		/* Just wait for the running loop to end */
		} catch (InterruptedException e) {
			// No problem
		}
	}

	public void speed(double hertz) {
		if (this.hertz == hertz) return;
		boolean wasRunning = running;
		pause();
		this.hertz = hertz;
		cycleDuration = hertz > 0 ? (long) (1 / hertz * 1000000000) : (long)hertz;
		if (wasRunning) go();
	}

	@Override
	public synchronized void run() {
		while(alive) {
			while(alive && !running)
				try { 
					wait(); 
				} catch (InterruptedException e) {}
			if (!alive) return;
			long waitTime;
			cycle = 0;
			startTime = systemNanos();
			try {
				while(running) {
					driven.clockPulse();
					cycle++;
					if (cycleDuration > 0) {
						waitTime = startTime + cycle * cycleDuration - systemNanos();
						if (waitTime > 0)
								sleep(waitTime / 1000000, (int) (waitTime % 1000000));
						else
							yield();
					} else 
						yield();
				}
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public String toString() {
		String res = alive ? (running ? "Running" : "Paused") : "Terminated"; 
		if (running) {
			float cy = (float)cyclesPerSecond();
			if (cy > 1000000) res = res + " at " + String.format(Locale.ENGLISH, "%.3fMHz", cy / 1000000);
			else if (cy > 1000)	res = res + " at " + String.format(Locale.ENGLISH, "%.3fKHz", cy / 1000);
				else res = res + " at " + String.format(Locale.ENGLISH, "%.3fHz", cy);
		}
		return res;
	}

	protected long systemNanos() {
		return System.nanoTime();
	}
	
	private float cyclesPerSecond() {
		long elapsed = systemNanos() - startTime;
		if (elapsed <= 0) return -1;
		return ((float)cycle / ((float)elapsed / 1000000000));
	}

	protected double hertz;
	protected ClockDriven driven;
	protected boolean alive = false;
	protected boolean running = false;

	protected long cycleDuration;   		// In nanoseconds. -1 = Maximum Speed, 0 = never starts
	protected long cycle = 0;
	protected long startTime = 0;

}
