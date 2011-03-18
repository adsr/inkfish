import org.mozilla.javascript.*;

public class LookupTableObject extends ScriptableObject implements Scriptable {
	private static final long serialVersionUID = 238270592527335645L;
	private int[] table = null;
	public LookupTableObject() { }
	public void setTable(int[] table) { this.table = table; }
	public void jsConstructor() { }
	public String getClassName() { return "LookupTableObject"; }
	public int jsGet_length() {
		return table.length;
	}
	public int jsFunction_valueAt(int i) {
		if (i < 0 || i >= table.length) return -1;
		return table[i];
	}
}
