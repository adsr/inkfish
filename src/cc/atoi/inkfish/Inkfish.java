package cc.atoi.inkfish;

import javax.sound.midi.*;
import org.mozilla.javascript.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Extensible JavaScriptable MIDI sequencer
 * @author Adam Saponara
 */
public class Inkfish implements InkClockListener, MidiInputListener {

	/**
	 * Number of ticks or pulses per quarter note.
	 */
	protected int ppqn;
	
	/**
	 * Device that will send us sequencing events. Null if using internal
	 * sequencing mode.
	 */
	protected MidiInput sequencer;
	
	/**
	 * Device or program that will send us clock events (start, stop, etc.)
	 */
	protected InkClock clock;
	
	/**
	 * Incrementing tick value
	 */
	protected long tick;

	/**
	 * Map of MIDI input aliases and their device representations. The keys of
	 * this map become variables in user JavaScript.
	 */
	protected HashMap<String, MidiInput> midiIns;

	/**
	 * Map of MIDI output aliases and their device representations. The keys of
	 * this map become variables in user JavaScript.
	 */
	protected HashMap<String, MidiOutput> midiOuts;
	
	/**
	 * The "ontick" function in user JavaScript will be called every N ticks
	 * where N is jsDivisor.
	 */
	protected int jsDivisor;
	
	/**
	 * Runtime context of user JavaScript
	 */
	protected Context jsContext;
	
	/**
	 * Top-level scope of user JavaScript
	 */
	protected Scriptable jsScope;
	
	/**
	 * Reference to "onmidiin" function in user JavaScript 
	 */
	protected Function jsMidiInFunc;

	/**
	 * Reference to "ontick" function in user JavaScript 
	 */
	protected Function jsTickFunc;
	
	/**
	 * Array of all MIDI Output JavaScripts objects
	 * @todo needed?
	 */
	protected MidiIoObject[] jsMidiOuts;
	
	/**
	 * Directory to search for user JavaScript and other resources
	 */
	protected File trackDir;

