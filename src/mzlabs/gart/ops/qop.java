/**
 * 
 */
package mzlabs.gart.ops;

import mzlabs.gart.quaternion;

public abstract class qop {
	public abstract String pname();

	public abstract int degree();

	public boolean isconst() {
		return false;
	}
	
	public double[] coords(quaternion x) {
		return null;
	}
	
	public abstract void dop(quaternion n, quaternion a, quaternion b, 
			double x, double y, double z);
}