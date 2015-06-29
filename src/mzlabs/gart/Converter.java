package mzlabs.gart;

import java.awt.Color;

public final class Converter {
	public static Converter colorConverter = new Converter();
	private final double crunchBound = 30.0;
	public boolean useCorrectCrunch = false;
	
	/**
	 * Newer continuous crunch, works with uncrunch
	 * @param x
	 * @return
	 */
	private double correctCrunch(final double x) {
		if(x >= crunchBound) {
			return 1.0;
		} else  if(x <= -crunchBound) {
			return 0.0;
		} else {
			return 1.0 / (1.0 + Math.exp(-x));
		}
	}
	
	/** 
	 * Older buggy crunch, nice effect on farchive.flist[45] 
	 * ( iexp ( exp( iexp( isin( exp( iexp( isin( / j( / x+iy+jx+ky k ) ) ) ) ) ) ) ) )
	 * the so-called crab
	 * @param x
	 * @return
	 */
	private static double buggyCrunch(final double x) {
		if((x>30.0)||(x<-30.0)) {
			return 0.0;
		}
		return 1.0/(1.0+Math.exp(-x));
	}

	public double crunch(final double x) {
		if(useCorrectCrunch) {
			return correctCrunch(x);
		} else {
			return buggyCrunch(x);
		}
	}

	/**
	 * crunch(uncrunch(x))==x
	 * @param x
	 * @return
	 */
	public double uncrunch(final double y) {
		if(y>=1.0) {
			return crunchBound;
		} else if (y<=0.0) {
			return -crunchBound;
		} else {
			return -Math.log(1.0/y - 1.0);
		}
	}

	private float toCIndx(final double r) {
		if (r <= 0) {
			return 0;
		}
		if (r >= 1.0) {
			return 1;
		}
		return (float)r;
	}

	public Color toColor(final double[] v) {
		return new Color(toCIndx(v[0]), toCIndx(v[1]), toCIndx(v[2]));
	}
}
