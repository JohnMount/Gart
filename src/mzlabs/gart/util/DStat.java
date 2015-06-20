package mzlabs.gart.util;

/**
 * stat base
 *   Copyright (C) 1995-2003  John Mount, Nina Zumel (j@mzlabs.com)
 */

/**
 * DObserver that computes,min,max,mean,stddev.
 */
public final class DStat  {
	private double min;

	private double max;

	private double sumW;

	private double sumWx;

	private double sumWxx;

	private int nObs;

	private int nBad;

	public DStat() {
		clear();
	}

	/**
	 * Returns a copy of dstat.
	 */
	public DStat(DStat dstat) {
		min = dstat.min;
		max = dstat.max;
		sumW = dstat.sumW;
		sumWx = dstat.sumWx;
		sumWxx = dstat.sumWxx;
		nObs = dstat.nObs;
		nBad = dstat.nBad;
	}

	public void clear() {
		min = Double.NaN;
		max = Double.NaN;
		sumW = 0.0;
		sumWx = 0.0;
		sumWxx = 0.0;
		nObs = 0;
		nBad = 0;
	}

	/**
	 * @param x
	 *            value to observe
	 * @param w
	 *            weight of observation (usually you set this to 1.0)
	 */
	public void observe(double x, double w) {
		++nObs;
		if ((Double.isNaN(x)) || (Double.isInfinite(x)) || (Double.isNaN(w))
				|| (Double.isInfinite(w)) || (w <= 0.0)) {
			++nBad;
			return;
		}
		if (Double.isNaN(min)) {
			min = x;
			max = x;
		} else {
			if (x < min) {
				min = x;
			} else if (x > max) {
				max = x;
			}
		}
		sumW += w;
		sumWx += w * x;
		sumWxx += w * x * x;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public int getNObs() {
		return nObs;
	}

	public int getNBad() {
		return nBad;
	}

	public double getTotalWeight() {
		return sumW;
	}

	public double getTotal() {
		return sumWx;
	}

	public double getSumOfSquares() {
		return sumWxx;
	}

	public double getMean() {
		if (sumW <= 0.0) {
			return Double.NaN;
		}
		return sumWx / sumW;
	}

	private static double sq(double x) {
		return x * x;
	}

	/**
	 * define avg(g) = sum_i w_i*x_i / sum w_i define n = sum_i 1
	 * 
	 * @return (n/(n-1)) sum_i w_i (x_i - avg(x)) / sum w_i
	 */
	public double getVariance() {
		int n = nObs - nBad;
		if ((n <= 1) || (sumW <= 0.0)) {
			return Double.NaN;
		}
		return Math.max(0.0, sumWxx / sumW - sq(sumWx / sumW));
	}

	public double getStdDev() {
		double r = getVariance();
		if (Double.isNaN(r)) {
			return r;
		}
		return Math.sqrt(r);
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("DStat:");
		if (nObs - nBad > 0) {
			b.append(" mean: ");
			b.append(getMean());
			b.append(" stddev: ");
			b.append(getStdDev());
		} else {
			b.append(" no good observations");
		}
		if (nBad > 0) {
			b.append(" badobs:");
			b.append(nBad);
		}
		return b.toString();
	}

	public static DStat[] array(int n) {
		DStat[] r =  new DStat[n];
		for(int i=0;i<n;++i) {
			r[i] = new DStat();
		}
		return r;
	}
	
	public static void clear(DStat[] r) {
		for(DStat ri: r) {
			ri.clear();
		}
	}
}

