package cc.atoi.inkfish;

import javax.sound.midi.*;

/**
 * Something that will listen to incoming MIDI messages from a MidiInput object
 * @author Adam Saponara
 */
public interface MidiInputListener {
	
	/**
	 * Invoked when msg is received by device at ts 
	 * @param device	MIDI input device
	 * @param msg		message sent
	 * @param ts		timestamp of message
	 */
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts);
	
}
