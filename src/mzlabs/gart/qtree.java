package mzlabs.gart;

// $Header: /Users/johnmount/Documents/CVSRoot/mzlabs/src/mzlabs/gart/qtree.java,v 1.12 2007/07/15 15:01:48 johnmount Exp $

/**
 * Genetic art system. Copyright (C) 1995-2003 John Mount (j@mzlabs.com)
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import mzlabs.gart.ops.BasicOps;
import mzlabs.gart.ops.qop;
import mzlabs.gart.ops.BasicOps.qop_qaut1;
import mzlabs.gart.ops.BasicOps.qop_qaut2;
import mzlabs.gart.ops.BasicOps.qop_qc1;
import mzlabs.gart.ops.BasicOps.qop_qc2;
import mzlabs.gart.ops.BasicOps.qop_qc3;
import mzlabs.gart.ops.BasicOps.qop_qc4;
import mzlabs.gart.ops.BasicOps.qop_qc5;
import mzlabs.gart.ops.BasicOps.qop_qconj;
import mzlabs.gart.ops.BasicOps.qop_qcx;
import mzlabs.gart.ops.BasicOps.qop_qcx1;
import mzlabs.gart.ops.BasicOps.qop_qcxy;
import mzlabs.gart.ops.BasicOps.qop_qcxy2;
import mzlabs.gart.ops.BasicOps.qop_qcy;
import mzlabs.gart.ops.BasicOps.qop_qcy1;
import mzlabs.gart.ops.BasicOps.qop_qdiv;
import mzlabs.gart.ops.BasicOps.qop_qexp;
import mzlabs.gart.ops.BasicOps.qop_qfloor;
import mzlabs.gart.ops.BasicOps.qop_qiexp;
import mzlabs.gart.ops.BasicOps.qop_qilog;
import mzlabs.gart.ops.BasicOps.qop_qimax;
import mzlabs.gart.ops.BasicOps.qop_qimin;
import mzlabs.gart.ops.BasicOps.qop_qinv;
import mzlabs.gart.ops.BasicOps.qop_qisin;
import mzlabs.gart.ops.BasicOps.qop_qmod;
import mzlabs.gart.ops.BasicOps.qop_qmult;
import mzlabs.gart.ops.BasicOps.qop_qnorm;
import mzlabs.gart.ops.BasicOps.qop_qnormp;
import mzlabs.gart.ops.BasicOps.qop_qorth1;
import mzlabs.gart.ops.BasicOps.qop_qorth2;
import mzlabs.gart.ops.BasicOps.qop_qplus;
import mzlabs.gart.ops.BasicOps.qop_qrl;
import mzlabs.gart.ops.BasicOps.qop_qrr;
import mzlabs.gart.ops.BasicOps.qop_qstd;
import mzlabs.gart.ops.BasicOps.qop_qsub;
import mzlabs.gart.util.DStat;

public final class qtree {
	public static final Random rand = new Random();

	private final static Map<String,qop> symbolTable = new HashMap<String,qop>();
	private final static Map<Integer,ArrayList<qop>> degMap = new HashMap<Integer,ArrayList<qop>>();
	
	public static void addOp(final qop qi) {
		final String nm = normalizeStr(qi.pname());
		if(!symbolTable.containsKey(nm)) {
			symbolTable.put(nm,qi);
			final Integer d = qi.degree();
			ArrayList<qop> l = degMap.get(d);
			if(l==null) {
				l = new ArrayList<qop>();
				degMap.put(d,l);
			}
			l.add(qi);
		}
	}

	// add in standard ops
	private static final qop subst = new BasicOps.subst(); 
	static {
		final qop[] qops = {
			subst, new qop_qstd(),
			new qop_qplus(), new qop_qsub(),
			new qop_qmult(), new qop_qinv(), new qop_qdiv(), new qop_qconj(),
			new qop_qaut1(), new qop_qaut2(), new qop_qexp(), new qop_qfloor(),
			new qop_qmod(), new qop_qnorm(), new qop_qnormp(),
			new qop_qorth1(), new qop_qorth2(), new qop_qc1(), new qop_qc2(),
			new qop_qc3(), new qop_qc4(), new qop_qc5(), new qop_qcx(),
			new qop_qcy(), new qop_qcx1(), new qop_qcy1(), new qop_qcxy(),
			new qop_qcxy2(), new qop_qisin(), new qop_qilog(), new qop_qiexp(),
			new qop_qimin(), new qop_qimax(), new qop_qrl(), new qop_qrr(),
			//new ImgNode("img",new File("/Users/johnmount/GeneticArt/Images/001-100/006.TIF")) 
		};
		// standard ops
		for(final qop qi: qops) {
			addOp(qi);
		}
		// // image nodes
		//ArrayList<ImgNode> nodes = ImgNode.imgNodes(new File("/Users/johnmount/GeneticArt/Images"),"jimg");
		//for(final qop qi: nodes) {
		//	addOp(qi);
		//}
	}

	public final static qop default_op = new qop_qcxy();

	// tree-node variables
	private qop _op = null;

	private qtree _l = null;

	private qtree _r = null;

	private qtree _parent = null;

	public quaternion _v;

	// copy non- fields
	private qtree rclone(qtree p) {
		qtree r = null;
		r = new qtree();
		r._parent = p;
		r._op = _op;
		if (_l != null) {
			r._l = _l.rclone(r);
		}
		if (_r != null) {
			r._r = _r.rclone(r);
		}
		return r;
	}

	qtree() {
		_v = new quaternion();
	}
	
	
	private void rEvalTree(final double x, final double y, final double z) {
		double[] newCoords = null;
		if(_l!=null) {
			_l.rEvalTree(x, y, z);
			newCoords = _op.coords(_l._v);
		}
		if(_r!=null) {
			if(newCoords==null) {
				_r.rEvalTree(x, y, z);
			} else {
				_r.rEvalTree(newCoords[0],newCoords[1],newCoords[2]);
			}
		}
		_op.dop(_v, _l!=null?_l._v:null, _r!=null?_r._v:null, x, y, z);
	}
	
	public void getColor(double[] ret) {
		ret[0] = Converter.colorConverter.crunch(_v.getI());
		ret[1] = Converter.colorConverter.crunch(_v.getJ());
		ret[2] = Converter.colorConverter.crunch(_v.getK());		
	}

	private static String normalizeStr(String s) {
		s = s.toLowerCase();
		s = s.replace('+', '_');
		s = s.replaceAll("\\s+","");
		return s;
	}
	

	// attach first token to this node and recurse
	int parse(String s, int offset) {
		int a, b;

		// some safe defaults
		_l = null;
		_r = null;
		_op = default_op;
		if ((s == null) || (s.length() < 1)) {
			return offset;
		}
		a = offset;
		// skip whitespace
		while (a < s.length()) {
			char ch = s.charAt(a);
			if ((Character.isWhitespace(ch)) || (ch == '(') || (ch == ')')) {
				++a;
			} else {
				break;
			}
		}
		b = a;
		if (a < s.length()) {
			b = a + 1;
			while (b < s.length()) {
				char ch = s.charAt(b);
				if ((Character.isWhitespace(ch)) || (ch == '(') || (ch == ')')) {
					break;
				} else {
					++b;
				}
			}
			final String t = s.substring(a, b);
			_op = symbolTable.get(normalizeStr(t));
			if(_op==null) {
				_op = default_op;
			}
			if ((_op != null) && (_op.degree() > 0)) {
				_l = new qtree();
				_l._parent = this;
				b = _l.parse(s, b);
				if (_op.degree() > 1) {
					_r = new qtree();
					_r._parent = this;
					b = _r.parse(s, b);
				}
			}
		}
		return b;
	}

	// print tree, prints to stdout
	public void printtree() {
		String s = toString();
		System.out.println(s);
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringB(b);
		return b.toString();
	}

	// tree to string, needs final println() after done
	private void toStringB(StringBuilder b) {
		qop q = null;
		if (this != null) {
			q = _op;
		}
		if (q == null) {
			b.append("null");
		} else {
			if (q.degree() == 0) {
				b.append(q.pname());
			} else if (q.degree() == 1) {
				b.append("( ");
				b.append(q.pname());
				if (_l != null) {
					b.append(" ");
					_l.toStringB(b);
				}
				b.append(" )");
			} else {
				b.append("( ");
				b.append(q.pname());
				if (_l != null) {
					b.append(" ");
					_l.toStringB(b);
				}
				if (_r != null) {
					b.append(" ");
					_r.toStringB(b);
				}
				b.append(" )");
			}
		}
	}

	public qtree(String s) {
		_v = new quaternion();
		parse(s, 0);
		// printtree();
	}

	// side effect variable nodes() is allowed to set
	private static qtree lastmatch = null;

	// count non-null nodes in tree, inorder traversal made obvious
	// also can pick up a matching node (count starts at 0)
	// allowed to alter lastmatch variable
	// call with nodes(0,target)
	// sets last match as a side-effect
	public int nodes(int start, int target) {
		qop q = _op;
		if (q == null) {
			return start;
		}
		int d = q.degree();
		if (d > 0) {
			start = _l.nodes(start, target);
		}
		if (start == target) {
			// found matching node
			lastmatch = this;
		}
		start += 1; //self
		if (d > 1) {
			start = _r.nodes(start, target);
		}
		return start;
	}

	// get a random op of given degree, -1 means any degree
	static qop randop(final int d) {
		ArrayList<qop> l = degMap.get(d);
		if(l==null) {
			int n = degMap.size();
			Integer[] ds = degMap.keySet().toArray(new Integer[n]);
			l = degMap.get(ds[rand.nextInt(n)]);
		}
		int n = l.size();
		return l.get(rand.nextInt(n));
	}

	// create a new tree
	public qtree breed(qtree t) {
		// maybe whole new tree
		if (rand.nextDouble() < .3) {
			return new qtree(8);
		}
		return breedP(t);
	}

	public qtree breedP(qtree t) {
		return breedP(this, t);
	}

	public static qtree breedP(final qtree a, final qtree t) {
		qtree r = null;
		qtree t2 = null;
		
		if(rand.nextBoolean()) {
			r = new qtree();
			r._op = subst;
			if(rand.nextBoolean()) {
				r._l = a.rclone(null);
				r._r = t.rclone(null);
			} else {
				r._r = a.rclone(null);
				r._l = t.rclone(null);
			}
			r._l._parent = r;
			r._r._parent = r;
			return r;
		}

		if (rand.nextBoolean()) {
			r = a.rclone(null);
			t2 = t;
		} else {
			r = t.rclone(null);
			t2 = a;
		}

		final qtree q1;
		{
			final int s1 = r.nodes(0, -1);
			final int r1 = rand.nextInt(s1);
			r.nodes(0, r1);
			q1 = qtree.lastmatch;
		}

		final qtree q2;
		{
			final int s2 = t2.nodes(0, -1);
			final int r2 = rand.nextInt(s2);
			t2.nodes(0, r2);
			q2 = qtree.lastmatch;
		}

		// do the graft
		boolean isleft = true;
		qtree par = q1._parent;
		qtree st = q2.rclone(null);
		if (par != null) {
			if (par._l == q1) {
				par._l = null;
				isleft = true;
			} else {
				par._r = null;
				isleft = false;
			}
		}
		// System.out.println();
		// System.out.println("host");
		// r.printtree();
		// System.out.println("donation");
		// st.printtree();
		if (par != null) {
			if (isleft) {
				par._l = st;
			} else {
				par._r = st;
			}
		}
		st._parent = par;
		// System.out.println("result");
		// r.printtree();

		if(rand.nextBoolean()) {
			// mutation
			final int s1 = r.nodes(0, -1);
			final int r1 = rand.nextInt(s1);
			r.nodes(0, r1);
			qtree qhit = qtree.lastmatch;
			if((qhit._op.degree()==2)&&(rand.nextBoolean())) {
				qhit._op = subst;
			} else {
				qhit._op = randop(qhit._op.degree());
			}
		}

		return r;
	}

	public static qtree newTree(String f) {
		return new qtree(f);
	}

	public static qtree rantree(int l) {
		return new qtree(l);
	}

	// breed a tree
	public static qtree sbreed(String s1, String s2) {
		qtree t1 = new qtree(s1);
		qtree t2 = new qtree(s2);
		qtree t3 = t1.breed(t2);
		return t3;
	}

	// random tree- nast probability model (not much care)
	// or go to arcive
	qtree(int l) {
		_v = new quaternion();
		if (rand.nextBoolean()) {
			//System.out.println("// formula source = random");
			r_rantree(l, null);
		} else {
			//System.out.println("// formula source = archive");
			final int n = farchive.flist.length;
			final int k = rand.nextInt(n);
			parse(farchive.flist[k], 0);
		}
	}

	void r_rantree(int l, qtree p) {
		_parent = p;
		if (l <= 0) {
			// terminate tree
			_op = randop(0);
		} else {
			// pick degree of node, prefer degree 2,1 over zero
			int[] degs = { 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0 };
			final int d = degs[rand.nextInt(degs.length)];
			_op = randop(d);
		}
		if (_op.degree() > 0) {
			_l = new qtree();
			_l.r_rantree(l - 1, this);
			if (_op.degree() > 1) {
				_r = new qtree();
				_r.r_rantree(l - 1, this);
			}
		}
	}

	public void picfromform(final int pwidth, final int pheight, final double z, final AAElm[] scheme, final Image img) {
		picfromform(new qtree[] { this }, new double[] { 1.0 }, pwidth, pheight, new double[] {z}, scheme, img);
	}
	
	
	/**
	 * Multiple formulas is to support transitions and fades
	 * @param formula
	 * @param wt
	 * @param pwidth
	 * @param pheight
	 * @param z
	 * @param scheme
	 * @param img
	 */
	public static void picfromform(final qtree[] formula, final double[] wt, 
			final int pwidth, final int pheight, final double[] z, final AAElm[] scheme, final Image img) {
		final Graphics g = img.getGraphics();
		if ((formula == null)||(formula.length<=0)) {
			Color c2 = new Color(127, 0, 0);
			g.setColor(c2);
			g.fillRect(0, 0, pwidth, pheight);
		} else {
			final int span;
			if (pwidth >= pheight) {
				span = pheight;
			} else {
				span = pwidth;
			}
			final double stride = 1.0 / (double) span;
			// compile the trees
			final int nformulae = formula.length;
			// evaluate
			final double[][][] r = new double[pheight][pwidth][3];
			double[][] subpixelVar = null;
			final int nscheme = scheme.length;
			if(nscheme>1) {
				subpixelVar = new double[pheight][pwidth];
			}
			{
				final double[] rkr = new double[3];
				final double[] rks = new double[3];
				DStat[] meanV = null;
				if(subpixelVar!=null) {
					meanV = DStat.array(3);
				}
				for (int i = 0; i < pwidth; ++i) {
					final double x = (i - pwidth * 0.5) * stride;
					for (int j = 0; j < pheight; ++j) {
						final double y = (j - pheight * 0.5) * stride;
						if(meanV!=null) {
							DStat.clear(meanV);
						}
						for (int k = 0; k < nscheme; ++k) {
							Arrays.fill(rks,0.0);
							final double tx = x + scheme[k].x * stride * 0.5;
							final double ty = y + scheme[k].y * stride * 0.5;
							for(int fi=0;fi<nformulae;++fi) {
								formula[fi].rEvalTree(tx, ty, z[fi]);
								formula[fi].getColor(rkr);
								for (int t = 0; t < 3; ++t) {
									rks[t] += rkr[t]*wt[fi];
								}
							}
							for (int t = 0; t < 3; ++t) {
								if(meanV!=null) {
									meanV[t].observe(rks[t],scheme[k].w);
								}
								r[j][i][t] += scheme[k].w*rks[t];
							}
						}
						if(meanV!=null) {
							double v0 = meanV[0].getVariance();
							double v1 = meanV[1].getVariance();
							double v2 = meanV[2].getVariance();
							subpixelVar[j][i] = Math.max(v0,Math.max(v1,v2));
						}
					}
				}
			}
			// here we have the image in r, so we can compute stats here if we want
			// Apparently: apply some sort of adaptive blurring scheme (it has been a while)
			final int processingRadius = 5;
			final double epsilon = 1.0e-3;
			final double[] smoothed = new double[3];
			final DStat varEst = new DStat();
			for (int i = 0; i < pwidth; ++i) {
				for (int j = 0; j < pheight; ++j) {
					final int radiussq;
					Arrays.fill(smoothed,0.0);
					if(subpixelVar!=null) {
						varEst.clear();
						for(int ii=Math.max(0,i-processingRadius);ii<=Math.min(pwidth-1,i+processingRadius);++ii) {
							for(int jj=Math.max(0,j-processingRadius);jj<=Math.min(pheight-1,j+processingRadius);++jj) {
								final double distsq = (i-ii)*(i-ii) + (j-jj)*(j-jj);
								if(distsq<=processingRadius*processingRadius) {
									final double obswt = Math.exp(-2.0*(distsq+epsilon)/(double)(processingRadius*processingRadius+epsilon));
									varEst.observe(subpixelVar[jj][ii],obswt);
								}
							}
						}
						final double useVar = varEst.getMean();
						radiussq = Math.max(1,Math.min(processingRadius,(int)Math.round(useVar/0.05)));
					} else {
						radiussq = 1;
					}
					double den = 0.0;
					for(int ii=Math.max(0,i-radiussq);ii<=Math.min(pwidth-1,i+radiussq);++ii) {
						for(int jj=Math.max(0,j-radiussq);jj<=Math.min(pheight-1,j+radiussq);++jj) {
							final double distsq = (i-ii)*(i-ii) + (j-jj)*(j-jj);
							if(distsq<=radiussq+epsilon) {
								final double smoothwt = Math.exp(-2.0*(distsq+epsilon)/(double)(radiussq+epsilon));
								den += smoothwt;
								for(int cc=0;cc<3;++cc) {
									smoothed[cc] += smoothwt*r[jj][ii][cc];
								}
							}
						}
					}
					for(int cc=0;cc<3;++cc) {
						smoothed[cc] /= den;
					}
					final Color c = Converter.colorConverter.toColor(smoothed);
					g.setColor(c);
					// fix: use some other method than rect
					g.fillRect(i, j, 1, 1);
				}
			}
		}
	}
	
}

