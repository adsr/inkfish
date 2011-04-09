package cc.atoi.inkfish;

import org.mozilla.javascript.*;

import java.util.LinkedList;
import java.util.concurrent.*;
import javax.sound.midi.*;

/**
 * Encapsulates MidiIo objects for use in user scripts
 * @author Adam Saponara
 */
public class MidiIoObject extends ScriptableObject implements Scriptable, InkClockListener {
	
	/**
	 * Required for serialization
	 */
	private static final long serialVersionUID = 238270592527335642L;
	
	/**
	 * Thread pool for sending messages with a delay
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(64);
	
	/**
	 * The current sequencer tick
	 */
	private long tick;
	
	/**
	 * The underlying MIDI device
	 */
	private MidiIo dev;
	
	/**
	 * List of MIDI messages to be sent at specific tick values in the future.
	 * This list is guaranteed to be sorted from lowest tick value to highest.
	 */
	private LinkedList<ScheduledMidiMessage> noteQueue = new LinkedList();
	
	public MidiIoObject() { }

	public String getClassName() { return "MidiIoObject"; }

	public void jsConstructor() { }

	/**
	 * Initializes the MidiIoObject. Sets the underlying MIDI device
	 * and listens to MidiInput from sequencer.
	 */
	public void initialize(MidiIo dev, InkClock clock) {
		this.dev = dev;
		clock.addListener(this);
	}
	

	/**
	 * Send a MIDI message. Returns true on success.  
	 */
	public boolean jsFunction_write(int command, int channel, int data1, int data2, int tickLength, int msDelay, int msLength) {
		
		// Skip if we are not a MidiOutput device
		if (dev == null || !(dev instanceof MidiOutput)) {
			return false;
		}
		
		// If this is a delayed message, schedule it
		if (msDelay > 0) {
			scheduler.schedule(new DelayedMidiMessage((MidiOutput)this.dev, command, channel, data1, data2), msDelay, TimeUnit.MILLISECONDS);
		}
		// Otherwise, write message to MidiOutput device
		else {
			try {
				((MidiOutput)dev).writeShort(command, channel, data1, data2);
			}
			catch (InvalidMidiDataException e) {
				return false;
			}
		}

		// If this was a NOTE_ON message, try to schedule a NOTE_OFF event in future
		if (command == ShortMessage.NOTE_ON) {
			// Schedule NOTE_OFF tickLength sequencer ticks in the future
			if (tickLength > 0) {
				// Find index to sort
				long offTick = this.tick + tickLength;
				int insertIndex, l = noteQueue.size(); 
				for (insertIndex = 0; insertIndex < l; insertIndex++) {
					if (noteQueue.get(insertIndex).getTick() >= offTick) {
						break;
					}
				}
				// Insert scheduled message in queue
				noteQueue.add(insertIndex, new ScheduledMidiMessage(offTick, ShortMessage.NOTE_OFF, channel, data1, 0));
			}
			// Schedule NOTE_OFF msLength milliseconds in the future
			else if (msLength > 0) {
				scheduler.schedule(new DelayedMidiMessage((MidiOutput)this.dev, ShortMessage.NOTE_OFF, channel, data1, 0), msDelay + msLength, TimeUnit.MILLISECONDS);
			}
		}
		return true;
	}
	
	
	/* @todo sysex function
	public static boolean jsFunction_sysex(Context cx, Scriptable self, Object[] data, Function func) {
		if (dev == null || !(dev instanceof MidiOutput)) {
			return false;
		}
		try {
			SysexMessage sm = new SysexMessage();
			sm.setMessage((byte[])data, data.length);
			((MidiOutput)dev).send(sm);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
	}
	*/
	
	/**
	 * Returns name of underlying MIDI device
	 */
	public String jsFunction_getDeviceName() {
		return dev.getName();
	}

	/**
	 * Invoked on sequencer tick event
	 */
	public void onTick(long tick) {
		
		// Update tick value
		this.tick = tick;
		
		// Continually pop notes off the queue and send them off if they are due
		// Note: This can technically benefit from synchronized code but I'm willing
		//       to sacrifice 100% reliability for simplicity, speed, and not
		//       holding up other sequencing events.
		while (!noteQueue.isEmpty() && noteQueue.getFirst().getTick() <= tick) {
			try {
				((MidiOutput)dev).writeShort(noteQueue.removeFirst().toShortMessage());
			}
			catch (InvalidMidiDataException e) {
				continue;
			}
		}
		
	}

	public void onStop(long tick) { }
	public void onStart(long tick) { }
	public void onContinue(long tick) { }
	public void onQuarterNote(long tick) { }

}

/**
 * Simple struct of a MIDI message that is meant to be sent at a certain tick 
 */
class ScheduledMidiMessage {
	
	private long tick;
	private int command;
	private int channel;
	private int data1;
	private int data2;
	
	public ScheduledMidiMessage(long tick, int command, int channel, int data1, int data2) {
		this.tick = tick;
		this.command = command;
		this.channel = channel;
		this.data1 = data1;
		this.data2 = data2;
	}
	
	public long getTick() {
		return this.tick;
	}	
	
	public ShortMessage toShortMessage() throws InvalidMidiDataException {
		ShortMessage m = new ShortMessage();
		m.setMessage(this.command, this.channel, this.data1, this.data2);
		return m;
	}
	
}


/**
 * Threaded MIDI message executor 
 */
class DelayedMidiMessage implements Runnable {
	
	private MidiOutput output;
	private int command;
	private int channel;
	private int data1;
	private int data2;

	public DelayedMidiMessage(MidiOutput output, int command, int channel, int data1, int data2) {
		this.output = output;
		this.command = command;
		this.channel = channel;
		this.data1 = data1;
		this.data2 = data2;
	}

	public void run() {
		try {
			output.writeShort(command, channel, data1, data2);
		}
		catch (InvalidMidiDataException e) {
		}
	}
	
}