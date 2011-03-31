import java.util.ArrayList;

import javax.sound.midi.*;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cc.atoi.inkfish.*;

abstract public class ElektronObject extends ScriptableObject implements Scriptable, MidiInputListener {

	private static final long serialVersionUID = 8140337288398952658L;
	protected static final byte[] SYSEX_START = {(byte)0xf0, 0x00, 0x20, 0x3c, 0x00, 0x00};
	protected static final byte[] SYSEX_END = {(byte)0xf7};
	
	protected MidiInput elektronIn;
	protected MidiOutput elektronOut;
	protected byte[] pattern;
	protected byte originalPosition;
	protected byte baseChannel = 0;

	abstract protected byte getDeviceByte();
	abstract protected void onElektronSysexIn(byte[] buffer, long ts); 
	abstract public String getClassName();

	public ElektronObject() { }
	
	public void jsConstructor() { }

	public void initialize(MidiInput in, MidiOutput out) {
		elektronIn = in;
		elektronOut = out;
		in.addListener(this);
	}

	public void onMidiIn(MidiInput in, MidiMessage msg, long ts) {
		
		// Return if not from Elektron device
		if (in != elektronIn) {
			return;
		}
		
		int status = msg.getStatus();
		if (status != ShortMessage.TIMING_CLOCK) {
		
			/*
			if (status == ShortMessage.STOP) {
				System.exit(0);
			}
			*/
	
			// Look for SysexMessage
			if (status == SysexMessage.SYSTEM_EXCLUSIVE) {
				
				byte[] data = ((SysexMessage)msg).getData();
				
				// Look for Elektron header and footer
				if (   data[0] == SYSEX_START[1]
					&& data[1] == SYSEX_START[2]
					&& data[2] == SYSEX_START[3]
					&& data[3] == getDeviceByte()
					&& data[4] == SYSEX_START[5]
					&& data[data.length - 1] == SYSEX_END[0]) {
					
					onElektronSysexIn(data, ts);
					
					
				}

			}
			
		}
		
	}
	
	public boolean jsFunction_loadPattern(int pattern) {
		return send(new byte[]{
			0x57,
			(byte)pattern
		});
	}
	
	public boolean jsFunction_loadKit(int kit) {
		return send(new byte[]{
			0x58,
			(byte)kit
		});
	}
	
	public boolean jsFunction_assignMachine(int track, int machine, int init) {
		return send(new byte[]{
			0x5b,
			(byte)(track % 6),
			(byte)(machine % 128),
			(byte)(init % 2)
		});
	}

	public boolean jsFunction_setReverbParameter(int param, int value) {
		return send(new byte[]{
			0x5e,
			(byte)param,
			(byte)value
		});
	}
	
	public boolean jsFunction_setStatusParameter(int param, int value) {
		return send(new byte[]{
			0x71,
			(byte)param,
			(byte)value
		});
	}

	public boolean jsFunction_setTempo(int bpm) {
		short sbpm = (short)(Math.min(Math.max(bpm, 30), 300) * 24);
		return send(new byte[]{
			0x61,
			(byte)(((sbpm << 1) & 0xff00) >> 8),
			(byte)(sbpm & 0x007f)
		});		
	}

	public boolean jsFunction_requestPattern(int pattern) {
		return send(new byte[]{
			0x68,
			(byte)pattern
		});
	}
	
	public void jsFunction_trig() { }
	
