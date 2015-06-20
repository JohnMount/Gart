package mzlabs.gart;

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class PStrip extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static AAElm[] scheme1 = AAElm.scheme(1, null);

	int fheight = 900;

	int fwidth = 600;

	int sheight = 60;

	int swidth = 90;

	BoxLayout tl = null;

	PButton[] buttons = null;

	PButton tempButton = null;

	int nextF = 0;

	GartUi4 parent = null;

	public PStrip(int fwidth_in, int fheight_in, GartUi4 parent_in) {
		fwidth = fwidth_in;
		fheight = fheight_in;
		parent = parent_in;
	}

	static class CButton extends JButton implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		PStrip parent;

		boolean right;

		public CButton(String text_in, PStrip parent_in, boolean right_in) {
			super(text_in);
			parent = parent_in;
			right = right_in;
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			synchronized (parent) {
				if (right) {
					--parent.nextF;
					int newIndex = (parent.nextF - parent.buttons.length)
							% farchive.flist.length;
					if (newIndex < 0) {
						newIndex += farchive.flist.length;
					}
					String f = farchive.flist[newIndex];
					parent.rotateButtonsRight(qtree.newTree(f));
				} else {
					int newIndex = parent.nextF % farchive.flist.length;
					++parent.nextF;
					if (newIndex < 0) {
						newIndex += farchive.flist.length;
					}
					String f = farchive.flist[newIndex];
					parent.rotateButtonsLeft(qtree.newTree(f));
				}
			}
		}
	}

	static class PButton extends JButton implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int w;

		public int h;

		public String fString;

		public qtree formula;

		public Image image;

		public GartUi4 parent = null;

		private PButton(int w_in, int h_in, Image image_in, qtree formula_in,
				GartUi4 parent_in) {
			super(new ImageIcon(image_in));
			parent = parent_in;
			w = w_in;
			h = h_in;
			image = image_in;
			formula = null;
			fString = null;
			setFormula(formula_in);
			addActionListener(this);
		}

		public PButton(int w_in, int h_in, qtree formula_in, GartUi4 parent_in) {
			this(w_in, h_in, new BufferedImage(w_in, h_in,
					BufferedImage.TYPE_INT_RGB), formula_in, parent_in);
		}

		public synchronized void overwriteFrom(PButton but) {
			formula = but.formula;
			fString = but.fString;
			image.getGraphics().drawImage(but.image, 0, 0, this);
		}

		public synchronized void setFormula(qtree formula_in) {
			formula = formula_in;
			fString = null;
			if (formula != null) {
				fString = formula.toString();
				formula.picfromform(w, h, 0.0, scheme1, image);
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (parent == null) {
				System.out.println("action: " + fString);
			} else {
				parent.placePic(formula);
			}
		}
	}

	public synchronized void init() {
		tl = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(tl);
		buttons = new PButton[fwidth / (swidth + 30) - 2];
		add(new CButton("<--", this, false));
		nextF = qtree.rand.nextInt(farchive.flist.length);
		for (int i = 0; i < buttons.length; ++i) {
			int newIndex = nextF % farchive.flist.length;
			if (newIndex < 0) {
				newIndex += farchive.flist.length;
			}
			String formula = farchive.flist[nextF % farchive.flist.length];
			++nextF;
			buttons[i] = new PButton(swidth, sheight, qtree.newTree(formula),
					parent);
			add(buttons[i]);
		}
		add(new CButton("-->", this, true));
		tempButton = new PButton(swidth, sheight, qtree
				.newTree(farchive.flist[0]), parent);
	}

	public synchronized void rotateButtonsLeft(qtree donor) {
		if (donor == null) {
			tempButton.overwriteFrom(buttons[0]);
		}
		for (int i = 0; i < buttons.length - 1; ++i) {
			buttons[i].overwriteFrom(buttons[i + 1]);
		}
		if (donor == null) {
			buttons[buttons.length - 1].overwriteFrom(tempButton);
		} else {
			buttons[buttons.length - 1].setFormula(donor);
		}
		repaint();
	}

	public synchronized void rotateButtonsRight(qtree donor) {
		if (donor == null) {
			tempButton.overwriteFrom(buttons[buttons.length - 1]);
		}
		for (int i = buttons.length - 1; i > 0; --i) {
			buttons[i].overwriteFrom(buttons[i - 1]);
		}
		if (donor == null) {
			buttons[0].overwriteFrom(tempButton);
		} else {
			buttons[0].setFormula(donor);
		}
		repaint();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("PStrip");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(900, 100);
		Container contentPane = frame.getContentPane();
		PStrip o = new PStrip(800, 60, null);
		o.init();
		contentPane.add(o, BorderLayout.NORTH);
		frame.setVisible(true);
		System.out.println("initted");
	}
}