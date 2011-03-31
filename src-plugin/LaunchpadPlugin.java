import cc.atoi.inkfish.*;
import org.mozilla.javascript.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Novation Launchpad Inkfish plugin.
 * @author Adam Saponara
 */
public class LaunchpadPlugin implements InkfishPlugin {

	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope) {
		
		// Get reference to MIDI device loader
		MidiDeviceLoader midiLoader = MidiDeviceLoader.getInstance();

		// Skip if no launchpad arguments
		if (!args.containsKey("lp")) {
			return;
		}
		
		// Define type in JS
		try {
			ScriptableObject.defineClass(jsScope, LaunchpadObject.class);
		}
		catch (Exception e) {
			// @todo no Pokemon
			return;
		}
		
		// Write constants to JS
		for (LaunchpadObject.Constant c : LaunchpadObject.Constant.values()) {
			jsScope.put(c.toString(), jsScope, c.value);
		}
		
		// Get references to Launchpad event handler functions if they exist
		String[] funcs = new String[]{"ongriddown","ongridup","onuserdown","onuserup","onscenedown","onsceneup"};
		HashMap<String, Function> funcMap = new HashMap<String, Function>();
		Object func; 
		for (int i = 0; i < funcs.length; i++) {
			func = jsScope.get(funcs[i], jsScope);
			funcMap.put(funcs[i], (func instanceof Function) ? (Function)func : null);
		}
		
		
		// For each launchpad (lp) command line arg
		for (String launchpadDef : args.get("lp")) {
			
			String[] launchpadParts = launchpadDef.split(":", 3);

			// Init launchpad JS object with MIDI devices
			LaunchpadObject launchpad = (LaunchpadObject)jsContext.newObject(jsScope, "LaunchpadObject");
			try {
				launchpad.initialize(
					new MidiInput(midiLoader.getMidiDeviceByNumber(Integer.parseInt(launchpadParts[0]))),
					new MidiOutput(midiLoader.getMidiDeviceByNumber(Integer.parseInt(launchpadParts[1]))),
					funcMap,
					jsScope
				);
			}
			catch (Exception e) {
				// @todo no Pokemon
				continue;
			}
			
			// Assign object to variable in JS scope
			jsScope.put(launchpadParts[2], jsScope, launchpad);
			
		}

	}

}
