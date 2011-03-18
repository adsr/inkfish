package cc.atoi.inkfish;

import org.mozilla.javascript.*;
import java.util.concurrent.*;
import javax.sound.midi.*;

/**
 * Encapsulates MidiIo objects for use in user scripts
 * @author Adam Saponara
 */
public class MidiIoObject extends ScriptableObject implements Scriptable {
	
	/**
	 * Required for serialization
	 */
	private static final long serialVersionUID = 238270592527335642L;
	
	/**
	 * Thread pool for sending messages with a delay
	 */
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(64);
	
	/**
	 * The underlying MIDI device
	 */
	private MidiIo dev;
	
	public MidiIoObject() { }

	public String getClassName() { return "MidiIoObject"; }

	public void jsConstructor() { }

	/**
	 * Sets the underlying MIDI device
	 */
	public void setDevice(MidiIo dev) { this.dev = dev; }
	

	/**
	 * Send a MIDI message. Returns true on success.  
	 */
	public boolean jsFunction_write(int command, int channel, int data1, int data2, int msDelay, int msLength) {
		if (dev == null || !(dev instanceof MidiOutput)) {
			return false;
		}
		if (msDelay > 0) {
			scheduler.schedule(new DelayedMidiMessage((MidiOutput)this.dev, command, channel, data1, data2), msDelay, TimeUnit.MILLISECONDS);
		}
		else {
			try {
				((MidiOutput)dev).writeShort(command, channel, data1, data2);
			}
			catch (InvalidMidiDataException e) {
				return false;
			}
		}
		if (msLength > 0 && command == 0x90) {
			// Schedule note off event
			scheduler.schedule(new DelayedMidiMessage((MidiOutput)this.dev, 0x80, channel, data1, 0), msDelay + msLength, TimeUnit.MILLISECONDS);
		}
		return true;
	}
	
	
	/*
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

}

/**
 * Threaded MIDI message executor 
 * @author Adam Saponara
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