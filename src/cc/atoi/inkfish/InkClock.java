package cc.atoi.inkfish;

/**
 * A music sequencer
 * @author Adam Saponara
 */
abstract public class InkClock {

	public InkClock(InkClockListener listener) {
		this.listener = listener;
	}

	/**
	 * A device that is listening to sequencing events. (This will be the
	 * parent Inkfish instance.)
	 */
	protected InkClockListener listener;
	
	/**
	 * A value that increments once for every sequencer tick
	 */
	protected long tick;
	
	/**
	 * A flag that denotes whether the sequencer is currently running
	 */
	protected boolean isRunning = false;
	
	/**
	 * Plays the sequencer
	 */
	abstract public void play();
	
	/**
	 * Stops the sequencer
	 */
	abstract public void stop();
	
	/**
	 * Pauses the sequencer
	 */
	abstract public void pause();
	
	/**
	 * Returns whether or not the sequencer is internally controllable
	 */
	abstract public boolean isControllable();
	
	/**
	 * Returns the current sequencer tick value
	 */
	public long getTick() { return tick; }
	
	/**
	 * Returns whether or not the sequencer is running
	 */
	public boolean isRunning() { return isRunning; }

}
