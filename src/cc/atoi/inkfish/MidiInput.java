package cc.atoi.inkfish;

import java.util.ArrayList;
import javax.sound.midi.*;

/**
 * MIDI Input port wrapper
 * @author Adam Saponara
 */
public class MidiInput extends MidiIo implements Receiver {

	/**
	 * Listeners (subscribes) to messages 
	 */
	private ArrayList<MidiInputListener> listeners;

	/**
	 * Initializes a MIDI input port via a MidiDevice instance
	 * @param device device that will transmit MIDI to us
	 */
	public MidiInput(MidiDevice device) throws MidiUnavailableException {
		this.device = device;
		this.device.getTransmitter().setReceiver(this);
		listeners = new ArrayList<MidiInputListener>(1);
	}

	/**
	 * Add a listener to the list
	 */
	public void addListener(MidiInputListener listener) {
		listeners.add(listener);
	}

	/**
	 * Invoked by MIDI transmitter (think of this as an "onMidiIn" type callback) 
	 */
	public void send(MidiMessage msg, long ts) {
		for (MidiInputListener listener : listeners) {
			listener.onMidiIn(this, msg, ts);
		}
	}

	public void close() { }

}
