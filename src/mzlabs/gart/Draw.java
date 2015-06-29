package mzlabs.gart;

// $Header: /Users/johnmount/Documents/CVSRoot/mzlabs/src/mzlabs/gart/Draw.java,v 1.28 2008/10/12 15:52:51 johnmount Exp $

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mzlabs.gart.img.ImgNode;
import mzlabs.gart.util.CommandLineHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public final class Draw {
	private Draw() {
	}

	public static Image draw(qtree formula, final int width, final int height, final double z,
			AAElm[] scheme) {
		Image image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		if (scheme == null) {
			scheme = AAElm.scheme(1, null);
		}
		formula.picfromform(width, height, z, scheme, image);
		return image;
	}
	
	public static class QRunnable implements Runnable {
		public static final int POOLSIZE = 4;
		private static final Object sync = new Object();
		private static int nextId = 0;
		private final int id;
		private final String[] formula;
		private final double[] wt; 
		private final int width;
		private final int height;
		private final double[] z;
		private final AAElm[] scheme;
		private final String dest;
		
		public QRunnable(final String[] formula, final double[] wt, 
				final int width, final int height, final double[] z,
				final AAElm[] scheme, final String dest) {
			synchronized(sync) {
				this.id = nextId;
				++nextId;
			}
			this.formula = formula;
			this.wt = wt;
			this.width = width;
			this.height = height;
			this.z = z;
			this.scheme = scheme;
			this.dest = dest;
			System.out.println("define(" + id + ")\t" + new Date());
		}
		
		public QRunnable(final String f, final int width, final int height, final double z, final AAElm[] scheme,
				final String dest) {
			this(new String[] {f}, new double[] {1.0}, width, height, new double[] {z},scheme,dest);
		}

		public void run() {
			try {
				final int n = formula.length;
				qtree[] formulaParsed= new qtree[n];
				StringBuilder b = new StringBuilder();
				for(int fi=0;fi<n;++fi) {				
					formulaParsed[fi] = new qtree(formula[fi]);
					if(fi>0) {
						b.append(" ");
					}
					b.append(wt[fi]);
					b.append("*'");
					b.append(formulaParsed[fi].toString());
					b.append("'(");
					b.append(z[fi]);
					b.append(")");
				}
				System.out.println("\tstart(" + id + ")\t" + new Date() + "\tcalculate: " + b.toString());
				Image image = draw(formulaParsed,wt,width,height,z,scheme);
				synchronized(sync) {
					System.out.println("\t\twrite(" + id + ")\t" + new Date() + "\t: '" + dest + "'");
					writePNG(image,dest);
					System.out.println("\tdone(" + id + ")\t" + new Date());
				}
			} catch (Throwable ex) {
				System.out.println("caught: " + ex);
			}
		}
		
		public static void addAndMaybeWait(ScheduledThreadPoolExecutor executer, Runnable r) {
			executer.execute(r);
			while(true) {
				final long taskCount = executer.getTaskCount();
				final long completed = executer.getCompletedTaskCount();
				//System.out.println("taskCount: " + taskCount + " completed: " + completed);
				if(taskCount-completed<20) {
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public static void shutdownAndAwaitFinish(ThreadPoolExecutor executer) {
			executer.shutdown();
			boolean done = false;
			while(!done) {
				try {
					if(executer.awaitTermination(0,TimeUnit.MILLISECONDS)) {
						done = true;
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private static Image draw(qtree[] formula, double[] wt, 
			final int width, final int height, final double[] z,
			AAElm[] scheme) {
		Image image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		if (scheme == null) {
			scheme = AAElm.scheme(1, null);
		}
		qtree.picfromform(formula,wt, width, height, z, scheme, image);
		return image;
	}
	

	public static Image draw(String f, final int width, final int height, final double z, final AAElm[] scheme) {
		qtree formula = new qtree(f);
		return draw(formula, width, height, z, scheme);
	}


	// assumes a rendered Image
	public static File writeIMG(final Image img, final String fileName, final String suffix) {
		try {
			RenderedImage rendImage = (RenderedImage) img;
			//String[] names = ImageIO.getWriterFormatNames();
			//for(final String ni: names) {
			//	System.out.println(ni);
			//}
			Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(suffix);
			ImageWriter writer = (ImageWriter) it.next();
			final File f = new File(fileName + "." + suffix);
			//System.out.println("writing '" + f.getAbsolutePath() + "'");
			ImageOutputStream ios = ImageIO.createImageOutputStream(f);
			writer.setOutput(ios);
			writer.write(new IIOImage(rendImage, null, null));
			ios.flush();
			ios.close();
			writer.dispose();
			return f;
		} catch (Exception e) {
			System.out.println("caught: " + e);
			e.printStackTrace();
			return null;
		}
	}

	//public static File writeJPG(final Image img, final String fileName) {
	//	return writeIMG(img,fileName,"jpg");
	//}

	public static File writePNG(final Image img, final String fileName) {
		return writeIMG(img,fileName,"png");
	}
	
	public static void display(Image img) {
		JFrame frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		JLabel label = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(final String[] args) throws Exception  {
		System.out.println("start\t" + new Date());
		final CommandLineHandler clh = new CommandLineHandler("Draw");
		final Option formDirArg = clh.addOArg("formDir","where to read input formulas");
		final Option formFileArg = clh.addOArg("formFile","where to read input formulas");
		final Option destDirArg = clh.addRArg("destDir","where to write result images");
		final Option sourceImgDirArg = clh.addOArg("srcImgs","directory of source images");
		final Option wArg = clh.addOArg("width","result image width in pixels");
		final Option hArg = clh.addOArg("height","result image height in pixels");
		final Option aaArg = clh.addOArg("aaLevel","level of anti aliasing");
		final CommandLine cmd = clh.parse(System.out, args);
		
		final File destDir = new File(cmd.getOptionValue(destDirArg.getOpt()));

		int w = 1280; //2560;
		int h = 800; //2048;
		int aa = 5;
		if(cmd.getOptionValue(wArg.getOpt())!=null) {
			w = Integer.parseInt(cmd.getOptionValue(wArg.getOpt()));
		}
		if(cmd.getOptionValue(hArg.getOpt())!=null) {
			h = Integer.parseInt(cmd.getOptionValue(hArg.getOpt()));
		}
		if(cmd.getOptionValue(aaArg.getOpt())!=null) {
			aa = Integer.parseInt(cmd.getOptionValue(aaArg.getOpt()));
		}
		
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));

		// define input images
		if(cmd.getOptionValue(sourceImgDirArg.getOpt())!=null) {
			File imgSrcDir = new File(cmd.getOptionValue(sourceImgDirArg.getOpt()));
			ArrayList<ImgNode> imgnodes = ImgNode.imgNodes(imgSrcDir,"Img");
			for(final ImgNode ni: imgnodes) {
				qtree.addOp(ni);
			}
		}
		
		System.out.println("start\t" + new Date());
		destDir.mkdirs();
		System.out.println("writing to '" + destDir.getAbsolutePath() + "'");
		final ScheduledThreadPoolExecutor executer = new ScheduledThreadPoolExecutor(QRunnable.POOLSIZE);
		if(cmd.getOptionValue(formDirArg.getOpt())!=null) {
			final File formDir = new File(cmd.getOptionValue(formDirArg.getOpt()));
			final File[] list = formDir.listFiles();
			for(final File fi: list) {
				final String nm = fi.getName();
				final String txtSuffix = ".txt";
				if(nm.endsWith(txtSuffix)) {
					LineNumberReader rdr = new LineNumberReader(new FileReader(fi));
					String f = rdr.readLine();
					rdr.close();
					final File dest = new File(destDir,nm.substring(0,nm.length() - txtSuffix.length()));
					QRunnable r = new QRunnable(f,w,h,0.0,aaScheme,dest.getAbsolutePath());
					QRunnable.addAndMaybeWait(executer,r);
				}
			}
		}
		if(cmd.getOptionValue(formFileArg.getOpt())!=null) {
			final File formFile = new File(cmd.getOptionValue(formFileArg.getOpt()));
			final LineNumberReader rdr = new LineNumberReader(new FileReader(formFile));
			String line = null;
			while((line = rdr.readLine())!=null) {
				line = line.trim();
				if((line.length()>0)&&(line.charAt(0)!='#')) {
					final int intVal = Integer.parseInt(line);
					final String f = farchive.flist[intVal];
					final File dest = new File(destDir,line);
					QRunnable r = new QRunnable(f,w,h,0.0,aaScheme,dest.getAbsolutePath());
					QRunnable.addAndMaybeWait(executer,r);
				}
			}
			rdr.close();
		}

		QRunnable.shutdownAndAwaitFinish(executer);
		System.out.println("all done\t" + new Date());
	}
}