	protected boolean send(byte[] bytes) {
		
		int msgLength = bytes.length + SYSEX_START.length + SYSEX_END.length;
		byte[] msgBytes = new byte[msgLength];
		System.arraycopy(SYSEX_START, 0, msgBytes, 0, SYSEX_START.length);
		msgBytes[4] = getDeviceByte();
		System.arraycopy(bytes, 0, msgBytes, SYSEX_START.length, bytes.length);
		System.arraycopy(SYSEX_END, 0, msgBytes, SYSEX_START.length + bytes.length, SYSEX_END.length);
		
		SysexMessage msg = new SysexMessage();
		try {
			msg.setMessage(msgBytes, msgLength);
			elektronOut.send(msg);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
		
	}
	
	protected ArrayList<Byte> pack(byte[] buffer, int offset, int length) {
		
		ArrayList<Byte> packed = new ArrayList<Byte>(length);
		int end = offset + length;
		for (int i = offset, j, nextj; i < end; i++) {
			j = i;
			nextj = j + 1;
			while (nextj < buffer.length && buffer[i] == buffer[nextj] && (nextj - i) < 0x7f) {
				j = nextj;
				nextj += 1;
			}
			if (j != i) {
				packed.add((byte)(0x80 + (byte)(j - i + 1)));
			}
			packed.add(buffer[i]);
			i = j;
		}
		
		return packed;
	}
	
	protected ArrayList<Byte> unpack(byte[] buffer) {
		
		// Initialize expandable array of bytes to store unpacked bytes
		ArrayList<Byte> unpacked = new ArrayList<Byte>(buffer.length * 25);
		int repeatCount = 0;
		for (int i = 0; i < buffer.length; i++) {
			if (repeatCount <= 0) {
				if ((buffer[i] & 0x80) != 0 && (buffer[i] & 0x7f) > 1) {
					repeatCount = buffer[i] & 0x7f;
				}
				else {
					unpacked.add(buffer[i]);
				}
			}
			else {
				while (--repeatCount >= 0) {
					unpacked.add(buffer[i]);
				}
			}
		}
		
		return unpacked;

	}
	
	protected int calcChecksum(ArrayList<Byte> buffer, int[] checksumRanges) {
		int checksum = 0;
		for (int i = 0, r = 0, remainingInRange = 0, l = buffer.size(); i < l; i++) {
			if (remainingInRange < 1 && r < checksumRanges.length && i == checksumRanges[r]) {
				remainingInRange = checksumRanges[r + 1];
				r += 2;
			}
			if (remainingInRange > 0) {
				checksum += buffer.get(i);
			}
		}
		
		// Return lower 14 bits of checksum
		return 0x3FFF & checksum;
	}

	protected ArrayList<Byte> encode(ArrayList<Byte> buffer, int[] sevenBitRanges) {
		
		// Calculate overall length of 7bit ranges
		int length = 0;
		for (int i = 1; i < sevenBitRanges.length; i += 2) {
			length += sevenBitRanges[i];
		}

		// We now know exactly how long our final byte array will be
		int bufferLen = buffer.size();
		ArrayList<Byte> bytes7 = new ArrayList<Byte>(bufferLen);

		// Encode buffer, applying 8-to-7bit encoding where applicable
		byte msb = 0;
		for (int i = 0, j = 0, r = 0, rangeOffset = 0, remainingInRange = 0; i < bufferLen; i++) {
			if (remainingInRange < 1 && r < sevenBitRanges.length && i == sevenBitRanges[r]) {
				rangeOffset = 0;
				remainingInRange = sevenBitRanges[r + 1];
				r += 2;
			}
			if (remainingInRange < 1) {
				bytes7.add(i < bufferLen ? buffer.get(i) : 0);
			}
			else {
				if (rangeOffset++ % 7 == 0) {
					msb = 0;
					for (int k = 0; k < 7; k++) {
						msb |= ((i + k < bufferLen ? buffer.get(i + k) : 0) & 0x80) >> (k + 1);
					}
					bytes7.add(msb);
				}
				bytes7.add((byte)((i < bufferLen ? (byte)buffer.get(i) : 0) & 0x7f));
				remainingInRange -= 1;
			}
		}

		return bytes7;
	}

	/**
	 * 
	 * @param buffer
	 * @param sevenBitRanges an array of <offset,length> pairs representing
	 *                       7bit ranges in the buffer. Pairs must be sorted
     *                       in ascending offset fashion.
	 * @param checksum
	 * @return
	 */
	protected byte[] decode(byte[] buffer, int[] sevenBitRanges) {
		
		// Calculate overall length of 7bit ranges
		int length = 0;
		for (int i = 1; i < sevenBitRanges.length; i += 2) {
			length += sevenBitRanges[i];
		}

		// We now know exactly how long our final byte array will be
		byte[] bytes8 = new byte[buffer.length - (int)Math.ceil(length / 8.0f)];

		// Decode buffer, applying 7-to-8bit decoding where applicable
		byte msb = 0;
		byte bitNum = 0;
		for (int i = 0, j = 0, r = 0, rangeOffset = 0, remainingInRange = 0; i < buffer.length; i++) {
			if (remainingInRange < 1 && r < sevenBitRanges.length && i == sevenBitRanges[r]) {
				rangeOffset = 0;
				remainingInRange = sevenBitRanges[r + 1];
				r += 2;
			}
			if (remainingInRange < 1) {
				bytes8[j++] = buffer[i];
			}
			else {
				bitNum = (byte)(rangeOffset++ % 8);
				if (bitNum == 0) {
					msb = buffer[i];
				}
				else {
					bytes8[j++] = (byte)(buffer[i] | ((msb & (1 << (7 - bitNum))) << bitNum));
				}
				remainingInRange -= 1;
			}
		}
		
		return bytes8;
		
	}

	public void dump(byte[] b) {
		dump(b, false);
	}
	
	public void dump(byte[] b, boolean offsets) {
		StringBuilder rep = new StringBuilder(b.length * 3);
		System.out.println("length: " + b.length);
		for (int i = 0; i < b.length; i++) {
			if (offsets) System.out.print(String.format("%02x: ", new Object[]{i}));
			System.out.print(String.format("%02x" + (offsets ? "\n" : " "), new Object[] { Byte.valueOf(b[i]) }));
		}
		System.out.println();
	}

	public void dump(ArrayList<Byte> b) {
		dump(b, false);
	}
	public void dump(ArrayList<Byte> b, boolean offsets) {
		byte[] c = new byte[b.size()];
		for (int i = 0; i < c.length; i++) c[i] = b.get(i); 
		dump(c, offsets);
	}
	
}
