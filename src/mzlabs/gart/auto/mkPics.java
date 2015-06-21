package mzlabs.gart.auto;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mzlabs.gart.AAElm;
import mzlabs.gart.Draw;
import mzlabs.gart.qtree;

public class mkPics {
	
	public static final File renderNice(final int idx, final qtree f) {
		final int w = 1024;
		final int h = 768;
		final int aa = 5;
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
		final Image img = Draw.draw(f, w, h, 0.0, aaScheme);
		final DecimalFormat decimalFormat = new DecimalFormat("000000");
		return Draw.writePNG(img,"picR"+decimalFormat.format(idx));
	}
	
	final Object lock = new Object();
	double record = 0.0;
	int ri = 0;
	final int workN = 20;
	final int nGroups = 20;
	final int nSlots = nGroups*workN; // mutiple of workN
	final qtree[] f = new qtree[nSlots];
	final double[] scores = new double[nSlots];

	
	/**
	 * score a batch so we can be parallel in producing images (not done yet) and also can
	 * pay scoring start up cost less often.
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public final double[] score(final int group, final qtree[] f) throws IOException, InterruptedException {
		final int w = 256;
		final int h = 256;
		final int aa = 1;
		final DecimalFormat decimalFormat = new DecimalFormat("000000");
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
		final ArrayList<String> command = new ArrayList<String>();
		command.add("python");
		command.add("./score.py");
		final File[] files = new File[f.length];
		for(int i=0;i<f.length;++i) {
			final Image img = Draw.draw(f[i], w, h, 0.0, aaScheme);
			final String nmI = "picS"+ group + "_" + decimalFormat.format(i);
			command.add("./"+nmI+".png");
			final File file = Draw.writePNG(img,nmI);
			files[i] = file;
		}
		final String resName = "picS" + group + "_score.txt";
		command.add("./" + resName);
		final Runtime r = Runtime.getRuntime();
		final String[] commandA = (String[])command.toArray(new String[0]);
//		for(final String ci: commandA) {
//		  System.out.print(ci + " ");
//		}
//		System.out.println();
		final Process proc = r.exec(commandA);
		proc.waitFor();
		final File scoreF = new File(resName);
		final BufferedReader rdr = new BufferedReader(new FileReader(scoreF));
		final double[] scores = new double[f.length];
		for(int i=0;i<f.length;++i) {
			final String scoreS = rdr.readLine();
			final double scoreV = Double.parseDouble(scoreS.trim());
			scores[i] = scoreV;
			synchronized (lock) {
				if(scoreV>record) {
					record = scoreV;
					++ri;
					final File fi = renderNice(ri,f[i]);
					System.out.println("" + ri + 
							"\t" + f[i].toString() + 
							"\t" + scoreS +
							"\t" + fi.getAbsolutePath() + 
							"\t" + new Date());
				}
			}
		}
		rdr.close();
		for(final File file: files) {
			file.delete();
		}
		scoreF.delete();
		return scores;
	}
	
	public final class RRun implements Runnable {
		public final int g;
		public final int workN;
		public final qtree[] workSet;
		public double[] scores = null;
		
		public RRun(final int g, final int workN) {
			this.g = g;
			this.workN = workN;
			workSet = new qtree[workN];
			for(int j=0;j<workN;++j) {
				workSet[j] = qtree.rantree(7);
			}
		}

		@Override
		public void run() {
			try {
				scores = score(g,workSet);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public final class BRun implements Runnable {
		public final int g;
		public final int workN;
		public final qtree[] workSet;
		private final Random rand;
		
		public BRun(final int g, final int workN, final Random rand) {
			this.g = g;
			this.workN = workN;
			workSet = new qtree[workN];
			this.rand = new Random(rand.nextLong());
			synchronized (lock) {
				for(int j=0;j<workN;++j) {
					final int p1 = rand.nextInt(f.length);
					final int p2 = rand.nextInt(f.length);
					workSet[j] = f[p1].breed(f[p2]);
				}
			}
		}

		@Override
		public void run() {
			try {
				final double[] news = score(g,workSet);
				synchronized (lock) {
					for(int j=0;j<workN;++j) {
						for(int t=0;t<5;++t) {
							final int v = rand.nextInt(nSlots);
							if(scores[v]<news[j]) {
								f[v] = workSet[j];
								scores[v] = news[j];
								break;
							}
						}
					}	
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void doit() throws IOException, InterruptedException {
		final Random rand = new Random(32588);
		final int runPhases = 10000;		
		
		System.out.println("" + "recordNumber" + 
				"\t" + "formula" + 
				"\t" + "score" +
				"\t" + "imagePath" + 
				"\t" + "when");
		
		final int threads = 10;
		{ // get initial concepts
			final ThreadPoolExecutor exec =
					new ThreadPoolExecutor(
							threads,
							threads,
							100000,
							TimeUnit.MILLISECONDS,
							new LinkedBlockingQueue<Runnable>()
							);
			final RRun[] initialTasks = new RRun[nGroups];
			for(int g=0;g<nGroups;++g) {
				initialTasks[g] = new RRun(g,workN);
				exec.execute(initialTasks[g]);
			}
			exec.shutdown();
			while(!exec.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
			}
			int i = 0;
			synchronized (lock) {
				for(final RRun task: initialTasks) {
					for(int j=0;j<task.workN;++j) {
						f[i] = task.workSet[j];
						scores[i] = task.scores[j];
						++i;
					}
				}
			}
		}
		final ThreadPoolExecutor exec =
				new ThreadPoolExecutor(
						threads,
						threads,
						100000,
						TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>()
						);
		// breed
		for(int step=0;step<runPhases;++step) {
			final BRun task = new BRun(step,workN,rand);
			if(exec.getActiveCount()>5) {
				task.run();
			} else {
				exec.execute(task);
			}
		}
		exec.shutdown();
		while(!exec.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		(new mkPics()).doit();
	}

}
