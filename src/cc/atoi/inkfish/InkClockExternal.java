package cc.atoi.inkfish;

abstract public class InkClockExternal extends InkClock {
	public void play() { }
	public void stop() { }
	public void pause() { }
	public boolean isControllable() { return false; }
	public InkClockExternal(InkClockListener listener) { super(listener); }
}
