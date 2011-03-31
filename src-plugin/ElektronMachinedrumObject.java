import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * Elektron Machinedrum JavaScript object
 * @todo WIP
 * @author Adam Saponara
 */
public class ElektronMachinedrumObject extends ElektronObject {

	private static final long serialVersionUID = 4395105075636947448L;
	
	public String getClassName() { return "ElektronMachinedrumObject"; }
	
	protected byte getDeviceByte() { return (byte)0x02; }

	/**
	 * Machinedrum related constants
	 */
	public enum Constant {
		TRACK_1(1),
		TRACK_2(2),
		TRACK_3(3),
		TRACK_4(4),
		TRACK_5(5),
		TRACK_6(6),
		TRACK_7(7),
		TRACK_8(8),
		TRACK_9(9),
		TRACK_10(10),
		TRACK_11(11),
		TRACK_12(12),
		TRACK_13(13),
		TRACK_14(14),
		TRACK_15(15),
		TRACK_16(16),
		MACHINE_GND_GND(0),
		MACHINE_GND_SIN(1),
		MACHINE_GND_NOIS(2),
		MACHINE_GND_IM(4),
		MACHINE_TRX_BD(16),
		MACHINE_TRX_SD(17),
		MACHINE_TRX_XT(18),
		MACHINE_TRX_CP(19),
		MACHINE_TRX_RS(20),
		MACHINE_TRX_CB(21),
		MACHINE_TRX_CH(22),
		MACHINE_TRX_OH(23),
		MACHINE_TRX_CY(24),
		MACHINE_TRX_MA(25),
		MACHINE_TRX_CL(26),
		MACHINE_TRX_XC(27),
		MACHINE_TRX_B2(28),
		MACHINE_EFM_BD(32),
		MACHINE_EFM_SD(33),
		MACHINE_EFM_XT(34),
		MACHINE_EFM_CP(35),
		MACHINE_EFM_RS(36),
		MACHINE_EFM_CB(37),
		MACHINE_EFM_HH(38),
		MACHINE_EFM_CY(39),
		MACHINE_E12_BD(48),
		MACHINE_E12_SD(49),
		MACHINE_E12_HT(50),
		MACHINE_E12_LT(51),
		MACHINE_E12_CP(52),
		MACHINE_E12_RS(53),
		MACHINE_E12_CB(54),
		MACHINE_E12_CH(55),
		MACHINE_E12_OH(56),
		MACHINE_E12_RC(57),
		MACHINE_E12_CC(58),
		MACHINE_E12_BR(59),
		MACHINE_E12_TA(60),
		MACHINE_E12_TR(61),
		MACHINE_E12_SH(62),
		MACHINE_E12_BC(63),
		MACHINE_PI_BD(64),
		MACHINE_PI_SD(65),
		MACHINE_PI_MT(66),
		MACHINE_PI_ML(67),
		MACHINE_PI_MA(68),
		MACHINE_PI_RS(69),
		MACHINE_PI_RC(70),
		MACHINE_PI_CC(71),
		MACHINE_PI_HH(72),
		MACHINE_INP_GA(80),
		MACHINE_INP_GB(81),
		MACHINE_INP_FA(82),
		MACHINE_INP_FB(83),
		MACHINE_INP_EA(84),
		MACHINE_INP_EB(85),
		MACHINE_MID_01(96),
		MACHINE_MID_02(97),
		MACHINE_MID_03(98),
		MACHINE_MID_04(99),
		MACHINE_MID_05(100),
		MACHINE_MID_06(101),
		MACHINE_MID_07(102),
		MACHINE_MID_08(103),
		MACHINE_MID_09(104),
		MACHINE_MID_10(105),
		MACHINE_MID_11(106),
		MACHINE_MID_12(107),
		MACHINE_MID_13(108),
		MACHINE_MID_14(109),
		MACHINE_MID_15(110),
		MACHINE_MID_16(111),
		MACHINE_CTR_AL(112),
		MACHINE_CTL_8P(113),
		MACHINE_CTL_RE(120),
		MACHINE_CTL_GB(121),
		MACHINE_CTL_EQ(122),
		MACHINE_CTL_DX(123),
		MACHINE_ROM_01(0),
		MACHINE_ROM_02(1),
		MACHINE_ROM_03(2),
		MACHINE_ROM_04(3),
		MACHINE_ROM_05(4),
		MACHINE_ROM_06(5),
		MACHINE_ROM_07(6),
		MACHINE_ROM_08(7),
		MACHINE_ROM_09(8),
		MACHINE_ROM_10(9),
		MACHINE_ROM_11(10),
		MACHINE_ROM_12(11),
		MACHINE_ROM_13(12),
		MACHINE_ROM_14(13),
		MACHINE_ROM_15(14),
		MACHINE_ROM_16(15),
		MACHINE_ROM_17(16),
		MACHINE_ROM_18(17),
		MACHINE_ROM_19(18),
		MACHINE_ROM_20(19),
		MACHINE_ROM_21(20),
		MACHINE_ROM_22(21),
		MACHINE_ROM_23(22),
		MACHINE_ROM_24(23),
		MACHINE_ROM_25(24),
		MACHINE_ROM_26(25),
		MACHINE_ROM_27(26),
		MACHINE_ROM_28(27),
		MACHINE_ROM_29(28),
		MACHINE_ROM_30(29),
		MACHINE_ROM_31(30),
		MACHINE_ROM_32(31),
		MACHINE_RAM_R1(32),
		MACHINE_RAM_R2(33),
		MACHINE_RAM_P1(34),
		MACHINE_RAM_P2(35),
		MACHINE_RAM_R3(37),
		MACHINE_RAM_R4(38),
		MACHINE_RAM_P3(39),
		MACHINE_RAM_P4(40),
		MACHINE_ROM_33(48),
		MACHINE_ROM_34(49),
		MACHINE_ROM_35(50),
		MACHINE_ROM_36(51),
		MACHINE_ROM_37(52),
		MACHINE_ROM_38(53),
		MACHINE_ROM_39(54),
		MACHINE_ROM_40(55),
		MACHINE_ROM_41(56),
		MACHINE_ROM_42(57),
		MACHINE_ROM_43(58),
		MACHINE_ROM_44(59),
		MACHINE_ROM_45(60),
		MACHINE_ROM_46(61),
		MACHINE_ROM_47(62),
		MACHINE_ROM_48(63),
		OUTPUT_BUS_A(0),
		OUTPUT_BUS_B(1),
		OUTPUT_BUS_C(2),
		OUTPUT_BUS_D(3),
		OUTPUT_BUS_E(4),
		OUTPUT_BUS_F(5),
		OUTPUT_BUS_MAIN(6);
		/* @todo finish these */
		public final int value;
		Constant(int v) {
		    value = v;
		}
	}
	
