package mzlabs.gart;

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import mzlabs.gart.img.ImgNode;
import mzlabs.gart.util.ClassControl;
import mzlabs.gart.util.CommandLineHandler;
import mzlabs.gart.util.XMLSerializer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.jdom.Element;

/**
 * 489 cp ~/Documents/workspace/writing/WebSite/JMPubs/gartMainfest.txt .
 * 490  rm ~/Documents/workspace/writing/WebSite/JMPubs/gart.jar 
 * 491  jar cmf gartMainfest.txt gart.jar  mzlabs/gart mzlabs/util/DObserver.class mzlabs/util/DStat.class 
 * 492  jar -i gart.jar 
 * 493  jar -tvf gart.jar 
 * 494  java -jar gart.jar 
 * 496  rm gartMainfest.txt 
 * 497  mv gart.jar ~/Documents/workspace/writing/WebSite/JMPubs
 * @author johnmount
 *
 */

public final class GartUi4 {
	Container container = null;
	
	public static final boolean webVersion = false;
	public static final String copyRight = "Copyright(c) John Mount www.mzlabs.com";


	final static String defaultPath;

	final static String configFilePath;
	static {
		String dp = "";
		try {
			dp = System.getProperties().getProperty("user.home", ".") + "/";
		} catch (Exception e) {
		}
		defaultPath = dp;
		final boolean useSerializer = !webVersion;
		if(useSerializer) {
			final String configFileName = "geneticArtConfig.xml";
			configFilePath = defaultPath + configFileName;
		} else {
			configFilePath = null;
		}
	}

	final AAElm[] scheme1 = AAElm.scheme(1, null);
	final AAElm[] scheme3 = AAElm.scheme(3, new Random(15153));
	final AAElm[] aascheme = scheme3;

	final int pwidth = 150;

	final int pheight = 100;

	final int swidth = 90;

	final int sheight = 60;

	private Random rand = new Random(1515);

	// synchronize through rand
	private JLayeredPane desktop = null;
	
	int targetFrames = Integer.MAX_VALUE;

