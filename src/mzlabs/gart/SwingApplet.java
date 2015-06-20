
package mzlabs.gart;

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import javax.swing.JApplet;

public final class SwingApplet extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// This is a hack to avoid an ugly error message in 1.1.
	public SwingApplet() {
		getRootPane().putClientProperty("defeatSystemEventQueueCheck",
				Boolean.TRUE);
	}

	public void init() {
		GartUi4 o = new GartUi4(this);
		o.init(900, 600);
		o.placeInitialItems();
	}
}