package cc.atoi.inkfish;

import javax.sound.midi.*;

/**
 * MIDI Output port wrapper
 * @author Adam Saponara
 */
public class MidiOutput extends MidiIo {

	/**
	 * Device that we want to output MIDI to
	 */
	private Receiver receiver;

	/**
	 * Initializes a MIDI output port via a MidiDevice instance
	 * @param device device that we will transmit MIDI to
	 */
	public MidiOutput(MidiDevice device) throws MidiUnavailableException {
		this.device = device;
		receiver = this.device.getReceiver();
	}

	/**
	 * Send a MIDI message immediately.
	 */
	public void send(MidiMessage msg) {
		send(msg, -1);
	}
	
	/**
	 * Send a MIDI message with timestamp (only works if target device support
	 * timestamping).
	 */
	public void send(MidiMessage msg, long ts) {
		receiver.send(msg, ts);
	}

	/**
	 * Send a 3-byte ("short") MIDI message 
	 */
	public void writeShort(ShortMessage smsg) throws InvalidMidiDataException {
		receiver.send(smsg, -1);
	}

	
	/**
	 * Send a 3-byte ("short") MIDI message by specifying its values
	 * @param command	MIDI command / status (note on, note off, control change)
	 * @param channel	target MIDI channel
	 * @param data1		2nd message byte
	 * @param data2		3rd message byte
	 */
	public void writeShort(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
		ShortMessage smsg = new ShortMessage();
		smsg.setMessage(command, channel, data1, data2);
		writeShort(smsg);
	}

}
