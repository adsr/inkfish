import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import cc.atoi.inkfish.*;

/**
 * Elektron Machinedrum and Monomachine plugins for Inkfish
 * @author Adam Saponara
 */
public class ElektronPlugin implements InkfishPlugin {

	/**
	 * Initializes Elektron devices in user JavaScript according to command
	 * line arguments.
	 */
	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope) {
		
		// Get reference to MIDI device loader
		MidiDeviceLoader midiLoader = MidiDeviceLoader.getInstance();

		// Monomachine
		if (args.containsKey("mnm")) {
			initializeDevice(ElektronMonomachineObject.class, args.get("mnm"), jsContext, jsScope, midiLoader);
		}

		// Machinedrum
		if (args.containsKey("md")) {
			// initializeDevice(ElektronMachinedrumObject.class, args.get("md"), jsContext, jsScope, midiLoader);
		}
		
	}
	
	/**
	 * Initializes an individual Elektron device in user JavaScript.
	 */
	protected boolean initializeDevice(Class<? extends ElektronObject> elektronClass, ArrayList<String> elektronDefs, Context jsContext, Scriptable jsScope, MidiDeviceLoader midiLoader) {
		
		// Define type in JS
		try {
			ScriptableObject.defineClass(jsScope, elektronClass);
		}
		catch (Exception e) {
			// @todo no Pokemon
			e.printStackTrace();
			return false;
		}
		
		// Define JS constants
		// @todo cleaner way to do this
		if (elektronClass == ElektronMonomachineObject.class) {
			for (ElektronMonomachineObject.Constant c : ElektronMonomachineObject.Constant.values()) {
				jsScope.put(c.toString(), jsScope, c.value);
			}
		}
		else if (elektronClass == ElektronMachinedrumObject.class) {
			for (ElektronMachinedrumObject.Constant c : ElektronMachinedrumObject.Constant.values()) {
				jsScope.put(c.toString(), jsScope, c.value);
			}
		}

		// For each machine defined on the command line...
		for (String elektronDef : elektronDefs) {
			
			String[] elektronParts = elektronDef.split(":", 3);
			
			// Make JS object
			//elektronClass.getName()
			//elektronClass.getSimpleName()
			//elektronClass.getCanonicalName()
			ElektronObject elektron = (ElektronObject)jsContext.newObject(jsScope, elektronClass.getSimpleName());
			try {
				elektron.setDevices(
					new MidiInput(midiLoader.getMidiDeviceByNumber(Integer.parseInt(elektronParts[0]))),
					new MidiOutput(midiLoader.getMidiDeviceByNumber(Integer.parseInt(elektronParts[1])))
				);
			}
			catch (Exception e) {
				// @todo no Pokemon
				e.printStackTrace();
				return false;
			}
			
			// Assign object to variable in JS scope
			jsScope.put(elektronParts[2], jsScope, elektron);
			
		}
		
		return true;

	}
	
}

