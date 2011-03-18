package cc.atoi.inkfish;

import java.util.ArrayList;
import javax.sound.midi.*;

/*
 * MIDI Input port wrapper
 */
public class MidiInput extends MidiIo implements Receiver {

	private ArrayList<MidiInputListener> listeners;

	public MidiInput(MidiDevice device) throws MidiUnavailableException {
		this.device = device;
		this.device.getTransmitter().setReceiver(this);
		listeners = new ArrayList<MidiInputListener>(1);
	}

	public void addListener(MidiInputListener listener) {
		listeners.add(listener);
	}

	public void send(MidiMessage msg, long ts) {
		for (MidiInputListener listener : listeners) {
			listener.onMidiIn(this, msg, ts);
		}
	}

	public void close() {
	}

}
