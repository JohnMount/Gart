package mzlabs.gart;

import java.awt.Color;

public class Converter {
	public static Converter colorConverter = new Converter();

	private final double crunchBound = 30.0;
	
	public double crunch(final double x) {
		if(x >= crunchBound) {
			return 1.0;
		} else  if(x <= -crunchBound) {
			return 0.0;
		} else {
			return 1.0 / (1.0 + Math.exp(-x));
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
