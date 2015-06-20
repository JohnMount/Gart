package mzlabs.gart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import mzlabs.gart.Draw.QRunnable;
import mzlabs.gart.img.ImgNode;
import mzlabs.gart.util.CommandLineHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public final class Movie {
	
	public static final String STILLSUFFIX = ".png";
	public static final String PIC = "pic";
	public static final String frameDir = "frames";
	
	public static void waitForProc(Process proc) throws IOException, InterruptedException {
		InputStream stdStream = proc.getInputStream();
		int b = 0;
		final boolean echo = false;
		while((b=stdStream.read())>=0) {
			if(echo) {
				System.out.write(b);
			}
		}
		proc.waitFor();
	}
	
	public static final boolean cleanup = true;
	
	// unbelievable Java makes us do this
	static void copy(final File source, final File dest) throws IOException {
	     FileChannel in = null, out = null;
	     try {          
	          in = new FileInputStream(source).getChannel();
	          out = new FileOutputStream(dest).getChannel();
	 
	          long size = in.size();
	          MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
	          out.write(buf);
	     } finally {
	          if (in != null) {
	        	  in.close();
	          }
	          if (out != null) {
	        	  out.close();
	          }
	     }
	}

	public static final NumberFormat nf = new DecimalFormat("0000000000");
	public static final String ffNumberFormat = "%010d"; // must match nf

	public static void drawMovie(final String[] f, final File movFile, final boolean loop, 
			final AAElm[] aaScheme, final int width, final int height,
			final int nFrames, final double timeScale,
			final File audioFile) throws IOException, InterruptedException  {
		if(movFile.exists()) {
			movFile.delete();
		} else {
			movFile.mkdirs();
			movFile.delete();
		}
		final File workDir = File.createTempFile("gmovie","dir");
		if(workDir.exists()) {
			workDir.delete();
		}
		workDir.mkdirs();
		System.out.println("working in dir: '" + workDir.getAbsolutePath() + "\t" + new Date());
		final File stillDir = new File(workDir,frameDir);
		stillDir.mkdir();
		final int range = (nFrames-1)/2;
		final int overlapW = range/10;
		double[] z = new double[2*range+1];
		for(int ii=-range;ii<=range;++ii) {
			z[ii+range] = timeScale*ii/(double)range;
		}
		double[] overlap = new double[overlapW];
		for(int ii=range+1;ii<range+1+overlapW;++ii) {
			overlap[ii-(range+1)] = timeScale*ii/(double)range;
		}
		{
			String prev = null;
			int lastId = -1;
			ScheduledThreadPoolExecutor executer = new ScheduledThreadPoolExecutor(QRunnable.POOLSIZE);
			{
				int j = 0;
				for(final String current: f) {
					for(int ii=0;ii<z.length;++ii,++j) {
						final double zi = z[ii];
						final QRunnable qrun;
						final String dest = stillDir.getAbsolutePath() + "/" + PIC + nf.format(j);
						lastId = j;
						if((prev==null)||(ii>=overlapW)) {
							qrun = new QRunnable(new String[] { current }, new double[] { 1.0 }, width, height, new double[] { zi }, aaScheme,dest);
						} else {
							final double wt = (overlapW-ii)/(double)(overlapW+1);
							qrun = new QRunnable(new String[] { prev, current }, new double[] { wt, 1.0-wt }, width, height, new double[] {overlap[ii], zi}, aaScheme,dest);
						}
						QRunnable.addAndMaybeWait(executer,qrun);
					}
					prev = current;
				}
			}
			QRunnable.shutdownAndAwaitFinish(executer);
			if(loop) {
				int origFrame = lastId-1;
				for(int nextId=lastId+1;origFrame>=0;++nextId,--origFrame) {
					final File source = new File(stillDir,PIC + nf.format(origFrame) + STILLSUFFIX);
					final File dest = new File(stillDir,PIC + nf.format(nextId) + STILLSUFFIX);
					copy(source,dest);  // TODO: find some way to link or symbolic link here, java 7 nio Path can do this
				}
			}
		}
		FFMpeg.encodeFrames(workDir, movFile, width, height, 60, audioFile);
		if(cleanup) {
			// clean up (somewhat safely)
			System.out.println("clean up");
			final File[] frames =  stillDir.listFiles();
			for(final File fi: frames) {
				final String nm = fi.getName();
				if(nm.toLowerCase().endsWith(STILLSUFFIX)) {
					fi.delete();
				}
			}
			stillDir.delete();
			workDir.delete();
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("start\t" + new Date());
		CommandLineHandler clh = new CommandLineHandler("Movie");
		Option formDirArg = clh.addRArg("formDir","where to read input formulas");
		Option destDirArg = clh.addRArg("destDir","where to write result movies");
		Option sourceImgDirArg = clh.addOArg("srcImgs","directory of source images");
		Option wArg = clh.addOArg("width","result movie width in pixels");
		Option hArg = clh.addOArg("height","result movie height in pixels");
		Option nArg = clh.addOArg("nFrames","number of movie frames");
		Option aaArg = clh.addOArg("aaLevel","level of anti aliasing");
		Option scArg = clh.addOArg("timeScale","time scaling rate");
		Option adArg = clh.addOArg("audio","audio input file");
		CommandLine cmd = clh.parse(System.out, args);
		
		final File formDir = new File(cmd.getOptionValue(formDirArg.getOpt()));
		final File destDir = new File(cmd.getOptionValue(destDirArg.getOpt()));

		int w = 300; 
		int h = 200;
		int n = 401;
		int aa = 1;
		double timeScale = 1.0;
		if(cmd.getOptionValue(wArg.getOpt())!=null) {
			w = Integer.parseInt(cmd.getOptionValue(wArg.getOpt()));
		}
		if(cmd.getOptionValue(hArg.getOpt())!=null) {
			h = Integer.parseInt(cmd.getOptionValue(hArg.getOpt()));
		}
		if(cmd.getOptionValue(nArg.getOpt())!=null) {
			n = Integer.parseInt(cmd.getOptionValue(nArg.getOpt()));
		}
		if(cmd.getOptionValue(aaArg.getOpt())!=null) {
			aa = Integer.parseInt(cmd.getOptionValue(aaArg.getOpt()));
		}
		if(cmd.getOptionValue(scArg.getOpt())!=null) {
			timeScale = Double.parseDouble(cmd.getOptionValue(scArg.getOpt()));
		}
		
		File audioFile = null;
		if(cmd.getOptionValue(adArg.getOpt())!=null) {
			audioFile = new File(cmd.getOptionValue(adArg.getOpt()));
			if((!audioFile.exists())||(!audioFile.canRead())) {
				throw new Exception("can't read audio file: " + audioFile.getAbsolutePath());
			}
		}

		final AAElm[] aaScheme = AAElm.scheme(aa, new Random(66262));

		// define input images
		if(cmd.getOptionValue(sourceImgDirArg.getOpt())!=null) {
			final File imgSrcDir = new File(cmd.getOptionValue(sourceImgDirArg.getOpt()));
			if((!imgSrcDir.exists())||(!imgSrcDir.canRead())) {
				throw new Exception("can't image source: " + imgSrcDir.getAbsolutePath());
			}
			ArrayList<ImgNode> imgnodes = ImgNode.imgNodes(imgSrcDir,"Img");
			for(final ImgNode ni: imgnodes) {
				qtree.addOp(ni);
			}
		}
		
		final File[] list = formDir.listFiles();
		ArrayList<String> forms = new ArrayList<String>();
		for(final File fi: list) {
			final String nm = fi.getName();
			final String txtSuffix = ".txt";
			if(nm.endsWith(txtSuffix)) {
				LineNumberReader rdr = new LineNumberReader(new FileReader(fi));
				String line = rdr.readLine();
				rdr.close();
				forms.add(line);
			}
		}
		final File dest = new File(destDir,"gart.mov");
		drawMovie(forms.toArray(new String[0]),dest, false, aaScheme, w, h, n, timeScale, audioFile);
		System.out.println("all done\t" + new Date());
	}

}