	/**
	 * Sole constructor. Sets up Inkfish environment according to params and
	 * also starts the playback clock.
	 * 
	 * @param params a hash of params from the command line
	 * @throws MidiUnavailableException
	 * @throws IOException
	 */
	protected Inkfish(HashMap<String, ArrayList<String>> params) throws MidiUnavailableException, IOException {

		// Get MidiDeviceLoader instance
		MidiDeviceLoader midiLoader = MidiDeviceLoader.getInstance();

		// Set working directory (default to ".")
		this.trackDir = new File(params.containsKey("dir") ? params.get("dir").get(0) : ".");
		if (!this.trackDir.isDirectory() || !this.trackDir.canRead()) {
			System.err.println("Unable to set working directory to " + this.trackDir.getAbsolutePath() + "; must be a readable directory");
			System.exit(1);
		}
		
		// Set ppqn (default to 24)
		this.ppqn = params.containsKey("ppqn") ? Integer.parseInt(params.get("ppqn").get(0)) : 24;
		
		// Set sequencer
		if (params.containsKey("seq")) {
			// External sequencer (sync to MIDI input)
			String seqArg = params.get("seq").get(0);
			MidiDevice device = seqArg.matches("^\\d+$")
				? midiLoader.getMidiTransmitterByNumber((new Integer(seqArg)).intValue())
				: midiLoader.getMidiTransmitterByName(seqArg);
			if (device == null) {
				System.err.println("Unable to open MIDI device " + seqArg + " as sequencer");
				System.exit(1);
			}
			else {
				clock = new InkClockExternalMidi(this, new MidiInput(device), ppqn);
				sequencer = new MidiInput(device);
			}
			
		}
		else {
			// Internal sequencer (default to 21 ms delay)
			this.clock = new InkClockInternalThread(this, params.containsKey("delay") ? Integer.parseInt(params.get("delay").get(0)) : 21, ppqn);
		}
		
		// Set jsDivisor (default to 6)
		this.jsDivisor = params.containsKey("divisor") ? Integer.parseInt(params.get("divisor").get(0)) : 6;

		// Instantiate MIDI devices
		this.midiIns = new HashMap<String, MidiInput>();
		this.midiOuts = new HashMap<String, MidiOutput>();
		if (params.containsKey("in")) {
			loadMidiIo(params.get("in"), this.midiIns, true, midiLoader);
		}
		if (params.containsKey("out")) {
			loadMidiIo(params.get("out"), this.midiOuts, false, midiLoader);
		}

		// Listen to midiIns
		for (String name : midiIns.keySet()) {
			MidiInput midiIn = midiIns.get(name);
			midiIn.addListener(this);
		}

		// Set tick to 0
		this.tick = 0;
		
		// Setup JavaScript environment
		try {

			// Init JavaScript proto objects
			this.jsContext = Context.enter();
			this.jsScope = this.jsContext.initStandardObjects();
			ScriptableObject.defineClass(jsScope, MidiIoObject.class);

			// Scan for plugins
			loadPlugins(params);

			// Make MidiOut JavaScript objects
			int iJsMidiOut = 0;
			this.jsMidiOuts = new MidiIoObject[midiOuts.size()];
			for (String name : midiOuts.keySet()) {
				MidiIoObject jsMidiOut = (MidiIoObject)this.jsContext.newObject(jsScope, "MidiIoObject");
				jsMidiOut.setDevice(midiOuts.get(name));
				jsScope.put(name, jsScope, jsMidiOut);
				jsMidiOuts[iJsMidiOut++] = jsMidiOut;
			}

			// Make MidiIn JavaScript objects
			for (String name : midiIns.keySet()) {
				MidiIoObject jsMidiIn = (MidiIoObject)this.jsContext.newObject(jsScope, "MidiIoObject");
				jsMidiIn.setDevice(midiIns.get(name));
				jsScope.put(name, jsScope, jsMidiIn);
			}

			// Make 'out' object for out.println etc
			Object jsOut = Context.javaToJS(System.out, jsScope);
			ScriptableObject.putProperty(jsScope, "out", jsOut);

			// Parse trackDir/*.js as user scripts
			BufferedInputStream fin;
			byte[] buffer;
			File[] scripts;
			scripts = trackDir.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return !file.isDirectory() && file.getName().toLowerCase().endsWith(".js");
				}
			});
			for (int i = 0; i < scripts.length; i++) {
				buffer = new byte[(int)scripts[i].length()];
				fin = new BufferedInputStream(new FileInputStream(scripts[i]));
				fin.read(buffer);
				Object result = this.jsContext.evaluateString(jsScope, new String(buffer), scripts[i].getName(), 1, null);
				fin.close();
			}

			// Get reference to onmidiin function if it exists
			Object func = this.jsScope.get("onmidiin", jsScope);
			this.jsMidiInFunc = (func instanceof Function) ? (Function)func : null;

			// Get reference to ontick function if it exists
			func = this.jsScope.get("ontick", jsScope);
			this.jsTickFunc = (func instanceof Function) ? (Function)func : null;

			// Start the clock if we are using an internal clock
			if (this.clock instanceof InkClockInternalThread) {
				this.clock.play();
			}

		}
		catch (RhinoException e) {
			System.err.println("js> " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Loads a list of MIDI devices defined by the elements of ioDefs into
	 * the midiIos hash
	 * 
	 * @param ioDefs		list of MIDI device descriptor in the form of 
	 *						#:alias or devicename:alias
	 * @param midiIos		hash of MidiIo objects to be populated
	 * @param isIn			a flag indicating whether these are MidiIn or 
	 *						MidiOut devices
	 * @param midiLoader	a MidiDeviceLoader instance
	 */
	protected void loadMidiIo(ArrayList<String> ioDefs, HashMap midiIos, boolean isIn, MidiDeviceLoader midiLoader) throws IllegalArgumentException, MidiUnavailableException {
		Iterator<String> iter = ioDefs.iterator();
		while (iter.hasNext()) {
			String argNext = iter.next();
			if (!argNext.contains(":")) {
				throw new IllegalArgumentException("Invalid midi parameter " + argNext + "; expected format (#|device):alias");
			}
			String[] deviceAlias = argNext.split(":", 2);
			MidiIo io = null;
			MidiDevice device = null;
			if (isIn) {
				device = deviceAlias[0].matches("^\\d+$")
					? midiLoader.getMidiTransmitterByNumber((new Integer(deviceAlias[0])).intValue())
					: midiLoader.getMidiTransmitterByName(deviceAlias[0]);
				if (device != null) io = new MidiInput(device);
			}
			else {
				device = deviceAlias[0].matches("^\\d+$")
					? midiLoader.getMidiReceiverByNumber((new Integer(deviceAlias[0])).intValue())
					: midiLoader.getMidiReceiverByName(deviceAlias[0]);
				if (device != null) io = new MidiOutput(device);
			}
			if (io == null) {
				System.err.println("Unable to open MIDI device " + deviceAlias[0] + " as " + (isIn ? "input" : "output"));
				System.exit(1);
			}
			midiIos.put((String)deviceAlias[1], isIn ? (MidiInput)io : (MidiOutput)io);
		}
	}

	/**
	 * Loads all Inkfish plugins available in the 'plugins' directory relative
	 * to the working directory
	 * 
	 * @param params a hash of params from the command line
	 * @param jsContext Rhino context
	 * @param jsScope Rhino scope
	 * @return an array of loaded Inkfish plugins
	 */
	protected void loadPlugins(HashMap<String, ArrayList<String>> params) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		// Load up *.js and *.gif
		File root = this.trackDir;
		File dir = new File("plugins");
		File[] plugins = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().toLowerCase().endsWith(".jar");
			}
		});
		if (plugins == null) {
			System.err.println("Local 'plugins' directory not found at " + dir.getAbsolutePath() + "; no plugins will be loaded");
			return;
		}
		URL[] urls = new URL[plugins.length];
		String[] pluginClasses = new String[plugins.length];
		for (int i = 0; i < plugins.length; i++) {
			urls[i] = plugins[i].toURI().toURL();
			pluginClasses[i] = new JarFile(plugins[i]).getManifest().getMainAttributes().getValue("Plugin-Class");
		}
		URLClassLoader classLoader = new URLClassLoader(urls);
		ArrayList<InkfishPlugin> pluginInstances = new ArrayList<InkfishPlugin>();
		for (int i = 0; i < pluginClasses.length; i++) {
			Class pluginClass = Class.forName(pluginClasses[i], true, classLoader);
			if (InkfishPlugin.class.isAssignableFrom(pluginClass)) {
				InkfishPlugin pluginInstance = (InkfishPlugin)pluginClass.newInstance();
				pluginInstance.initialize(params, root, jsContext, jsScope);
				pluginInstances.add(pluginInstance);
			}
		}
		
	}

	/**
	 * Returns the string alias of a MIDI device if available
	 * 
	 * @param i a MIDI device
	 * @return the string alias for the MIDI device
	 */
	protected String aliasForMidiInput(MidiInput i) {
		for (String alias : midiIns.keySet()) {
			if (midiIns.get(alias) == i) {
				return alias;
			}
		}
		return null;
	}

	/**
	 * Invoked when a message is received by a MIDI device
	 */
	public void onMidiIn(MidiInput device, MidiMessage msg, long ts) {
		forwardMidiToJs(device, msg, ts);
	}

	/**
	 * Invoked during playback for each sequencer tick
	 */
	public void onTick(long tick) {
		this.tick = tick;
		forwardTickToJs(tick);
	}

	/**
	 * Invoked during playback for each quarter note (defined by ppqn)
	 */
	public void onQuarterNote(long tick) { }

	/**
	 * Invoked when playback on the sequencer is stopped
	 */
	public void onStop(long tick) { }

	/**
	 * Invoked when playback on the sequencer is started
	 */
	public void onStart(long tick) { }

	/**
	 * Invoked when playback on the sequencer is resumed
	 */
	public void onContinue(long tick) { }

	/**
	 * Invokes the 'onmidiin' function in JavaScript if it is declared,
	 * passing in the name of the receiving device and the message bytes
	 * 
	 * @param device the receiving MIDI device
	 * @param msg the MIDI message received
	 * @param ts the timestamp at which the message was received
	 */
	protected void forwardMidiToJs(MidiInput device, MidiMessage msg, long ts) {
		if (jsMidiInFunc == null) {
			return;
		}
		jsContext = Context.enter();
		byte[] bytes = msg.getMessage();
		Object args[] = { aliasForMidiInput(device), bytes[0] & 0xF0, bytes[0] & 0x0F, bytes.length > 0 ? bytes[1] : null, bytes.length > 1 ? bytes[2] : null, ts };
		jsMidiInFunc.call(jsContext, jsScope, jsScope, args);
	}

	/**
	 * Invokes the 'ontick' function in JavaScript if it is declared for each
	 * tick where tick % jsDivisor is 0
	 * 
	 * @param tick the current tick value
	 */
	protected void forwardTickToJs(long tick) {
		if (jsTickFunc == null || tick % jsDivisor != 0) {
			return;
		}
		jsContext = Context.enter();
		Object args[] = { tick / jsDivisor };
		jsTickFunc.call(jsContext, jsScope, jsScope, args);
	}

	/**
	 * Prints command line argument help
	 */
	public static void help() {
		System.out.println(
			  "Usage:\n"
			+ "java Inkfish [ --help\n"
			+ "             | --list\n"
			+ "             | --dir --in* --out* --seq? --ppqn? --delay? --divisor? ]\n\n"
			+ "--help      Prints this help message.\n"
			+ "--list      Lists MIDI devices by number and exits.\n"
			+ "--dir       Sets the working directory. (Inkfish will look for and .js files\n"
			+ "            in this directory.)\n"
			+ "--in        Makes a reference to a MIDI input device by name or number. The\n"
			+ "            format for this switch is \"(#|name):alias\".\n"
			+ "--out       Same as --in except for MIDI output devices.\n"
			+ "--seq       Sets an external sequencer source. If not specified, the internal\n"
			+ "            sequencer will be used.\n"
			+ "--ppqn      Sets the PPQN of the sequencer. (default = 24)\n"
			+ "--delay     Sets the pulse delay of the internal sequencer in milliseconds.\n"
			+ "            (default = 21)\n"
			+ "--divisor   Sets the tick divisor. Setting this to the same value of PPQN\n"
			+ "            means the ontick function will be called every quarter note,\n"
			+ "            while a value of 1 means ontick will be called every clock pulse\n"
			+ "            (might be slow). (default = 6)\n"
		);
	}

	/**
	 * Parses command line arguments into a more accessible HashMap
	 * 
	 * @param args command line arguments
	 * @return a hash of named config params and their values
	 */
	public static HashMap<String, ArrayList<String>> parseArgs(String[] args) {
		
		HashMap<String, ArrayList<String>> params = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String nextArg = (i < args.length - 1 ? args[i + 1] : null); // null if there is no next argument
			if (arg.startsWith("--")) {
				String value;
				String key = arg.substring(2);
				if (nextArg != null && !nextArg.startsWith("--")) {
					value = nextArg;
					i += 1; // skip next arg
				}
				else {
					value = "1"; // default value instead of null
				}
				if (!params.containsKey(key)) {
					params.put(key, new ArrayList<String>());
				}
				params.get(key).add(value);
			}
		}
		return params;
		
	}
	
	/**
	 * Entry point for Inkfish app
	 * 
	 * @param args command line arguments
	 * @throws IOException
	 * @throws MidiUnavailableException
	 */
	public static void main(String[] args) throws IOException, MidiUnavailableException {
		
		// Parse command line args
		HashMap<String, ArrayList<String>> params = Inkfish.parseArgs(args);
		
		if (params.containsKey("help")) {
			// Print help and exit
			Inkfish.help();
			System.exit(0);
		}
		else if (params.containsKey("list")) {
			// Print MIDI devices and exit
			MidiDeviceLoader.getInstance().list();
			System.exit(0);
		}
				
		// Instantiate Inkfish with params
		new Inkfish(params);
		
	}
	
}
