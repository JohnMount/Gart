/**
 * 
 */
package mzlabs.gart;

abstract class MState {
	int n = 0;
	boolean diverged = false;
		
	public boolean diverged() {
		return diverged;
	}

	abstract public void step(final int maxSteps);
	
	public void assignColor(int[] r) {
		if(!diverged) {
			for(int i=0;i<3;++i) {
				r[i] = 255;
			}
		} else {
			final int color = (n+16*(n%16))%256;
			r[0] = 0;
			r[1] = color;
			r[2] = 0;
		}
	}
	
	private static final class SimpleMState extends MState {
		private final double cx;
		private final double cy;
		private double x;
		private double y;
	
		public SimpleMState(final double cx, final double cy) {
			this.cx = cx;
			this.cy = cy;
			x = cx;
			y = cy;
		}

		public void step(final int maxSteps) {
			for(int i=0;(i<maxSteps)&&(!diverged);++i) {
				// f(z) <- z^2 + c (in complex numbers)
				// z = x + y i, c = cx + cy i
				// f(z) <- (x^2 - y^2 + cx) + (2 x y + cy) i
				final double xsq = x*x;
				final double ysq = y*y;
				final double nsq = xsq + ysq;
				if(nsq>=4.0) {
					diverged = true;
				}
				n += 1;
				final double fx = xsq - ysq + cx;
				final double fy = 2.0*x*y + cy;
				//System.out.println("(" + x + "," + y + ") -> (" + fx + "," + fy + ") "  + normsq);
				x = fx;
				y = fy;
			}
		}
	}


	
	private static final class CompositeMState extends MState {
		final double cut;

		private final class SortedNum {
			private double l = 0.0;
			private double s = 0.0;
			
			public SortedNum(final double a, final double b) {
				l = a;
				s = b;
			}

			
			private void add(final double v) {
				if(Math.abs(v)>=cut) {
					l += v;
				} else {
					s += v;
				}
			}

			public void add(final SortedNum cx) {
				add(cx.l);
				add(cx.s);
			}

			public void sub(final SortedNum cx) {
				add(-cx.l);
				add(-cx.s);
			}
			
			public void mult(final SortedNum a, final SortedNum b) {
				clear();
				add(a.l*b.l);
				add(a.l*b.s);
				add(a.s*b.l);
				add(a.s*b.s);
			}
			
			public double value() {
				return l+s;
			}
			
			public void clear() {
				l = 0.0;
				s = 0.0;
			}
		}
		
		private final SortedNum cx;
		private final SortedNum cy;
		private SortedNum x;
		private SortedNum y;
	
		public CompositeMState(final double cut, final double cx, final double cy, final double dx, final double dy) {
			this.cut = cut;
			this.cx = new SortedNum(cx,dx);
			this.cy = new SortedNum(cy,dy);
			x = new SortedNum(0.0,0.0);
			x.add(this.cx);
			y = new SortedNum(0.0,0.0);
			y.add(this.cy);
		}

		public void step(final int maxSteps) {
			final SortedNum xx = new SortedNum(0.0,0.0);
			final SortedNum yy = new SortedNum(0.0,0.0);
			final SortedNum xy = new SortedNum(0.0,0.0);
			final SortedNum nx = new SortedNum(0.0,0.0);
			final SortedNum ny = new SortedNum(0.0,0.0);			
			for(int i=0;(i<maxSteps)&&(!diverged);++i) {
				// f(z) <- z^2 + c (in complex numbers)
				// z = x + y i, c = cx + cy i
				// f(z) <- (x^2 - y^2 + cx) + (2 x y + cy) i
				xx.mult(x,x);
				yy.mult(y,y);
				{
					final double nsq = xx.value() + yy.value();
					if(nsq>=4.0) {
						diverged = true;
					}
				}
				n += 1;
				xy.clear();
				nx.clear();
				ny.clear();
				nx.add(xx);
				nx.sub(yy);
				nx.add(cx);
				xy.mult(x,y);
				ny.add(xy);
				ny.add(xy);
				ny.add(cy);
				//System.out.println("(" + x + "," + y + ") -> (" + fx + "," + fy + ") "  + normsq);
				x.clear();
				y.clear();
				x.add(nx);
				y.add(ny);
			}
		}
	}
	
	public static MState newMState(final double cut, final double cx, final double cy, final double dx, final double dy) {
		if(true) {
			return new SimpleMState(cx+dx,cy+dy);
		} else {
			// too slow
			return new CompositeMState(cut,cx,cy,dx,dy);
		}
	}
}