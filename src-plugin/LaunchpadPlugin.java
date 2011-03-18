import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.MidiMessage;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cc.atoi.inkfish.*;

/**
 * Novation Launchpad Inkfish plugin.
 * @todo WIP
 * @author Adam Saponara
 */
public class LaunchpadPlugin implements InkfishPlugin {

	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope) {
		// @todo
	}

}

/*
class LaunchpadObject extends ScriptableObject implements Scriptable, MidiInputListener {
	private static final long serialVersionUID = 238270592527335644L;
	public LaunchpadObject() { }
	public void jsConstructor() { }
	public String getClassName() { return "LaunchpadObject"; }
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts) {
		
	}
}
*/