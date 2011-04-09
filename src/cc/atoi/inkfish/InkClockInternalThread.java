package cc.atoi.inkfish;

import java.util.concurrent.*;

/**
 * An internal thread-based sequencer. This class generates sequencer ticks
 * on a regular interval using a ScheduledExecutorService.
 * @author Adam Saponara
 */
public class InkClockInternalThread extends InkClockInternal implements Runnable {
	
	/**
	 * The scheduler used to generate ticks on a regular interval
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	/**
	 * Latest ticket that was returned from scheduler
	 */
	private ScheduledFuture ticket = null;
	
	/**
	 * Stores the value of the delay to set on the next tick
	 */
	private Long nextMilliDelay = null;
	
	/**
	 * Delay between each tick in milliseconds
	 */
	private long milliDelay;
	
	/**
	 * Number of ticks per quarter note
	 */
	private int ppqn;
	
	/**
	 * Incrementing tick value
	 */
	private long tick = 0;
	
	/**
	 * Increments once per tick within a single quarter note
	 */
	private int pulse = 0;
	
	/**
	 * Whether or not the sequencer is paused
	 */
	private boolean isPaused = false;
	
	/**
	 * Whether or not the sequencer is stopped
	 */
	private boolean isStopped = true;

	/**
	 * Sets up an internal sequencer with PPQN = 24
	 * @param listener		device that listens for sequencer events
	 * @param milliDelay	number of milliseconds between each tick
	 */
	public InkClockInternalThread(long milliDelay) {
		this(milliDelay, 24);
	}

	/**
	 * Sets up an internal sequencer 
	 * @param listener		device that listens for sequencer events
	 * @param milliDelay	number of milliseconds between each tick
	 * @param ppqn			number of ticks per quarter note
	 */
	public InkClockInternalThread(long milliDelay, int ppqn) {
		this.ppqn = ppqn;
		this.milliDelay = milliDelay;
	}

	/**
	 * Invoked whenever the thread runs (each tick)
	 */
	public void run() {
		
		// Skip if paused
		if (isPaused) return;
		
		// onTick event
		raiseOnTick(tick);
		
		// onQuarterNote event if applicable
		pulse++;
		if (pulse == ppqn) {
			pulse = 0;
			raiseOnQuarterNote(tick);
		}
		
		// Change delay if needed
		if (nextMilliDelay != null) {
			long newDelay = nextMilliDelay.longValue();
			if (ticket != null) {
				ticket.cancel(true);
			}
			ticket = scheduler.scheduleAtFixedRate(this, newDelay, newDelay, TimeUnit.MILLISECONDS);
			this.milliDelay = newDelay;
			nextMilliDelay = null;
		}
		
		// Increment tick
		tick++;
	}
	
	/**
	 * Sets the delay between each tick
	 * @param milliDelay delay in milliseconds
	 */
	public void setDelay(long milliDelay) {
		nextMilliDelay = new Long(milliDelay);
	}

	/**
	 * Starts or unpauses the sequencer 
	 */
	public void play() {
		if (isStopped) {
			isStopped = false;
			ticket = scheduler.scheduleAtFixedRate(this, 0, this.milliDelay, TimeUnit.MILLISECONDS);
			raiseOnStart(tick);
		}
		else if (isPaused) {
			isPaused = false;
			raiseOnContinue(tick);
		}
	}

	/**
	 * Stops the sequencer
	 */
	public void stop() {
		ticket.cancel(true);
		isStopped = true;
		raiseOnStop(tick);
	}

	/**
	 * Pauses the sequencer
	 */
	public void pause() {
		isPaused = true;
		raiseOnStop(tick);
	}

}
