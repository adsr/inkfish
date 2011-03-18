package cc.atoi.inkfish;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;


public class InkClockExternalMidi extends InkClockExternal implements MidiInputListener {

	private MidiInput input;
	private int pulse = 0;
	private int ppqn = 24;

	public InkClockExternalMidi(InkClockListener listener, MidiInput input, int ppqn) {
		super(listener);
		this.input = input;
		input.addListener(this);
		this.ppqn = ppqn;
	}

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
					// 0x7B is "All notes off"
					isRunning = false;
					listener.onStop(tick);
				}
				break;
		}
	}
}
