package mzlabs.gart.img;

import java.awt.image.Raster;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.media.jai.JAI;

import mzlabs.gart.Converter;
import mzlabs.gart.quaternion;
import mzlabs.gart.ops.qop;

public final class ImgNode extends qop {
	
	private static final double brate = 0.5;
	private static final double[][] blurKernel = { 
		{ Math.pow(brate,Math.sqrt(2.0)), brate, Math.pow(brate,Math.sqrt(2.0))},
		{ brate,  1.0, brate },
		{ Math.pow(brate,Math.sqrt(2.0)), brate, Math.pow(brate,Math.sqrt(2.0)) }
	};
	
	private static final class Payload {
		public final int w;
		public final int h;
		public final double stride;
		public final float[][][] colorDat; // [rgb][width][height]
		
		public Payload(final File f) {
			Raster r = JAI.create("fileload", f.getAbsolutePath()).getData();
			w = r.getWidth();
			h = r.getHeight();
			stride = 0.5*(double)Math.max(h,w);
			colorDat = new float[3][w][h];
			final int[] dArray = new int[3];
			if(blurKernel==null) {
				for(int i=0;i<w;++i) {
					for(int j=0;j<h;++j) {
						r.getPixel(i, j, dArray);
						for(int k=0;k<3;++k) {
							colorDat[k][i][j] = (float)dArray[k];
						}
					}
				}
			} else {
				final int bw = (blurKernel.length-1)/2;
				for(int i=0;i<w;++i) {
					for(int j=0;j<h;++j) {
						double wt = 0.0;
						for(int ni=-bw;ni<=bw;++ni) {
							if((i+ni>=0)&&(i+ni<w)) {
								for(int nj=-bw;nj<=bw;++nj) {
									if((j+nj>=0)&&(j+nj<h)) {
										final double wij = blurKernel[ni+bw][nj+bw];
										wt += wij;
										r.getPixel(i+ni, j+nj, dArray);
										for(int k=0;k<3;++k) {
											colorDat[k][i][j] += (float)(wij*dArray[k]);
										}
									}								
								}
							}
						}
						for(int k=0;k<3;++k) {
							colorDat[k][i][j] /= wt;
						}
					}
				}				
			}
		}
	}
	
	private static final class Cache extends LinkedHashMap<String,Payload> {
		public final int target;
		
		public Cache(final int target) {
			super(2*target,(float)0.5,true);
			this.target = target;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<String,Payload> eldest) {
			if(size()>target) {
				System.out.println("dump " + eldest.getKey());
				return true;
			} else {
				return false;
			}
		}
		
	}
	
	private static Cache cache = new Cache(50);

	private final File f;
	private final String nm;
	
	public ImgNode(final String nm, final File f) {
		this.f = f;
		this.nm = nm;
	}

	@Override
	public int degree() {
		return 0;
	}

	private static final double sq(double x) {
		return x*x;
	}
	
	// TODO: make a second img node that moves view point, rotates and scales.
	// TODO: make a parallel subst node: ( Psubst A b ) = ( op ( subst A b ) A ) 
	@Override
	public void dop(quaternion n, final quaternion a, final quaternion b, final double x,
			final double y, final double z) {
		Payload dat = null;
		synchronized (cache) {
			dat = cache.get(nm);
			if(dat==null) {
				dat = new Payload(f);
				System.out.println("load " + nm + "\t" + f.getAbsolutePath());
				cache.put(nm,dat);
			}
		}
		final int xcF = Math.max(0,Math.min(dat.w-1,(int)Math.floor(dat.w/2 + x*dat.stride)));
		final int ycF = Math.max(0,Math.min(dat.h-1,(int)Math.floor(dat.h/2 + y*dat.stride)));
		final double xcM = Math.max(0.0,Math.min(dat.w-1.0,(dat.w/2.0 + x*dat.stride)));
		final double ycM = Math.max(0.0,Math.min(dat.h-1.0,(dat.h/2.0 + y*dat.stride)));
		final int xcC = Math.max(0,Math.min(dat.w-1,(int)Math.ceil(dat.w/2 + x*dat.stride)));
		final int ycC = Math.max(0,Math.min(dat.h-1,(int)Math.ceil(dat.h/2 + y*dat.stride)));
		final double dFM = Math.sqrt(sq(xcF-xcM) + sq(ycF-ycM));
		final double dMC = Math.sqrt(sq(xcM-xcC) + sq(ycM-ycC));
		final double lambda = dFM/(dFM + dMC);
		n.e = 1.0;
		final double colRange = 255.0;
		n.i = Converter.colorConverter.uncrunch(((1.0-lambda)*dat.colorDat[0][xcF][ycF]+lambda*dat.colorDat[0][xcC][ycC])/colRange);
		n.j = Converter.colorConverter.uncrunch(((1.0-lambda)*dat.colorDat[1][xcF][ycF]+lambda*dat.colorDat[1][xcC][ycC])/colRange); 
		n.k = Converter.colorConverter.uncrunch(((1.0-lambda)*dat.colorDat[2][xcF][ycF]+lambda*dat.colorDat[2][xcC][ycC])/colRange);
	}

	@Override
	public String pname() {
		return nm;
	}
	
	private static final Set<String> targets = new HashSet<String>(Arrays.asList(new String[] {
			".tif", ".tiff", ".jpg", ".jpeg", ".png", ".gif"
	  }));
	
	private static void imgFiles(final File f, ArrayList<File> found) {
		if(f.exists()) {
			if(f.isDirectory()) {
				File[] s = f.listFiles();
				Arrays.sort(s);
				for(File si: s) {
					imgFiles(si,found);
				}
			} else {
				String nm = f.getName();
				int s = nm.lastIndexOf('.');
				if(s>0) {
					final String suff = nm.substring(s,nm.length()).toLowerCase();
					if(targets.contains(suff)) {
						found.add(f);
					}
				}
			}		
		}
	}
	
	public static ArrayList<ImgNode> imgNodes(final File dir, final String prefix) {
		ArrayList<File> found = new ArrayList<File>();
		Collections.sort(found);
		imgFiles(dir,found);
		ArrayList<ImgNode> ret = new ArrayList<ImgNode>(found.size());
		int i = 1;
		for(final File fi: found) {
			final String nm = prefix + i;
			ImgNode nd = new ImgNode(nm,fi);
			System.out.println("define image '" + nd.f.getAbsolutePath() + "' as node: " + nd.nm);
			ret.add(nd);
			++i;
		}
		return ret;
	}
}
