package cc.atoi.inkfish;

public interface InkClockListener {
	public void onTick(long tick);
	public void onStop(long tick);
	public void onStart(long tick);
	public void onContinue(long tick);
	public void onQuarterNote(long tick);
}
