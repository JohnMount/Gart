package mzlabs.gart;


/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class Grid extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	final static AAElm[] scheme1 = AAElm.scheme(1, null);


	private final int sheight = 120;

	private final int swidth = 180;

	private final int nx = 4;
	private final int ny = 5;

	private final int fheight = (sheight+30)*ny;

	private final int fwidth = (swidth+30)*nx;

	private final GridLayout tl;

	private final PButton[][] buttons;
	
	private PButton lastPressed = null;




	public Grid() {
		tl = new GridLayout(ny,nx);
		buttons = new PButton[ny][nx];
	}


	private final class PButton extends JButton implements ActionListener, Comparable<PButton> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final int x;
		public final int y;
		public final int w;
		public final int h;
		public String fString;
		public qtree formula;
		public final Image image;

		public PButton(final int x, final int y, 
				final int w_in, final int h_in, final qtree formula_in,
				final BufferedImage image) {
			super(new ImageIcon(image));
			this.x = x;
			this.y = y;
			w = w_in;
			h = h_in;
			this.image = image;
			formula = null;
			fString = null;
			setFormula(formula_in);
			addActionListener(this);
		}

		public PButton(final int x, final int y, 
				final int w_in, final int h_in, final qtree formula_in) {
			this(x,y,w_in,h_in,formula_in,
					new BufferedImage(w_in, h_in,BufferedImage.TYPE_INT_RGB));
					
		}


		public synchronized void setFormula(qtree formula_in) {
			formula = formula_in;
			fString = null;
			if (formula != null) {
				fString = formula.toString();
				formula.picfromform(w, h, 0.0, scheme1, image);
				image.flush();
				repaint();
			}
		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("action: " + fString);
			PButton replace = null;
			do {
				replace = buttons[qtree.rand.nextInt(ny)][qtree.rand.nextInt(nx)];
				if(replace.compareTo(this)==0) {
					replace = null;
				} else if(replace.compareTo(lastPressed)==0) {
					replace = null;
				}
			} while(replace==null);
			final PButton donor;
			final qtree nf;
			if(qtree.rand.nextBoolean()) {
				donor = lastPressed;
				nf = formula.breedP(donor.formula);
				System.out.println("breed ("
						+ this.x + "," + this.y 
						+ ") with (" + donor.x + "," + donor.y + ") replace (" + replace.x + "," + replace.y + ")");
			} else {
				donor = null;
				nf = qtree.rantree(7);
				System.out.println("random replace (" + replace.x + "," + replace.y + ")");
			}
			replace.setFormula(nf);
			for(int y=0;y<ny;++y) {
				for(int x=0;x<nx;++x) {
					buttons[y][x].setSelected(false);
				}
			}
			this.setSelected(true);
			replace.setSelected(true);
			if(donor!=null) {
				donor.setSelected(true);
			}
			lastPressed = this;
			System.out.println("newest (" + replace.x + "," + replace.y + ")\n\t" + nf.toString());
		}

		//@Override  // declaring this override forces us to Java1.6
		public int compareTo(final PButton o) {
			if(x!=o.x) {
				return (x>=o.x)?1:-1;
			}
			if(y!=o.y) {
				return (y>=o.y)?1:-1;
			}
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			return compareTo((PButton)o)==0;
		}
		
		@Override
		public int hashCode() {
			return x + 17*y;
		}
	}

	public synchronized void init() {
		setLayout(tl);
		for(int y=0;y<ny;++y) {
			for(int x=0;x<nx;++x) {
				final qtree tree;
				if(qtree.rand.nextBoolean()) {
					String formula = farchive.flist[qtree.rand.nextInt(farchive.flist.length)];
					tree = qtree.newTree(formula);
				} else {
					tree = qtree.rantree(7);
				}
				buttons[y][x] = new PButton(x,y,swidth, sheight, tree);
				add(buttons[y][x]);
			}
		}
		final int dx = qtree.rand.nextInt(nx);
		final int dy = qtree.rand.nextInt(ny);
		lastPressed = buttons[dy][dx];
	}


	public static void main(String[] args) {
		Grid o = new Grid();
		JFrame frame = new JFrame("Grid");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(o.fwidth,o.fheight);
		Container contentPane = frame.getContentPane();
		o.init();
		contentPane.add(o, BorderLayout.NORTH);
		frame.setVisible(true);
		System.out.println("initted");
	}
}