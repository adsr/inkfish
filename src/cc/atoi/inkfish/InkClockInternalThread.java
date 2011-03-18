package cc.atoi.inkfish;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InkClockInternalThread extends InkClockInternal implements Runnable {
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private int delay;
	private int ppqn;
	private long tick = 0;
	private int pulse = 0;
	private boolean isPaused = false;
	private boolean isStopped = true;

	public InkClockInternalThread(InkClockListener listener, int milliDelay) {
		this(listener, milliDelay, 24);
	}

	public InkClockInternalThread(InkClockListener listener, int milliDelay, int ppqn) {
		super(listener);
		this.ppqn = ppqn;
		this.delay = milliDelay;
	}

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

	public void play() {

		if (isStopped) {
			isStopped = false;
			scheduler.scheduleAtFixedRate(this, 0, this.delay, TimeUnit.MILLISECONDS);
			listener.onStart(tick);
		}
		else if (isPaused) {
			isPaused = false;
			listener.onContinue(tick);
		}
	}

	public void stop() {
		scheduler.shutdownNow();
		isStopped = true;
		listener.onStop(tick);
	}

	public void pause() {
		isPaused = true;
		listener.onStop(tick);
	}

}
