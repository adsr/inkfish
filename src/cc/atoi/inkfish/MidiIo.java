package cc.atoi.inkfish;

import javax.sound.midi.*;

/**
 * MIDI I/O port wrapper (mainly because the concept behind
 * javax.sound.midi.Receiver keeps confusing me)
 * @author Adam Saponara
 */
abstract class MidiIo {

	/**
	 * MIDI device we are wrapping
	 */
	protected MidiDevice device;

	/**
	 * Returns name of MIDI device
	 */
	public String getName() {
		return device.getDeviceInfo().getName();
	}

	/**
	 * Upon garbage collection, close underlying MIDI device
	 */
	protected void finalize() throws Throwable {
		try {
			device.close();
		}
		finally {
			super.finalize();
		}
	}
}
