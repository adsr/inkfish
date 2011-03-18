package cc.atoi.inkfish;

import org.mozilla.javascript.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface that all Inkfish plugins must implement
 * @author Adam Saponara
 */
public interface InkfishPlugin {
	
	/**
	 * Invoked when plugin should initialize. This occurs once per at the
	 * beginning of Inkfish execution. 
	 * @param args		arguments from the command line in neat format
	 * @param root		root directory
	 * @param jsContext	user JavaScript runtime
	 * @param jsScope	top-level scope in user JavaScript
	 */
	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope);
}
