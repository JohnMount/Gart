package mzlabs.gart;

// $Header: /Users/johnmount/Documents/CVSRoot/mzlabs/src/mzlabs/gart/AAElm.java,v 1.4 2007/07/15 15:01:47 johnmount Exp $

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.util.Random;

public final class AAElm {
	public final double x;

	public final double y;

	public final double w;

	public AAElm(double x_in, double y_in, double w_in) {
		x = x_in;
		y = y_in;
		w = w_in;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("(");
		b.append(x);
		b.append(",");
		b.append(y);
		b.append(") ");
		b.append(w);
		return b.toString();
	}

	public static void print(AAElm[] l) {
		for (int i = 0; i < l.length; ++i) {
			System.out.println(l[i]);
		}
	}

	private static double[] estWts(double[] x_, double[] y_, Random rand) {
		double tot = x_.length;
		double[] w_ = new double[x_.length];
		// intial bias towards all regions
		for (int i = 0; i < w_.length; ++i) {
			w_[i] = 1.0;
		}
		if (x_.length <= 1) {
			return w_;
		}
		int passes = (int) (100 * (tot + 1) * (tot + 1));
		// slow est, but we assume x_.length is small
		for (int i = 0; i < passes; ++i) {
			double tx = 2.0 * (rand.nextDouble() - 0.5);
			double ty = 2.0 * (rand.nextDouble() - 0.5);
			int match = 0;
			double msq = (tx - x_[0]) * (tx - x_[0]) + (ty - y_[0])
					* (ty - y_[0]);
			for (int j = 1; j < x_.length; ++j) {
				double dsq = (tx - x_[j]) * (tx - x_[j]) + (ty - y_[j])
						* (ty - y_[j]);
				if (dsq < msq) {
					match = j;
					msq = dsq;
				}
			}
			w_[match] += 1.0;
			tot += 1;
		}
		double scale = 1.0 / tot;
		for (int i = 0; i < w_.length; ++i) {
			w_[i] *= scale;
		}
		return w_;
	}

	public static AAElm[] scheme(final int n, final Random rand) {
		if (n <= 1) {
			AAElm[] r = new AAElm[1];
			r[0] = new AAElm(0.0, 0.0, 1.0);
			return r;
		}
		double[] x_ = new double[n];
		double[] y_ = new double[n];
		for (int i = 0; i < n; ++i) {
			x_[i] = 2.0 * (rand.nextDouble() - 0.5);
			y_[i] = 2.0 * (rand.nextDouble() - 0.5);
		}
		double[] w_ = estWts(x_, y_, rand);
		AAElm[] r = new AAElm[n];
		for (int i = 0; i < n; ++i) {
			r[i] = new AAElm(x_[i], y_[i], w_[i]);
		}
		return r;
	}
}