import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import cc.atoi.inkfish.MidiInput;

/**
 * Elektron Monomachine JavaScript object
 * @author Adam Saponara
 * @todo WIP
 */
public class ElektronMonomachineObject extends ElektronObject {

	private static final long serialVersionUID = -190101740355215164L;
	
	public String getClassName() { return "ElektronMonomachineObject"; }

	protected byte getDeviceByte() { return (byte)0x03; }

	private Stack<Object[]> noteOff = (Stack<Object[]>)new Vector<Object[]>(64); /* @todo class instead of Object[] */
	
	private long tick = 0;
	
	public enum Constant {
		TRACK_1(0),
		TRACK_2(1),
		TRACK_3(2),
		TRACK_4(3),
		TRACK_5(4),
		TRACK_6(5),
		MACHINE_GND_GND(0),
		MACHINE_GND_SIN(1),
		MACHINE_GND_NOIS(2),
		MACHINE_SWAVE_SAW(4),
		MACHINE_SWAVE_PULS(5),
		MACHINE_SWAVE_ENS(14),
		MACHINE_SID_6581(3),
		MACHINE_DPRO_WAVE(6),
		MACHINE_DPRO_BBOX(7),
		MACHINE_DPRO_DDRW(32),
		MACHINE_DPRO_DENS(33),
		MACHINE_FM_STAT(8),
		MACHINE_FM_PAR(9),
		MACHINE_FM_DYN(10),
		MACHINE_VO_VO(6),
		MACHINE_FX_THRU(12),
		MACHINE_FX_REVERB(13),
		MACHINE_FX_CHORUS(15),
		MACHINE_FX_DYNAMIX(16),
		MACHINE_FX_RINGMOD(17),
		MACHINE_FX_PHASER(18),
		MACHINE_FX_FLANGER(19),
		OUTPUT_BUS_EF(4),
		OUTPUT_BUS_CD(2),
		OUTPUT_BUS_AB(1),
		INPUT_NEIGHBOR(0),
		INPUT_A(1),
		INPUT_B(2),
		INPUT_AB(3),
		INPUT_BUS_AB(4),
		INPUT_BUS_CD(5),
		INPUT_BUS_EF(6),
		GATEBOX_PARAM1(1),
		STATUS_PARAM1(1); /* @todo */
		public final int value;
		Constant(int v) {
		    value = v;
		}
	}
	
	public boolean jsFunction_loadPattern(int pattern) { return super.jsFunction_loadPattern(pattern); }
	public boolean jsFunction_loadKit(int kit) { return super.jsFunction_loadKit(kit); }
	public boolean jsFunction_assignMachine(int track, int machine, int init) { return super.jsFunction_assignMachine(track, machine, init); }
	public boolean jsFunction_setTempo(int bpm) { return super.jsFunction_setTempo(bpm); }
	public boolean jsFunction_requestPattern(int pattern) { return super.jsFunction_requestPattern(pattern); }
	public boolean jsFunction_setReverbParameter(int param, int value) { return super.jsFunction_setReverbParameter(param, value); }
	public boolean jsFunction_setStatusParameter(int param, int value) { return super.jsFunction_setStatusParameter(param, value); }

