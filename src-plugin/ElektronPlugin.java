import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.MidiMessage;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cc.atoi.inkfish.*;

public class ElektronPlugin implements InkfishPlugin {

	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope) {
		
	}

}

class ElektronObject extends ScriptableObject implements Scriptable, MidiInputListener {
	private static final long serialVersionUID = 238270592527335643L;
	public ElektronObject() { }
	public void jsConstructor() { }
	public String getClassName() { return "ElektronObject"; }
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts) {
		
	}
}