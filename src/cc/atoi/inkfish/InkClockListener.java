package cc.atoi.inkfish;

/**
 * A device that listens for clock/sequencer events
 * @author Adam Saponara
 */
public interface InkClockListener {

	/**
	 * Invoked for every sequencer tick
	 */
	public void onTick(long tick);
	
	/**
	 * Invoked when the sequencer is stopped
	 */
	public void onStop(long tick);

	/**
	 * Invoked when the sequencer is started from the beginning
	 */
	public void onStart(long tick);
	
	/**
	 * Invoked when the sequencer is resumed
	 */
	public void onContinue(long tick);
	
	/**
	 * Invoked for every quarter note
	 */
	public void onQuarterNote(long tick);
	
}