	private final Map<PFrame,PFrame> pframes = new LinkedHashMap<PFrame,PFrame>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<PFrame,PFrame> eldest) {
			final boolean nuke = (size() > targetFrames);
			if(nuke) {
				PFrame p = eldest.getKey();
				p.dispose();
			}
	        return nuke;
	     }
	};

	int fwidth = 900;

	int fheight = 600;

	int aheight = pheight + 50;

	int awidth = pwidth + 30;

	PFrame leftFrame = null;

	PFrame rightFrame = null;

	Image leftImage = null;

	Image rightImage = null;

	JLabel leftLable = null;

	JLabel rightLable = null;

	public GartUi4(Container container_in) {
		container = container_in;
	}

	public void init(int fwidth_in, int fheight_in) {
		synchronized (rand) {
			fwidth = fwidth_in;
			fheight = fheight_in;
			container.setSize(fwidth, fheight);
			PStrip topStrip = null;
			final boolean useTopStrip = !webVersion;
			if(useTopStrip) {
				topStrip = new PStrip(fwidth, sheight, this);
			}
			if (topStrip != null) {
				topStrip.init();
			}
			JButton newButton = new JButton("random picture");
			JButton copyRButton = new JButton(copyRight);
			newButton.setToolTipText("create new random picture");
			JButton breedButton = new JButton("<-- breed pictures -->");
			breedButton.setToolTipText("breed selected pictures");
			JPanel pbot = new JPanel(new BorderLayout());
			pbot.add(newButton, BorderLayout.WEST);
			pbot.add(copyRButton, BorderLayout.CENTER);
			leftImage = new BufferedImage(swidth, sheight,
					BufferedImage.TYPE_INT_RGB);
			leftLable = new JLabel(new ImageIcon(leftImage));
			rightImage = new BufferedImage(swidth, sheight,
					BufferedImage.TYPE_INT_RGB);
			rightLable = new JLabel(new ImageIcon(rightImage));
			JPanel pr = new JPanel();
			pr.add(leftLable);
			pr.add(breedButton);
			pr.add(rightLable);
			pbot.add(pr, BorderLayout.EAST);
			Container contentPane = null;
			if (container instanceof JApplet) {
				contentPane = ((JApplet) container).getContentPane();
			} else if (container instanceof JFrame) {
				contentPane = ((JFrame) container).getContentPane();
			}
			if (topStrip != null) {
				contentPane.add(topStrip, BorderLayout.NORTH);
			}
			contentPane.add(pbot, BorderLayout.SOUTH);
			if (container instanceof Window) {
				Window win = (Window) container;
				win.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						if (configFilePath != null) {
							try {
								System.out.println("writing \""
										+ configFilePath + "\"");
								GConfig conf = getConfig();
								Element e1 = XMLSerializer.toElement("geneticart", conf);
								Element res = e1;
								XMLSerializer.writeToFile(configFilePath, res);
								System.out.println("all done\t" + new Date());
							} catch (Exception ex) {
								System.out.println("caught: " + ex);
							}
						}
						System.exit(0);
					}
				});
			}
			newButton.addActionListener(new BreedListener(true));
			breedButton.addActionListener(new BreedListener(false));
			// Set up the layered pane
			desktop = new JDesktopPane();
			desktop.setOpaque(true);
			contentPane.add(desktop, BorderLayout.CENTER);
			container.setVisible(true);
		}
	}

	public void placeInitialItems() {
		synchronized (rand) {
			PFrame p0 = buildPic(qtree.rantree(7), 0, 0);
			pframes.put(p0,p0);
			p0.setVisible(true);
			Dimension dim = p0.getSize();
			aheight = (int) dim.getHeight();
			awidth = (int) dim.getWidth();
			final int nx = fheight / aheight;
			final int ny = fwidth / awidth;
			targetFrames = nx*ny -1;
			for (int j = 0; j < nx; ++j) {
				for (int i = 0; i < ny; ++i) {
					if( ((i != 0) || (j != 0)) && ((i!=ny-1)||(j!=nx-1))) {
						PFrame pi = buildPic(qtree.rantree(7), i * awidth, j
								* aheight);
						if (pi != null) {
							pframes.put(pi,pi);
							pi.setVisible(true);
						}
					}
				}
			}
		}
	}

	public void placeStoredItems(GConfig store) {
		synchronized (rand) {
			if (store.frames != null) {
				targetFrames = store.frames.length + 1; // not quite right, should store this value
				for (int i = 0; i < store.frames.length; ++i) {
					if (store.frames[i] != null) {
						try {
							PFrame pi = buildPic(store.frames[i]);
							if (pi != null) {
								pframes.put(pi,pi);
								pi.setVisible(true);
							}
						} catch (Exception e) {
							System.out.println("caught: " + e);
						}
					}
				}
			}
			if (store.config != null) {
				try {
					awidth = store.config.awidth;
					aheight = store.config.aheight;
					fwidth = store.config.fwidth;
					fheight = store.config.fheight;
					container.setSize(fwidth, fheight);
					Object lf = store.config.lformula;
					if (lf != null) {
						PFrame frame = buildPic(qtree.newTree((String) lf), 0,
								0, 15, 10, aascheme);
						leftFrame = frame;
						frame.aFormula.picfromform(swidth, sheight, 0.0, aascheme,
								leftImage);
						leftLable.repaint();
					}
					Object rf = store.config.rformula;
					if (rf != null) {
						PFrame frame = buildPic(qtree.newTree((String) rf), 0,
								0, 15, 10, aascheme);
						rightFrame = frame;
						frame.aFormula.picfromform(swidth, sheight, 0.0, aascheme,
								rightImage);
						rightLable.repaint();
					}
				} catch (Exception e) {
					System.out.println("config problem: " + e);
				}
			}
			final int nx = fheight / aheight;
			final int ny = fwidth / awidth;
			targetFrames = nx*ny -1;
		}
	}

	Rectangle[] getBoundsList() {
		ArrayList<Rectangle> l = new ArrayList<Rectangle>();
		synchronized (rand) {
			final Set<PFrame> whacks = new HashSet<PFrame>();
			for(final PFrame ci: pframes.keySet()) {
				if (ci.isClosed()) {
					whacks.add(ci);
				} else {
					l.add(ci.getBounds());
				}
			}
			for(final PFrame wi: whacks) {
				pframes.remove(wi);
			}
		}
		return l.toArray(new Rectangle[0]);
	}

	public Point2D getNewPlacement() {
		synchronized (rand) {
			// get forbidden list
			Rectangle[] forb = getBoundsList();
			if ((forb == null) || (forb.length <= 0)) {
				return new Point2D.Double(0.0, 0.0);
			}
			// refresh frame size (optional)
			Dimension dim = container.getSize();
			fheight = (int) dim.getHeight();
			fwidth = (int) dim.getWidth();
			// try list of locations
			for (int j = 0; j < fheight / aheight; ++j) {
				for (int i = 0; i < fwidth / awidth; ++i) {
					int x = i * awidth;
					int y = j * aheight;
					boolean fail = false;
					for (int k = 0; (k < forb.length) && (!fail); ++k) {
						int u = (int) forb[k].getX();
						int w = (int) forb[k].getWidth();
						int v = (int) forb[k].getY();
						int h = (int) forb[k].getHeight();
						fail |= (u > x - w) && (v > y - h) && (u < x + awidth)
								&& (v < y + aheight);
					}
					if (!fail) {
						return new Point2D.Double(x, y);
					}
				}
			}
			// random coordinates
			return new Point2D.Double(rand.nextInt(fwidth - awidth), rand
					.nextInt(fheight - 2 * aheight));
		}
	}

	public GConfig getConfig() {
		GConfig r = new GConfig();
		synchronized (rand) {
			LinkedList<PConfig> l = new LinkedList<PConfig>();
			Dimension dim = container.getSize();
			fheight = (int) dim.getHeight();
			fwidth = (int) dim.getWidth();
			r.config = new FConfig();
			r.config.awidth = awidth;
			r.config.aheight = aheight;
			r.config.fwidth = fwidth;
			r.config.fheight = fheight;
			r.config.lformula = "";
			if (leftFrame != null) {
				r.config.lformula = leftFrame.aFormula.toString();
			}
			r.config.rformula = "";
			if (rightFrame != null) {
				r.config.rformula = rightFrame.aFormula.toString();
			}
			for(final PFrame ci: pframes.keySet()) {
				if ((ci != null) && (!ci.isClosed())) {
					PConfig p = ci.getConfig();
					if (p != null) {
						l.addLast(p);
					}
				}
			}
			r.frames = l.toArray(new PConfig[0]);
		}
		return r;
	}

	class PFrame extends JInternalFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public final qtree aFormula;

		public final Image img;

		public final int w;

		public final int h;

		public final boolean highQuality;

		public PFrame(String name_in, qtree aFormula_in, int w_in, int h_in,
				boolean highQuality_in, Image img_in) {
			super(name_in, true, true, true, true);
			aFormula = aFormula_in;
			highQuality = highQuality_in;
			w = w_in;
			h = h_in;
			img = img_in;
		}

		public PConfig getConfig() {
			PConfig r = new PConfig();
			Rectangle loc = getBounds();
			r.formula = aFormula.toString();
			r.x = (int) loc.getX();
			r.y = (int) loc.getY();
			r.w = w;
			r.h = h;
			r.highQuality = highQuality;
			return r;
		}
	}

	abstract class PListener implements ActionListener {
		public final PFrame frame;

		public PListener(PFrame frame_in) {
			frame = frame_in;
		}
	}

	class LTakeListener extends PListener {
		public LTakeListener(PFrame frame_in) {
			super(frame_in);
		}

		public void actionPerformed(ActionEvent e) {
			synchronized (rand) {
				leftFrame = frame;
				frame.aFormula.picfromform(swidth, sheight, 0.0, aascheme, leftImage);
				leftLable.repaint();
			}
		}
	}

	class RTakeListener extends PListener {
		public RTakeListener(PFrame frame_in) {
			super(frame_in);
		}

		public void actionPerformed(ActionEvent e) {
			synchronized (rand) {
				rightFrame = frame;
				frame.aFormula
						.picfromform(swidth, sheight, 0.0, aascheme,rightImage);
				rightLable.repaint();
			}
		}
	}

	class EditListener extends PListener {
		public EditListener(PFrame frame_in) {
			super(frame_in);
		}

		public void actionPerformed(ActionEvent e) {
			Rectangle loc = frame.getBounds();
			buildText(frame.aFormula, (int) loc.getX(), (int) loc.getY());
			//System.out.println(frame.aFormula);
		}
	}

	class DrawListener extends PListener {
		public DrawListener(PFrame frame_in) {
			super(frame_in);
		}

		public void actionPerformed(ActionEvent e) {
			Rectangle loc = frame.getBounds();
			PFrame pi = buildPic(frame.aFormula, (int) loc.getX(), (int) loc
					.getY(), 300, 200, aascheme);
			if (pi != null) {
				synchronized (rand) {
					pframes.put(pi,pi);
					pi.setVisible(true);
				}
			}
		}
	}

	class SaveListener extends PListener {
		public SaveListener(PFrame frame_in) {
			super(frame_in);
		}

		public void actionPerformed(ActionEvent e) {
			final DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH.mm.ss.SSS");
			final String destName = "gartPicture" + df.format(new Date());
			{
				try {
					final File f = new File(destName + ".txt");
					PrintStream p = new PrintStream(f);
					p.println(frame.aFormula.toString());
					p.close();
					System.out.println("wrote: '" + f.getAbsolutePath() + "'");
				} catch (FileNotFoundException e1) {
					System.out.println("caught: " + e1);
				}
				
			}
			//System.out.println("writing: \"" + destName + "." + suffix);
			Draw.writePNG(frame.img, destName);
		}
	}

	public PFrame buildPic(qtree formula, int x, int y) {
		return buildPic(formula, x, y, pwidth, pheight, aascheme);
	}

	public PFrame buildPic(PConfig dat) {
		if (dat == null) {
			return null;
		}
		try {
			return buildPic(qtree.newTree(dat.formula), dat.x, dat.y, dat.w,
					dat.h, (dat.highQuality ? aascheme : aascheme));
		} catch (Exception e) {
			return null;
		}
	}

	public PFrame buildPic(qtree formula, final int x, final int y, final int w, final int h,
			AAElm[] scheme) {
		Image img = Draw.draw(formula, w, h, 0.0, scheme);
		boolean highQuality = (scheme != null) && (scheme.length > 1);
		PFrame internalFrame = new PFrame("pic", formula, w, h, highQuality,
				img);
		internalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		internalFrame.setBounds(x, y, w, h);
		JLabel label = new JLabel(new ImageIcon(img));
		internalFrame.getContentPane().add(label, BorderLayout.CENTER);
		JMenu selMenu = new JMenu("Action Menu");
		selMenu.setToolTipText("select action for this picture");
		{
			JMenuItem leftAction = new JMenuItem("take over left selection");
			leftAction.addActionListener(new LTakeListener(internalFrame));
			leftAction.setToolTipText("take over left selection");
			selMenu.add(leftAction);
		}
		{
			JMenuItem rightAction = new JMenuItem("take over right selection");
			rightAction.addActionListener(new RTakeListener(internalFrame));
			rightAction.setToolTipText("take over right selection");
			selMenu.add(rightAction);
		}
		{
			JMenuItem editAction = new JMenuItem("edit formula");
			editAction.addActionListener(new EditListener(internalFrame));
			editAction.setToolTipText("edit formula");
			selMenu.add(editAction);
		}
		{
			JMenuItem drawAction = new JMenuItem("large picture");
			drawAction.addActionListener(new DrawListener(internalFrame));
			drawAction.setToolTipText("draw large picture");
			selMenu.add(drawAction);
		}
		if (container instanceof Window) {
			JMenuItem saveAction = new JMenuItem("save image");
			saveAction.addActionListener(new SaveListener(internalFrame));
			saveAction.setToolTipText("save image to disk");
			selMenu.add(saveAction);
		}
		JMenuBar bar = new JMenuBar();
		bar.setToolTipText("select action for this picture");
		bar.add(selMenu);
		internalFrame.getContentPane().add(bar, BorderLayout.SOUTH);
		internalFrame.pack();
		synchronized (rand) {
			desktop.add(internalFrame, new Integer(1));
		}
		return internalFrame;
	}

	public PFrame placePic(qtree formula) {
		if (formula == null) {
			return null;
		}
		Point2D place = null;
		synchronized (rand) {
			place = getNewPlacement();
		}
		PFrame pi = buildPic(formula, (int) place.getX(), (int) place.getY());
		if (pi != null) {
			synchronized (rand) {
				pframes.put(pi,pi);
				pi.setVisible(true);
			}
		}
		return pi;
	}

	class TextListener implements ActionListener {
		public final JTextComponent txt;

		public TextListener(JTextComponent txt_in) {
			txt = txt_in;
		}

		public void actionPerformed(ActionEvent e) {
			qtree formula = null;
			try {
				javax.swing.text.Document doc = txt.getDocument();
				if (doc == null) {
					return;
				}
				String form = doc.getText(0, doc.getLength());
				if ((form == null) || (form.length() <= 0)) {
					return;
				}
				formula = qtree.newTree(form);
				if (formula == null) {
					return;
				}
			} catch (Exception ex) {
				return;
			}
			placePic(formula);
		}
	}

	public JInternalFrame buildText(qtree formula, int x, int y) {
		JInternalFrame internalFrame = new JInternalFrame("formula", true,
				true, true, true);
		internalFrame.setBounds(x, y, 2 * pwidth, 2 * pheight);
		JEditorPane text = new JEditorPane();
		//text.setLineWrap(true);
		//text.setWrapStyleWord(true);
		//text.getDocument().addDocumentListener();
		text.setText(formula.toString());
		JScrollPane scroll = new JScrollPane(text);
		internalFrame.getContentPane().add(scroll, BorderLayout.CENTER);

		JButton newButton = new JButton("new picture");
		newButton.setToolTipText("create new picture from formula");
		newButton.addActionListener(new TextListener(text));
		JPanel p = new JPanel();
		p.add(newButton);
		internalFrame.getContentPane().add(p, BorderLayout.SOUTH);

		internalFrame.pack();
		synchronized (rand) {
			desktop.add(internalFrame, new Integer(1));
		}
		internalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		internalFrame.setSize(2 * pwidth, 2 * pheight);
		internalFrame.setVisible(true);
		return internalFrame;
	}

	// An inner class to handle presses of the Open button
	class BreedListener implements ActionListener {
		final boolean forceNew;

		public BreedListener(boolean forceNew_in) {
			forceNew = forceNew_in;
		}

		public void actionPerformed(ActionEvent e) {
			qtree formula = null;
			synchronized (rand) {
				if (forceNew) {
					formula = qtree.rantree(7);
				} else {
					if (leftFrame != null) {
						if (rightFrame != null) {
							formula = leftFrame.aFormula
									.breedP(rightFrame.aFormula);
						} else {
							formula = leftFrame.aFormula.breedP(qtree
									.rantree(7));
						}
					} else {
						if (rightFrame != null) {
							formula = qtree.rantree(7).breedP(
									rightFrame.aFormula);
						} else {
							formula = qtree.rantree(7);
						}
					}
				}
			}
			placePic(formula);
		}
	}

	public static GConfig readStore() {
		GConfig store = null;
		if ((configFilePath != null) && (configFilePath.length() > 0)) {
			System.out.println("reading \"" + configFilePath + "\"");
			try {
				org.jdom.Document doc = XMLSerializer
						.readFromFile(configFilePath);
				if (doc != null) {
					Element re = doc.getRootElement();
					if (re != null) {
						Class<?> template = Class
								.forName("mzlabs.gart.GartUi4$GConfig");
						ClassControl cc = ClassControl
								.buildClassControl(template);
						store = (GConfig) XMLSerializer.toObject(re, cc);
					}
				}
			} catch (Exception e) {
				System.out.println("caught: " + e);
			}
		}
		return store;
	}

	public static class FConfig {
		public int awidth;

		public int aheight;

		public int fwidth;

		public int fheight;
		
		public String lformula;

		public String rformula;
	}

	public static class PConfig {
		public String formula;

		public int x;

		public int y;

		public int w;

		public int h;

		public boolean highQuality;
	}

	public static class GConfig {
		public FConfig config;

		public PConfig[] frames;
	}

	public static void main(String[] args) throws ParseException {
		System.out.println("start\t" + new Date());
		CommandLineHandler clh = new CommandLineHandler("GartUi4");
		Option sourceImgDir = clh.addOArg("srcImgs","directory of source images");
		CommandLine cmd = clh.parse(System.out, args);
		
		// define input images
		if(cmd.getOptionValue(sourceImgDir.getOpt())!=null) {
			File imgSrcDir = new File(cmd.getOptionValue(sourceImgDir.getOpt()));
			ArrayList<ImgNode> imgnodes = ImgNode.imgNodes(imgSrcDir,"Img");
			for(final ImgNode ni: imgnodes) {
				qtree.addOp(ni);
			}
		}
		
		//System.getProperties().list(System.out);
		System.out.println(copyRight);
		System.out.println("start");
		JFrame frame = new JFrame(copyRight);
		GartUi4 o = new GartUi4(frame);
		GConfig store = readStore();
		if ((store != null) && (store.config != null)) {
			System.out.println("regenerating");
			o.init(store.config.fwidth, store.config.fheight);
		} else {
			System.out.println("init");
			o.init(900, 600);
		}
		if (store != null) {
			o.placeStoredItems(store);
		} else {
			o.placeInitialItems();
		}
		System.out.println("initted");
	}
}