package cc.atoi.inkfish;

abstract public class InkClockInternal extends InkClock {
	public boolean isControllable() { return true; }
	public InkClockInternal(InkClockListener listener) { super(listener); }
}
