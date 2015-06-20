package mzlabs.gart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class Fractal {
	final int width = 300; // 1024;// 600;
	final int height = 200; // 768; //400;
	int frameNum = 0;
	final File baseDir;
	final File stillDir;
	final AAElm[] aa = AAElm.scheme(7,new Random(325));



	Fractal() throws IOException {
		baseDir = File.createTempFile("fractal","dir");
		baseDir.delete();
		baseDir.mkdirs();
	    stillDir = new File(baseDir,Movie.frameDir);
	    stillDir.mkdirs();
	}
	
	public void cleanup() {
		{
			File[] l = stillDir.listFiles();
			for(File f : l) {
				f.delete();
			}
		}
		stillDir.delete();
		baseDir.delete();
	}
	
	
	private static final class Frame {
		public final double xOrigin;
		public final double yOrigin;
		public final int width;
		public final int height;
		public final double x0;
		public final double y0;
		public final double scale;
		
		public Frame(final double xOrigin, final double yOrigin, final int width, final int height, final double scale) {
			// want frame s.t. x(width/2) = xOrigin, y(height/2.0) = yOrigin, scale=s
			// (width/2)*scale + x = xOrigin, so x = xOrigin - scale*width/2, (sim) y = yOrigin -scale*height/2
			this.xOrigin = xOrigin;
			this.yOrigin = yOrigin;
			this.scale = scale;
			this.width = width;
			this.height = height;
			this.x0 = xOrigin-scale*width/2.0;
			this.y0 = yOrigin-scale*height/2.0;
		}
		
		public double xShift(final int xc) {
			return xc*scale;
		}
		
		public double yShift(final int yc) {
			return yc*scale;
		}

		public double x(final int xc) {
			return x0 + xShift(xc);
		}
		
		public double y(final int yc) {
			return y0 + yShift(yc);
		}
		
		public int xi(final double x) {
			return (int)Math.round((x-x0)/scale);
		}
		
		public int yi(final double y) {
			return (int)Math.round((y-y0)/scale);
		}
		
		public Frame scaled(final double s) {
			return new Frame(xOrigin,yOrigin,width,height,s);
		}
	}
	
	public static void compute(final Frame frame, MState[][] states) {
		final int recommendedSteps = 1024;
		final double x0 = frame.x0;
		final double y0 = frame.y0;
		for(int xc=0;xc<states.length;++xc) {
			final double xShift = frame.xShift(xc);
			for(int yc=0;yc<states[0].length;++yc) {
				final double yShift = frame.yShift(yc);
				final MState s = MState.newMState(frame.scale*2*(states.length+states[0].length),x0,y0,xShift,yShift);
				states[xc][yc] = s;
				s.step(recommendedSteps);
			}
		}
	}
	
	public File fName(final int i, final String suffix) {
		return new File(stillDir,Movie.PIC + Movie.nf.format(i) + suffix); 
	}
	
	public void render(final Frame frame, final Frame prevFrame, final MState[][] prevState) {
		Image image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();
		final Color black = new Color(0,0,0,255);
		g.setColor(black);
		g.fillRect(0,0,width,height);
		double[] clr = new double[3];
		int[] ctmp = new int[3];
		for(int xc=0;xc<width;++xc) {
			final double xBase = frame.x(xc);
			for(int yc=0;yc<height;++yc) {
				final double yBase = frame.y(yc);
				for(int i=0;i<3;++i) {
					clr[i] = 0.0;
				}
				double tw = 0.0;
				{
					final int pxc = prevFrame.xi(xBase);
					final int pyc = prevFrame.yi(yBase);
					final MState s = prevState[pxc][pyc];
					s.assignColor(ctmp);
					final double w = 0.1;
					for(int i=0;i<3;++i) {
						clr[i] += w*ctmp[i];
					}
					tw += w;
				}
				for(int ai=0;ai<aa.length;++ai) {
					final int pxc = prevFrame.xi(xBase + frame.scale*aa[ai].x);
					final int pyc = prevFrame.yi(yBase + frame.scale*aa[ai].y);
					if((pxc>=0)&&(pyc>=0)&&(pxc<prevState.length)&&(pyc<prevState[0].length)) {
						final MState s = prevState[pxc][pyc];
						s.assignColor(ctmp);
						for(int i=0;i<3;++i) {
							clr[i] += aa[ai].w*ctmp[i];
						}
						tw += aa[ai].w;
					}
				}
				for(int i=0;i<3;++i) {
					ctmp[i] = Math.max(0,Math.min(255,(int)Math.round(clr[i]/tw)));
				}
				Color c = new Color(ctmp[0],ctmp[1],ctmp[2],255);
				g.setColor(c);
				g.drawLine(xc,yc,xc,yc);
			}
		}
		final File fnName = fName(frameNum,"");
		Draw.writePNG(image,fnName.getAbsolutePath());
		++frameNum;
	}
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		System.out.println("start");
		Fractal f = new Fractal();
		final int frameCount = 50; // 5000;
		{
			// pick target point
			double ll = 0.0;
			double lu = 4.0;
			final double slopeY = 0.32366;
			while((lu-ll)>1.0e-16) {
				double p = (ll + lu)/2.0;
				double x = p;
				double y = slopeY*p;
				MState s = MState.newMState(1.0e-8,x,y,0,0);
				s.step(1024);
				if(!s.diverged()) {
					ll = p;
				} else {
					lu = p;
				}
			}
			final Frame drawFrame;
			final Frame computeFrame;
			{
				final double p = (ll + lu)/2.0;
				final double x0 = p;
				final double y0 = slopeY*p;
				final double s0 = 8.0/(double)Math.max(f.width,f.height);
				drawFrame = new Frame(x0,y0,f.width,f.height,s0);
				final int blowup = 3;
				computeFrame = new Frame(x0,y0,blowup*f.width,blowup*f.height,s0/(double)blowup);
			}
			System.out.println("have point: (" + drawFrame.xOrigin + "," + drawFrame.yOrigin + ")");
			final MState[][] state = new MState[computeFrame.width][computeFrame.height];
			final double pow = 0.995;  // takes about 138 applications to halve doing blowUp^2 worked (=25) so getting 5x savings
			final int subSteps = (int)Math.floor(Math.log(0.5)/Math.log(pow));
			while(f.frameNum<frameCount) {
				final double mult = Math.pow(pow,f.frameNum);
				final Frame cframe = computeFrame.scaled(mult*computeFrame.scale);
				Fractal.compute(cframe,state);
				{
					Frame rframe = drawFrame.scaled(mult*drawFrame.scale);
					f.render(rframe,cframe,state);
					System.out.println("frame " + f.frameNum);
				}
				for(int j=0;(f.frameNum<frameCount)&&(j<subSteps);++j) {
					final double dmult = Math.pow(pow,f.frameNum);
					Frame rframe = drawFrame.scaled(dmult*drawFrame.scale);
					f.render(rframe,cframe,state);
					System.out.println("\tframe " + f.frameNum);
				} 
			}
		}
		final boolean loop = true;
		if(loop) {
			final String STILLSUFFIX = ".png";
			int origFrame = frameCount-2;
			for(int nextId=frameCount;origFrame>=0;++nextId,--origFrame) {
				final File source = f.fName(origFrame,STILLSUFFIX);
				final File dest = f.fName(nextId,STILLSUFFIX);
				Movie.copy(source,dest);  // TODO: find some way to link or symbolic link here, java 7 nio Path can do this
			}
		}
		final File movFile = new File("fractalZoom.mpg");
		System.out.println("rendering movie to: " + movFile.getAbsolutePath());
		FFMpeg.encodeFrames(f.baseDir, movFile,f.width,f.height,60,null);
		f.cleanup();
		System.out.println("all done");
	}

}
