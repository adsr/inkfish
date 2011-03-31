import java.util.HashMap;

import cc.atoi.inkfish.*;

import javax.sound.midi.*;
import org.mozilla.javascript.*;

public class LaunchpadObject extends ScriptableObject implements Scriptable, MidiInputListener {
	
	private static final long serialVersionUID = -8113612231969362095L;

	/**
	 * Launchpad input
	 */
	protected MidiInput launchpadIn;

	/**
	 * Launchpad output
	 */
	protected MidiOutput launchpadOut;

	/**
	 * Hash of event handler function names to their functions.
	 * E.g., 'ongriddown' -> function ongriddown() { ... }
	 */
	protected HashMap<String, Function> funcMap;
	
	/**
	 * 2d array of booleans representing the up-down state of grid buttons on
	 * the Launchpad. True means down, false means up.
	 */
	protected boolean[][] btnGrid = new boolean[8][8];

	/**
	 * 1d array of booleans representing the up-down state of the user buttons
	 * on the Launchpad. True means down, false means up.
	 */
	protected boolean[] btnUser = new boolean[8];

	/**
	 * 1d array of booleans representing the up-down state of the scene
	 * buttons on the Launchpad. True means down, false means up.
	 */
	protected boolean[] btnScene = new boolean[8];
	
	/**
	 * Reference to scope of user scripts
	 */
	protected Scriptable jsScope;

	public LaunchpadObject() { }

	public void jsConstructor() { }

	public String getClassName() { return "LaunchpadObject"; }

	/**
	 * Launchpad related constants
	 */
	public enum Constant {
		COLOR_OFF(12),
		COLOR_RED_LO(13),
		COLOR_RED(15),
		COLOR_AMBER_LO(29),
		COLOR_AMBER(63),
		COLOR_YELLOW(62),
		COLOR_GREEN_LO(28),
		COLOR_GREEN(60),
		BUTTON_UP(0),
		BUTTON_DOWN(1),
		BUTTON_LEFT(2),
		BUTTON_RIGHT(3),
		BUTTON_SESSION(4),
		BUTTON_USER_1(5),
		BUTTON_USER_2(6),
		BUTTON_MIXER(7);
		public final int value;
		Constant(int v) {
		    value = v;
		}
	}
	
	/**
	 * Initializes the Launchpad JS object
	 * @param in Launchpad MIDi input
	 * @param out Launchpad MIDi output
	 * @param map map of event handlers to their functions in JS scope
	 * @param scope user script scope
	 */
	public void initialize(MidiInput in, MidiOutput out, HashMap<String, Function> map, Scriptable scope) {
		launchpadIn = in;
		launchpadOut = out;
		in.addListener(this);
		funcMap = map;
		jsScope = scope;
	}
	
	/**
	 * Calls the function named funcName in user script with arguments of args  
	 * @param funcName name of JS function to call 
	 * @param args arguments to pass in
	 */
	public void raiseEvent(String funcName, Object... args) {
		
		// Skip if the function is not defined
		Function func = funcMap.get(funcName);
		if (func == null) {
			return;
		}
		
		// Insert reference to self as first param
		Object[] fArgs = new Object[args.length + 1];
		fArgs[0] = this;
		System.arraycopy(args, 0, fArgs, 1, args.length);

		// Enter new context for this thread
		Context jsContext = Context.enter();
		
		// Call function with arguments including self as first param
		func.call(jsContext, jsScope, jsScope, fArgs);
	}
	
	/**
	 * Invoked when a message is received by a MIDI device
	 */
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts) {
		
		// Skip if the message is not from this Launchpad
		if (device != launchpadIn) {
			return;
		}
		
		// Parse MIDI bytes and invoke appropriate event handlers
		byte[] mbytes = msg.getMessage();
		int mvel  = (int)mbytes[2];
		int mkey, mx, my;

		switch ((int)mbytes[0] & 0xF0) {

			case ShortMessage.NOTE_ON:
				mkey = (int)mbytes[1];
				mx = mkey & 0x0F;
				my = (mkey & 0xF0) >> 4;
				if (mx < 8) {
					// Grid button
					if (mvel == 0) {
						btnGrid[my][mx] = false;
						raiseEvent("ongridup", my, mx, ts);
					}
					else {
						btnGrid[my][mx] = true;
						raiseEvent("ongriddown", my, mx, ts);
					}
				}
				else {
					// Scene button
					if (mvel == 0) {
						btnScene[my] = false;
						raiseEvent("onsceneup", my, ts);
					}
					else {
						btnScene[my] = true;
						raiseEvent("onscenedown", my, ts);
					}
				}
				break;

			case ShortMessage.CONTROL_CHANGE:
				mx = (int)mbytes[1] - 104;
				// User button
				if (mvel == 0) {
					btnUser[mx] = false;
					raiseEvent("onuserup", mx, ts);
				}
				else {
					btnUser[mx] = true;
					raiseEvent("onuserdown", mx, ts);
				}
				break;
		}
	}
	
	/**
	 * Sets the color of a button on the grid
	 */
	public boolean setGridColor(int row, int col, int color) {
		if (row < 0 || row > 7 || col < 0 || col > 8) return false; // allow col=8 for scene buttons
		try {
			launchpadOut.writeShort(ShortMessage.NOTE_ON, 0, 16 * row + col, color);
		}
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Sets the color of a user button
	 */
	public boolean setUserColor(int u, int color) {
		if (u < 0 || u > 7) return false;
		try {
			launchpadOut.writeShort(ShortMessage.CONTROL_CHANGE, 0, u + 104, color);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the color of a scene button
	 */
	public boolean setSceneColor(int s, int color) {
		return setGridColor(s, 8, color);
	}

	/**
	 * Returns true if the specified grid button is down
	 */
	public boolean isGridDown(int row, int col) {
		if (row < 0 || row > 7 || col < 0 || col > 7) return false;
		return btnGrid[row][col];
	}

	/**
	 * Returns true if the specified user button is down
	 */
	public boolean isUserDown(int u) {
		if (u < 0 || u > 7) return false;
		return btnUser[u];
	}

	/**
	 * Returns true if the specified scene button is down
	 */
	public boolean isSceneDown(int s) {
		if (s < 0 || s > 7) return false;
		return btnScene[s];
	}

	/**
	 * Sends a reset packet to the Launchpad
	 */
	public boolean clear() {
		try {
			launchpadOut.writeShort(ShortMessage.CONTROL_CHANGE, 0, 0, 0);
		}
		catch (InvalidMidiDataException e) {
			return false;
		}
		return true;
	}	
	
}
