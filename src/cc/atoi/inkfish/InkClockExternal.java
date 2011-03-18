package cc.atoi.inkfish;

/**
 * An external sequencer (such as a hardware MIDI sequencer). Note that play,
 * pause and stop do nothing because we don't have direct control.
 * @author Adam Saponara
 */
abstract public class InkClockExternal extends InkClock {

	public InkClockExternal(InkClockListener listener) {
		super(listener);
	}

	/**
	 * No implementation
	 */
	public void play() { }
	
	/**
	 * No implementation
	 */
	public void stop() { }
	
	/**
	 * No implementation
	 */
	public void pause() { }
	
	/**
	 * Returns false
	 */
	public boolean isControllable() {
		return false;
	}
	
}
