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
import mzlabs.gart.farchive;
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

	double record = 0.0;
	int ri = 0;
	final int workN = 20;
	final int nGroups = 20;
	final int nSlots = nGroups*workN; // mutiple of workN
	final qtree[] f = new qtree[nSlots];
	final double[] scores = new double[nSlots];


	public static final class DrawI implements Runnable {
		public final int group;
		public final int i;
		public final qtree fi;
		public final String nmI;
		public File resFile = null;

		public DrawI(final int group, final int i, final qtree fi) {
			this.group = group;
			this.i = i;
			this.fi = fi;
			final DecimalFormat decimalFormat = new DecimalFormat("000000");
			nmI = "picS"+ group + "_" + decimalFormat.format(i);
		}

		@Override
		public void run() {
			synchronized (this) {
				final int w = 256;
				final int h = 256;
				final int aa = 1;
				final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
				final Image img = Draw.draw(fi, w, h, 0.0, aaScheme);
				final File file = Draw.writePNG(img,nmI);
				resFile = file;
			}
		}
	}

	/**
	 * don't call this from multiple threads if running CUDA
	 * score a batch so we can be parallel in producing images (not done yet) and also can
	 * pay scoring start up cost less often.
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static final double[] score(final int group, final qtree[] f) throws IOException, InterruptedException {
		final int threads = 10;
		final ThreadPoolExecutor exec =
				new ThreadPoolExecutor(
						threads,
						threads,
						100000,
						TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>()
						);
		final DrawI[] tasks = new DrawI[f.length];
		for(int i=0;i<f.length;++i) {
			tasks[i] = new DrawI(group,i,f[i]);
			if(exec.getActiveCount()>5) {
				tasks[i].run();
			} else {
				exec.execute(tasks[i]);
			}
		}
		exec.shutdown();
		while(!exec.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
		}
		final File[] files = new File[f.length];
		final ArrayList<String> command = new ArrayList<String>();
		command.add("python");
		command.add("./score.py");
		for(int i=0;i<f.length;++i) {
			synchronized(tasks[i]) {
				files[i] = tasks[i].resFile;
				command.add("./"+tasks[i].nmI+".png");
			}
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
		}
		rdr.close();
		for(final File file: files) {
			file.delete();
		}
		scoreF.delete();
		return scores;
	}

	public void checkRecords(final qtree[] formulas, final double[] fscores) {
		for(int i=0;i<formulas.length;++i) {
			final qtree formi = formulas[i];
			final double scoreV = fscores[i];
			if(scoreV>record) {
				record = scoreV;
				++ri;
				final File fi = renderNice(ri,formi);
				System.out.println("" + ri + 
						"\t" + formi.toString() + 
						"\t" + scoreV +
						"\t" + fi.getAbsolutePath() + 
						"\t" + new Date());
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

		{ // get initial concepts
			final int[] indices= { 45, 51, 161, 167, 198, 346, 512, 515 };
			final qtree[] workSet = new qtree[indices.length];
			for(int j=0;j<indices.length;++j) {
				workSet[j] = qtree.newTree(farchive.flist[indices[j]]);
			}
			final double[] scoresW = score(0,workSet);
			checkRecords(workSet,scoresW);
			int i = 0;
			while(i<scores.length) {
				for(int j=0;j<workSet.length;++j) {
					f[i] = workSet[j];
					scores[i] = scoresW[j];
					++i;
					if(i>scores.length) {
						break;
					}
				}
			}
		}
		// breed and score
		for(int g=1;g<=runPhases;++g) {
			final qtree[] workSet = new qtree[workN];
			for(int j=0;j<workN;++j) {
				final int p1 = rand.nextInt(f.length);
				final int p2 = rand.nextInt(f.length);
				workSet[j] = f[p1].breed(f[p2]);
			}
			final double[] news = score(g,workSet);
			checkRecords(workSet,news);
			// get into population
			final int ntry = 7;
			final int nmax = 3;
			for(int j=0;j<workN;++j) {
				int ins = 0;
				for(int t=0;t<ntry;++t) {
					final int v = rand.nextInt(nSlots);
					if(scores[v]<news[j]) {
						f[v] = workSet[j];
						scores[v] = news[j];
						++ins;
						if(ins>=nmax) {
							break;
						}
					}
				}
			}
		}
		//System.err.println("all done\t" + new Date());
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		(new mkPics()).doit();
	}

}
