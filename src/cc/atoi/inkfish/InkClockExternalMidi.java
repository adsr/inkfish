package cc.atoi.inkfish;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * An external MIDI-based sequencer. This class listens for MIDI timecode
 * messages and triggers the appropriate events on the listener. 
 * @author Adam Saponara
 */
public class InkClockExternalMidi extends InkClockExternal implements MidiInputListener {

	/**
	 * The MIDI input on which we'll listen for MIDI timecode.
	 */
	private MidiInput input;
	
	/**
	 * Increments once per pulse (tick) within a single quartner note.
	 */
	private int pulse = 0;
	
	/**
	 * Numbers of pulses (ticks) per quarter note (PPQN).
	 */
	private int ppqn = 24;
	
	/**
	 * Sets up an external MIDI clock.
	 * @param listener	device that listens for sequencer events
	 * @param input		MIDI input device that will send MIDI timecode
	 * @param ppqn		how many pulses per quarter note the external sequencer
	 * 					is configured to send. 
	 */
	public InkClockExternalMidi(InkClockListener listener, MidiInput input, int ppqn) {
		super(listener);
		this.input = input;
		input.addListener(this);
		this.ppqn = ppqn;
	}

	/**
	 * Called when the MIDI input sends a MIDI message
	 */
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts) {

		byte[] mbytes = msg.getMessage();
		int stat = (int)(mbytes[0] & 0xFF);

		switch (stat) {
			case ShortMessage.START:
				isRunning = true;
				tick = 0;
				pulse = 0;
				listener.onStart(tick);
				listener.onQuarterNote(tick);
				break;
			case ShortMessage.STOP:
				isRunning = false;
				listener.onStop(tick);
				break;
			case ShortMessage.TIMING_CLOCK:
				if (!isRunning) break;
				listener.onTick(tick);
				pulse++;
				if (pulse == ppqn) {
					pulse = 0;
					listener.onQuarterNote(tick);
				}
				tick++;
				break;
			case ShortMessage.CONTINUE:
				isRunning = true;
				listener.onContinue(tick);
				break;
			case ShortMessage.CONTROL_CHANGE:
				if ((int)mbytes[1] == 0x7B) {
					// @todo is this a MIDI standard?
					// 0x7B is "All notes off"
					isRunning = false;
					listener.onStop(tick);
				}
				break;
		}
	}
}
