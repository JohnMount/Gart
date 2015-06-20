package mzlabs.gart.ops;

import mzlabs.gart.Converter;
import mzlabs.gart.quaternion;

public class BasicOps {
	// long list of operators
	
	public static class qop_qplus extends qop {
		public String pname() {
			return "+";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qplus(a, b);
		}
	}

	public static class qop_qsub extends qop {
		public String pname() {
			return "-";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qsub(a, b);
		}
	}

	public static class qop_qmult extends qop {
		public String pname() {
			return "*";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qmult(a, b);
		}
	}

	public static class qop_qinv extends qop {
		public String pname() {
			return "inv";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qinv(a);
		}
	}

	public static class qop_qdiv extends qop {
		public String pname() {
			return "/";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qdiv(a, b);
		}
	}

	public static class qop_qconj extends qop {
		public String pname() {
			return "conj";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qconj(a);
		}
	}

	public static class qop_qaut1 extends qop {
		public String pname() {
			return "A1";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qaut1(a, b);
		}
	}

	public static class qop_qaut2 extends qop {
		public String pname() {
			return "A2";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qaut2(a, b);
		}
	}

	public static class qop_qexp extends qop {
		public String pname() {
			return "exp";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qexp(a);
		}
	}

	public static class qop_qfloor extends qop {
		public String pname() {
			return "floor";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qfloor(a);
		}
	}

	public static class qop_qmod extends qop {
		public String pname() {
			return "mod";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qmod(a, b);
		}
	}

	public static class qop_qnorm extends qop {
		public String pname() {
			return "normalize";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qnorm(a);
		}
	}

	public static class qop_qnormp extends qop {
		public String pname() {
			return "normp";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qnormp(a);
		}
	}

	public static class qop_qorth1 extends qop {
		public String pname() {
			return "orth1";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qorth1(a, b);
		}
	}

	public static class qop_qorth2 extends qop {
		public String pname() {
			return "orth2";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qorth2(a, b);
		}
	}

	public static class qop_qc1 extends qop {
		public String pname() {
			return "1";
		}

		public int degree() {
			return 0;
		}

		public boolean isconst() {
			return true;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qc1();
		}
	}

	public static class qop_qc2 extends qop {
		public String pname() {
			return "i";
		}

		public int degree() {
			return 0;
		}

		public boolean isconst() {
			return true;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qc2();
		}
	}

	public static class qop_qc3 extends qop {
		public String pname() {
			return "j";
		}

		public int degree() {
			return 0;
		}

		public boolean isconst() {
			return true;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qc3();
		}
	}

	public static class qop_qc4 extends qop {
		public String pname() {
			return "k";
		}

		public int degree() {
			return 0;
		}

		public boolean isconst() {
			return true;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qc4();
		}
	}

	public static class qop_qc5 extends qop {
		public String pname() {
			return "golden";
		}

		public int degree() {
			return 0;
		}

		public boolean isconst() {
			return true;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qc5();
		}
	}

	public static class qop_qstd extends qop {
		public String pname() {
			return "std";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.e = 0.0;
			n.i = Converter.colorConverter.uncrunch(0.5*(x + 1.0));
			n.j = Converter.colorConverter.uncrunch(0.5*(y + 1.0));
			n.k = Converter.colorConverter.uncrunch(0.5*(z + 1.0));
		}
	}

	public static class qop_qcx extends qop {
		public String pname() {
			return "x";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcx(x, y, z);
		}
	}

	public static class qop_qcy extends qop {
		public String pname() {
			return "y";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcy(x, y, z);
		}
	}

	public static class qop_qcx1 extends qop {
		public String pname() {
			return "x_k";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcx1(x, y, z);
		}
	}

	public static class qop_qcy1 extends qop {
		public String pname() {
			return "y_k";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcy1(x, y, z);
		}
	}

	public static class qop_qcxy extends qop {
		public String pname() {
			return "x_iy";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcxy(x, y, z);
		}
	}

	public static class qop_qcxy2 extends qop {
		public String pname() {
			return "x_iy_jx_ky";
		}

		public int degree() {
			return 0;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qcxy2(x, y, z);
		}
	}

	public static class qop_qisin extends qop {
		public String pname() {
			return "isin";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qisin(a);
		}
	}

	public static class qop_qilog extends qop {
		public String pname() {
			return "ilog";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qilog(a);
		}
	}

	public static class qop_qiexp extends qop {
		public String pname() {
			return "iexp";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qiexp(a);
		}
	}

	public static class qop_qimin extends qop {
		public String pname() {
			return "imin";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qimin(a, b);
		}
	}

	public static class qop_qimax extends qop {
		public String pname() {
			return "imax";
		}

		public int degree() {
			return 2;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qimax(a, b);
		}
	}

	public static class qop_qrl extends qop {
		public String pname() {
			return "rolL";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qrl(a);
		}
	}

	public static class qop_qrr extends qop {
		public String pname() {
			return "rolR";
		}

		public int degree() {
			return 1;
		}

		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.qrr(a);
		}
	}

	public static class subst extends qop {
		public String pname() {
			return "subst";
		}
	
		@Override
		public final int degree() {
			return 2;
		}
	
		private static double xform(final double x) {
			return x - Math.floor(x + 0.5);
		}
		
		@Override
		public double[] coords(quaternion x) {
			double[] r = new double[3];
			r[0] = xform(x.e);
			r[1] = xform(x.i);
			r[2] = xform(x.j);
			return r;			
		}
		
		public void dop(quaternion n, quaternion a, quaternion b, double x, double y, double z) {
			n.set(b);
		}
	}
}
