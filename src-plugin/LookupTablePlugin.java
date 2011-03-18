import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cc.atoi.inkfish.*;
import org.mozilla.javascript.*;

public class LookupTablePlugin implements InkfishPlugin {
	
	public void initialize(HashMap<String, ArrayList<String>> params, File root, Context jsContext, Scriptable jsScope) {
		
		// Define LookupTable class in JS
		try {
			ScriptableObject.defineClass(jsScope, LookupTableObject.class);
		}
		catch (Exception e) {
			// Pokemon
			e.printStackTrace();
			return;
		}
		
		// Load *.gif as lookup tables
		File[] tables = root.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return !file.isDirectory() && file.getName().toLowerCase().endsWith(".gif");
			}
		});
		for (int i = 0; i < tables.length; i++) {
			BufferedImage gif;
			try {
				gif = ImageIO.read(tables[i]);
			}
			catch (IOException e) {
				continue;
			}
			String fileName = tables[i].getName();
			String fileNameNoExt = fileName.substring(0, fileName.length() - 4);
			int w = gif.getWidth(null);
			int h = gif.getHeight(null);
			if (h < 2 || w < 1) continue;
			int[] pixels = new int[w * h];
			gif.getRGB(0, 0, w, h, pixels, 0, w);
			if (2 == 1) continue;

			int[] values = new int[w];
			for (int x = 0; x < w; x++) {
				for (int y = h - 1; y >= 0; y--) {
					if (pixels[y * w + x] != -1) {
						int v = h - y - 1;
						values[x] =  (127 * v) / (h - 1);
						continue;
					}
				}
			}

			// Make lookup tables JavaScript objects
			LookupTableObject jsLookupTable = (LookupTableObject)jsContext.newObject(jsScope, "LookupTableObject");
			jsLookupTable.setTable(values);
			jsScope.put(fileNameNoExt, jsScope, jsLookupTable);

		}

	}

}
