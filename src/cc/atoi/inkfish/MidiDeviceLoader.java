package cc.atoi.inkfish;

import javax.sound.midi.*;

/**
 * Convenience class for loading MIDI devices
 * @author Adam Saponara
 */
public class MidiDeviceLoader {

	private MidiDevice.Info[] infos;
	private static MidiDeviceLoader instance;

	public static MidiDeviceLoader getInstance() {
		if (instance == null) {
			instance = new MidiDeviceLoader();
		}
		return instance;
	}

	private MidiDeviceLoader() {
		refresh();
	}

	public void refresh() {
		infos = MidiSystem.getMidiDeviceInfo();
	}

	public void list() {
		list(false, false);
	}

	public void listTransmitters() {
		list(false, true);
	}

	public void listReceivers() {
		list(true, false);
	}

	public void list(boolean isReceiver, boolean isTransmitter) {

		for (int i = 0; i < infos.length; i++) {
			MidiDevice device;
			try {
				device = MidiSystem.getMidiDevice(infos[i]);
				int maxTransmitters = device.getMaxTransmitters();
				int maxReceivers = device.getMaxReceivers();
				if (!((!isReceiver || maxReceivers != 0) && (!isTransmitter || maxTransmitters != 0))) {
					continue;
				}
				System.out.format("%d. %s  in:%s  out:%s\n",
					i,
					infos[i].getName(),
					maxTransmitters == -1 ? "(unlimited)" : Integer.toString(maxTransmitters),
					maxReceivers == -1 ? "(unlimited)" : Integer.toString(maxReceivers)
				);
			}
			catch (MidiUnavailableException e) {
				continue;
			}
		}

	}

	public MidiDevice getMidiReceiverByName(String deviceName) {
		return getMidiDeviceByName(deviceName, true, false);
	}

	public MidiDevice getMidiTransmitterByName(String deviceName) {
		return getMidiDeviceByName(deviceName, false, true);
	}

	public MidiDevice getMidiDeviceByName(String deviceName) {
		return getMidiDeviceByName(deviceName, false, false);
	}

	public MidiDevice getMidiDeviceByName(String deviceName, boolean isReceiver, boolean isTransmitter) {

		for (int i = 0; i < infos.length; i++) {
			if (!deviceName.equals(infos[i].getName())) {
				continue;
			}
			return getMidiDeviceByNumber(i, isReceiver, isTransmitter);
		}

		return null;
	}

	public MidiDevice getMidiReceiverByNumber(int num) {
		return getMidiDeviceByNumber(num, true, false);
	}

	public MidiDevice getMidiTransmitterByNumber(int num) {
		return getMidiDeviceByNumber(num, false, true);
	}

	public MidiDevice getMidiDeviceByNumber(int num) {
		return getMidiDeviceByNumber(num, false, false);
	}

	public MidiDevice getMidiDeviceByNumber(int num, boolean isReceiver, boolean isTransmitter) {
		if (num >= infos.length) {
			return null;
		}
		try {
			MidiDevice device = MidiSystem.getMidiDevice(infos[num]);
			int maxReceivers = device.getMaxReceivers();
			int maxTransmitters = device.getMaxTransmitters();
			if ((!isReceiver || maxReceivers != 0) && (!isTransmitter || maxTransmitters != 0)) {
				device.open();
				return device;
			}
		}
		catch (MidiUnavailableException e) {
		}
		return null;
	}


}
