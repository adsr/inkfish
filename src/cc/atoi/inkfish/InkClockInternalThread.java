package cc.atoi.inkfish;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An internal thread-based sequencer. This class generates sequencer ticks
 * on a regular interval using a ScheduledExecutorService.
 * @author Adam Saponara
 */
public class InkClockInternalThread extends InkClockInternal implements Runnable {
	
	/**
	 * The scheduler used to generate ticks on a regular interval. 
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	/**
	 * Delay between each tick in milliseconds.
	 */
	private int milliDelay;
	
	/**
	 * Number of ticks per quarter note.
	 */
	private int ppqn;
	
	/**
	 * Incrementing tick value.
	 */
	private long tick = 0;
	
	/**
	 * Increments once per tick within a single quarter note.
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
	public InkClockInternalThread(InkClockListener listener, int milliDelay) {
		this(listener, milliDelay, 24);
	}

	/**
	 * Sets up an internal sequencer 
	 * @param listener		device that listens for sequencer events
	 * @param milliDelay	number of milliseconds between each tick
	 * @param ppqn			number of ticks per quarter note
	 */
	public InkClockInternalThread(InkClockListener listener, int milliDelay, int ppqn) {
		super(listener);
		this.ppqn = ppqn;
		this.milliDelay = milliDelay;
	}

	/**
	 * Invoked whenever the thread runs (each tick)
	 */
	public void run() {
		if (isPaused) return;
		listener.onTick(tick);
		pulse++;
		if (pulse == ppqn) {
			pulse = 0;
			listener.onQuarterNote(tick);
		}
		tick++;
	}

	/**
	 * Starts or unpauses the sequencer 
	 */
	public void play() {
		if (isStopped) {
			isStopped = false;
			scheduler.scheduleAtFixedRate(this, 0, this.milliDelay, TimeUnit.MILLISECONDS);
			listener.onStart(tick);
		}
		else if (isPaused) {
			isPaused = false;
			listener.onContinue(tick);
		}
	}

	/**
	 * Stops the sequencer
	 */
	public void stop() {
		scheduler.shutdownNow();
		isStopped = true;
		listener.onStop(tick);
	}

	/**
	 * Pauses the sequencer
	 */
	public void pause() {
		isPaused = true;
		listener.onStop(tick);
	}

}
