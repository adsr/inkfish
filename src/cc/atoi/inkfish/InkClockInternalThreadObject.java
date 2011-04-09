package cc.atoi.inkfish;

import org.mozilla.javascript.*;

public class InkClockInternalThreadObject extends ScriptableObject implements Scriptable {

	/**
	 * Required for serialization
	 */
	private static final long serialVersionUID = 6191299674199520639L;
	
	/**
	 * Internal sequencer
	 */
	private InkClockInternalThread clock = null;
	
	public InkClockInternalThreadObject() { }

	public String getClassName() { return "InkClockInternalThreadObject"; }

	public void jsConstructor() { }

	/**
	 * Set internal sequencer
	 * @param clock internal sequencer
	 */
	public void setClock(InkClockInternalThread clock) { this.clock = clock; }
	
	/**
	 * Set clock delay on sequencer
	 * @param delay
	 */
	public void jsFunction_setDelay(int delay) { clock.setDelay((long)delay); }
	
	/**
	 * Starts the sequencer
	 */
	public void jsFunction_play() { clock.play(); }
	
	/**
	 * Stops the sequencer
	 */
	public void jsFunction_stop() { clock.stop(); }
	
	/**
	 * Pauses the sequencer
	 */
	public void jsFunction_pause() { clock.pause(); }
	
}
