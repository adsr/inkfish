package cc.atoi.inkfish;

import javax.sound.midi.*;

/*
 * MIDI Output port wrapper
 */
public class MidiOutput extends MidiIo {

	private Receiver receiver;

	public MidiOutput(MidiDevice device) throws MidiUnavailableException {
		this.device = device;
		receiver = this.device.getReceiver();
	}

	public void send(MidiMessage msg) {
		send(msg, -1);
	}

	public void send(MidiMessage msg, long ts) {
		receiver.send(msg, ts);
	}

	public void writeShort(ShortMessage smsg) throws InvalidMidiDataException {
		receiver.send(smsg, -1);
	}

	public void writeShort(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
		ShortMessage smsg = new ShortMessage();
		smsg.setMessage(command, channel, data1, data2);
		receiver.send(smsg, -1);
	}

}
