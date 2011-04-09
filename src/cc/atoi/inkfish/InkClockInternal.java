package cc.atoi.inkfish;

/**
 * An internally controllable sequencer
 * @author Adam Saponara
 */
abstract public class InkClockInternal extends InkClock {
	
	/**
	 * Returns true
	 */
	public boolean isControllable() {
		return true;
	}
	
}
