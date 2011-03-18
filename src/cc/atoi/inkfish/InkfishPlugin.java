package cc.atoi.inkfish;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.mozilla.javascript.*;

public interface InkfishPlugin {
	public void initialize(HashMap<String, ArrayList<String>> args, File root, Context jsContext, Scriptable jsScope);
}
