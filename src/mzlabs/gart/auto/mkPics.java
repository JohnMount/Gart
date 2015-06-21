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
	
	double record = 0.0;
	int ri = 0;
	
	/**
	 * score a batch so we can be parallel in producing images (not done yet) and also can
	 * pay scoring start up cost less often.
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public final double[] score(final qtree[] f) throws IOException, InterruptedException {
		final int w = 256;
		final int h = 256;
		final int aa = 1;
		final DecimalFormat decimalFormat = new DecimalFormat("000000");
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
		// TODO: parallelize this step
		final ArrayList<String> command = new ArrayList<String>();
		command.add("python");
		command.add("./score.py");
		final File[] files = new File[f.length];
		for(int i=0;i<f.length;++i) {
			final Image img = Draw.draw(f[i], w, h, 0.0, aaScheme);
			final String nmI = "picS"+decimalFormat.format(i);
			command.add("./"+nmI);
			final File file = Draw.writePNG(img,nmI);
			files[i] = file;
		}
		final String resName = "picS.txt";
		command.add("./" + resName);
		final Runtime r = Runtime.getRuntime();
		final Process proc = r.exec((String[])command.toArray(new String[] {}));
		proc.waitFor();
		final File scoreF = new File(resName);
		final BufferedReader rdr = new BufferedReader(new FileReader(scoreF));
		final double[] scores = new double[f.length];
		for(int i=0;i<f.length;++i) {
			final String scoreS = rdr.readLine();
			final double scoreV = Double.parseDouble(scoreS.trim());
			scores[i] = scoreV;
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
		rdr.close();
		for(final File file: files) {
			file.delete();
		}
		scoreF.delete();
		return scores;
	}
	
	
	
	public void doit() throws IOException, InterruptedException {
		final Random rand = new Random(32588);
		final int workN = 20;
		final int nSlots = 20*workN;
		final int runPhases = 10000;
		final qtree[] workSet = new qtree[workN];
		final qtree[] f = new qtree[nSlots];
		final double[] scores = new double[nSlots];
		
		System.out.println("" + "recordNumber" + 
				"\t" + "formula" + 
				"\t" + "score" +
				"\t" + "imagePath" + 
				"\t" + "when");
		
		for(int i=0;i<nSlots;++i) {
			f[i] = qtree.rantree(7);
		}
		{
			int i = 0;
			for(int j=0;j<workN;++j) {
				workSet[j] = qtree.rantree(7);
				f[i+j] = workSet[j];
			}
			final double[] news = score(workSet);
			for(int j=0;j<workN;++j) {
				scores[i+j] = news[j];
			}
		}
		for(int step=0;step<runPhases;++step) {
			for(int j=0;j<workN;++j) {
				final int p1 = rand.nextInt(nSlots);
				final int p2 = rand.nextInt(nSlots);
				workSet[j] = f[p1].breed(f[p2]);
			}
			final double[] news = score(workSet);
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
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		(new mkPics()).doit();
	}

}