	public boolean jsFunction_loadPattern(int pattern) { return super.jsFunction_loadPattern(pattern); }
	public boolean jsFunction_loadKit(int kit) { return super.jsFunction_loadKit(kit); }
	public boolean jsFunction_assignMachine(int track, int machine, int init) { return super.jsFunction_assignMachine(track, machine, init); }
	public boolean jsFunction_setTempo(int bpm) { return super.jsFunction_setTempo(bpm); }
	public boolean jsFunction_setReverbParameter(int param, int value) { return super.jsFunction_setReverbParameter(param, value); }
	public boolean jsFunction_setStatusParameter(int param, int value) { return super.jsFunction_setStatusParameter(param, value); }
	public boolean jsFunction_requestPattern(int pattern) { return super.jsFunction_requestPattern(pattern); }
	
	public boolean jsFunction_setPitchBend(int bend) {
		// @todo
		return false;
	}
	
	public boolean jsFunction_trig(int track, int vel) {
		// @todo
		byte note;
		switch (track) {
			case 1: /* @todo Constant.TRACK_1.value */ note = 36; break;
			case 2: note = 38; break;
			case 3: note = 40; break;
			case 4: note = 41; break;
			case 5: note = 43; break;
			case 6: note = 45; break;
			case 7: note = 47; break;
			case 8: note = 48; break;
			case 9: note = 50; break;
			case 10: note = 52; break;
			case 11: note = 53; break;
			case 12: note = 55; break;
			case 13: note = 57; break;
			case 14: note = 59; break;
			case 15: note = 60; break;
			case 16: note = 62; break;
			default: return false;
		}
		ShortMessage msg = new ShortMessage();
		try {
			msg.setMessage(ShortMessage.NOTE_ON, note, vel);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
	}
	
	// @todo more JS constants needed
	// @todo rename send sysex
	// @todo impl send as convenience shortmessage send
	public boolean jsFunction_setLevel(int track, int level) {
		ShortMessage msg = new ShortMessage();
		int channelOffset = (int)Math.ceil((track - 1) / 4.0d);
		try {
			msg.setMessage(ShortMessage.CONTROL_CHANGE & (byte)(baseChannel + channelOffset), track, level);
		}
		catch (InvalidMidiDataException e) {
		}
		return true;
	}
	
	public boolean jsFunction_setMute(int track, int mute) {
		// @todo
		return false;
	}
	
	public boolean jsFunction_setParam(int track, int param, int value) {
		// @todo
		byte base;
		switch (track) {
		
		}
		return false;
	}
	
	public boolean jsFunction_setDelayParameter(int param, int value) {
		return send(new byte[]{
			0x5d,
			(byte)param,
			(byte)value
		});
	}

	public boolean jsFunction_setEQParameter(int param, int value) {
		return send(new byte[]{
			0x5f,
			(byte)param,
			(byte)value
		});
	}

	public boolean jsFunction_setCompressorParameter(int param, int value) {
		return send(new byte[]{
			0x60,
			(byte)param,
			(byte)value
		});
	}


	public boolean jsFunction_setRouting(int track, int bus) {
		return send(new byte[]{
			0x5c,
			(byte)track,
			(byte)bus
		});
	}

	protected void onElektronSysexIn(byte[] buffer, long ts) {
	
	}
	
}
