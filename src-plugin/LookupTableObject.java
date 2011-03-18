import org.mozilla.javascript.*;

/**
 * LookupTable JavaScript object
 * @author Adam Saponara
 */
public class LookupTableObject extends ScriptableObject implements Scriptable {
	
	private static final long serialVersionUID = 238270592527335645L;

	public LookupTableObject() { }

	public void jsConstructor() { }

	public String getClassName() { return "LookupTableObject"; }

	/**
	 * Array (table) of values from 0 to 127 inclusive 
	 */
	private int[] table = null;

	/**
	 * Sets the underlying array 
	 */
	public void setTable(int[] table) {
		this.table = table;
	}
	
	/**
	 * Returns the length of the table
	 */
	public int jsGet_length() {
		return table.length;
	}

	/**
	 * Returns the value of the table at position i (zero-indexed)
	 */
	public int jsFunction_valueAt(int i) {
		if (i < 0 || i >= table.length) return -1;
		return table[i];
	}
}
