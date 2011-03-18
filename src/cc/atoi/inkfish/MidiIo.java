package cc.atoi.inkfish;

import javax.sound.midi.*;

/*
 * MIDI I/O port wrapper
 */
abstract class MidiIo {

	protected MidiDevice device;

	public String getName() {
		return device.getDeviceInfo().getName();
	}

	protected void finalize() throws Throwable {
		device.close();
		super.finalize();
	}
}
