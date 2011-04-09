package cc.atoi.inkfish;

import java.util.ArrayList;

/**
 * A music sequencer
 * @author Adam Saponara
 */
abstract public class InkClock {

	/**
	 * A list of listeners / subscribers to sequencing events
	 */
	protected ArrayList<InkClockListener> listeners = new ArrayList<InkClockListener>(1);
	
	/**
	 * Add a listener to the list
	 */
	public void addListener(InkClockListener listener) {
		listeners.add(listener);
	}

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
	
	/**
	 * Raises onTick events for all listeners
	 */
	public void raiseOnTick(long tick) {
		for (InkClockListener l : listeners) l.onTick(tick);
	}
	
	/**
	 * Raises onStop events for all listeners
	 */
	public void raiseOnStop(long tick) {
		for (InkClockListener l : listeners) l.onStop(tick);
	}
	
	/**
	 * Raises onStart events for all listeners
	 */
	public void raiseOnStart(long tick) {
		for (InkClockListener l : listeners) l.onStart(tick);
	}
	
	/**
	 * Raises onContinue events for all listeners
	 */
	public void raiseOnContinue(long tick) {
		for (InkClockListener l : listeners) l.onContinue(tick);
	}
	
	/**
	 * Raises onQuarterNote events for all listeners
	 */
	public void raiseOnQuarterNote(long tick) {
		for (InkClockListener l : listeners) l.onQuarterNote(tick);
	}

}
