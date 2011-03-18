package cc.atoi.inkfish;

abstract public class InkClock {
	protected InkClockListener listener;
	protected long tick;
	protected boolean isRunning = false;
	abstract public void play();
	abstract public void stop();
	abstract public void pause();
	abstract public boolean isControllable();
	public InkClock(InkClockListener listener) { this.listener = listener; }
	public long getTick() { return tick; }
	public boolean isRunning() { return isRunning; }
}
