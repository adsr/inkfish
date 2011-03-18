package cc.atoi.inkfish;

import javax.sound.midi.*;

/*
 * Something that will listen to incoming MIDI messages from a MidiInput object
 */
public interface MidiInputListener {
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts);
}