	public boolean jsFunction_trig(int track, int note, int vel, int length) {
		ShortMessage msg = new ShortMessage();
		try {
			msg.setMessage(ShortMessage.NOTE_ON | (byte)track, note, vel);
			elektronOut.send(msg);
			rememberNoteOff(track, note, tick + length);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
	}
	
	public boolean jsFunction_setRouting(int track, int bus, int inputs) {
		return send(new byte[]{
			0x5c,
			(byte)track,
			(byte)bus,
			(byte)inputs
		});
	}

	public void onMidiIn(MidiInput in, MidiMessage msg, long ts) {
		super.onMidiIn(in, msg, ts);
		if (in == elektronIn && msg.getStatus() == ShortMessage.TIMING_CLOCK) {
			tick += 1;
			while (noteOff.size() > 0 && ((Long)(((Object[])(noteOff.peek()))[0])).longValue() <= tick) {
				elektronOut.send(((ShortMessage)(((Object[])(noteOff.pop()))[1])));
			}
		}
	}

	private void rememberNoteOff(int track, int note, long tickOff) {
		ShortMessage off = new ShortMessage();
		try {
			off.setMessage(ShortMessage.NOTE_OFF | (byte)track, note, 0);
		}
		catch (InvalidMidiDataException e) {
			return;
		}
		int size = noteOff.size();
		Object[] offElement = new Object[]{new Long(tickOff), off};
		if (size < 1) {
			noteOff.push(offElement);
		}
		else {
			int insertIndex;
			for (insertIndex = size; insertIndex > 0; insertIndex--) {
				if (((Long)(((Object[])(noteOff.get(insertIndex)))[0])).longValue() >= tickOff) {
					break;
				}
			}
			noteOff.insertElementAt(offElement, insertIndex);
		}
	}
	
	/* 
	
	Monomachine doesn't accept patterns in pattern mode unfortunately ;(
	 
	public boolean jsFunction_sendPattern() {
		System.out.print("unpacked: "); dump(pattern);
		ArrayList<Byte> packed = pack(pattern, 0, pattern.length);
		System.out.print("packed: "); dump(packed);
		ArrayList<Byte> encoded = encode(packed, new int[]{0, packed.size()});
		System.out.print("encoded (to send): "); dump(encoded);

		int encodedSize = encoded.size();
		byte[] data = new byte[encodedSize + 15];
		System.arraycopy(SYSEX_START, 0, data, 0, SYSEX_START.length);
		data[4] = getDeviceByte();
		data[6] = 0x67;
		data[7] = 0x05;
		data[8] = 0x01;
		data[9] = originalPosition;
		for (int i = 0; i < encodedSize; i++) data[10 + i] = encoded.get(i);
		int checksum = calcChecksum(encoded, new int[]{0, encodedSize});
		int length = encodedSize + 5;
		int p = encodedSize + 10;
		data[p++] = (byte)((checksum & 0x00003f80) >> 7);
		data[p++] = (byte)(checksum & 0x0000007f);
		data[p++] = (byte)((length & 0x00003f80) >> 7);
		data[p++] = (byte)(length & 0x0000007f);
		data[p++] = (byte)0xf7;
		dump(data);

		SysexMessage msg = new SysexMessage();
		try {
			msg.setMessage(data, data.length);
			elektronOut.send(msg);
		}
		catch (InvalidMidiDataException e) {
			System.out.println("failed");
		}
		return true;
	}
	*/

	protected void onElektronSysexIn(byte[] data, long ts) {

		//data[0->4] elektron header
		//data[5] message type (used below)
		//data[6] version
		//data[7] revision
		//data[8] "original position of message"
		//data[n-4, n-5] checksum 
		//data[n-2, n-3] message length
		//data[n-1] end (0xf7)
		
		// Look for pattern dump
		if (data[5] == 0x67) {
			originalPosition = data[8];	
			System.out.print("encoded (raw): "); dump(data);
			byte[] decoded = decode(data, new int[]{9, data.length - 14});
			System.out.print("decoded: "); dump(decoded);
			//ArrayList<Byte> unpacked = unpack(decoded, 9, decoded.length - 14);
			ArrayList<Byte> unpacked = unpack(decoded);
			System.out.print("unpacked: "); dump(unpacked);
			pattern = new byte[unpacked.size() - 13];
			for (int i = 0; i < pattern.length; i++) pattern[i] = (byte)unpacked.get(i + 9);
		}
		// Look for status response
		else if (data[6] == 0x72) {
			// @todo status response
		}
		else {
			// @todo other messages?
		}
	}

}
