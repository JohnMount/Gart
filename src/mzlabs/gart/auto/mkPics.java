package mzlabs.gart.auto;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;

import mzlabs.gart.AAElm;
import mzlabs.gart.Draw;
import mzlabs.gart.qtree;

public class mkPics {
	
	public static final File renderNice(final int idx, final qtree f) {
		final int w = 1024;
		final int h = 768;
		int aa = 5;
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
		final Image img = Draw.draw(f, w, h, 0.0, aaScheme);
		DecimalFormat decimalFormat = new DecimalFormat("000000");
		return Draw.writePNG(img,"picR"+decimalFormat.format(idx));
	}
	
	public static final double score(final qtree f) throws IOException, InterruptedException {
		final int w = 256;
		final int h = 256;
		int aa = 1;
		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));
		final Image img = Draw.draw(f, w, h, 0.0, aaScheme);
		final File file = Draw.writePNG(img,"picS");
		final Runtime r = Runtime.getRuntime();
		final Process proc = r.exec(new String[] { "python","./score.py","./picS.png","./picS.txt" });
		proc.waitFor();
		final File scoreF = new File("picS.txt");
		final BufferedReader rdr = new BufferedReader(new FileReader(scoreF));
		final String scoreS = rdr.readLine();
		final double scoreV = Double.parseDouble(scoreS.trim());
		rdr.close();
		file.delete();
		scoreF.delete();
		return scoreV;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Random rand = new Random(32588);
		final int nSlots = 10;
		final qtree[] f = new qtree[nSlots];
		final double[] scores = new double[nSlots];
		for(int i=0;i<nSlots;++i) {
			f[i] = qtree.rantree(7);
			scores[i] = score(f[i]);
		}
		double record = 0.0;
		int ri = 0;
		for(int j=0;j<100;++j) {
			final int p1 = rand.nextInt(nSlots);
			final int p2 = rand.nextInt(nSlots);
			final qtree newf = f[p1].breed(f[p2]);
			final double news = score(newf);
			for(int t=0;t<5;++t) {
				final int v = rand.nextInt(nSlots);
				if(scores[v]<news) {
					f[v] = newf;
					scores[v] = news;
					break;
				}
			}
			if(news>record) {
				record = news;
				++ri;
				final File fi = renderNice(ri,newf);
				System.out.println("" + ri + 
						"\t" + newf.toString() + 
						"\t" + fi.getAbsolutePath() + 
						"\t" + new Date());
			}
		}
	}

}
