package mzlabs.gart.util;

/**
 * Object loader Copyright (C) 2001-2003 John Mount, Nina Zumel (j@mzlabs.com)
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public final class XMLSerializer {
	/**
	 * write JDOM as XML to String
	 * 
	 * @param root
	 *            JDom element to write as document
	 * @return string
	 */
	public static String writeToString(Document doc) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();  // TODO: buffer
		PrintStream ps = new PrintStream(bs);
		writeToStream(ps, doc);
		return bs.toString();
	}

	/**
	 * write JDOM as XML to String
	 * 
	 * @param root
	 *            JDom element to write as document
	 * @return string
	 */
	public static String writeToString(Element root) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();  // TODO: buffer
		PrintStream ps = new PrintStream(bs);
		writeToStream(ps, root);
		return bs.toString();
	}

	/**
	 * write JDOM as XML to file
	 * 
	 * @param os
	 *            stream to write to
	 * @param doc
	 *            JDom document to write
	 */
	public static void writeToStream(PrintStream os, Document doc) {
		try {
			XMLOutputter printer = new XMLOutputter(Format.getPrettyFormat());
			printer.output(doc, os);
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
	}

	/**
	 * write JDOM as XML to file
	 * 
	 * @param os
	 *            stream to write to
	 * @param root
	 *            JDom element to write as document
	 */
	public static void writeToStream(PrintStream os, Element root) {
		try {
			XMLOutputter printer = new XMLOutputter(Format.getPrettyFormat());
			printer.output(root, os);
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
	}

	/**
	 * write JDOM as XML to file
	 * 
	 * @param fname
	 *            file to write to
	 * @param doc
	 *            JDom document to write
	 */
	public static void writeToFile(String fname, Document doc) {
		try {
			FileOutputStream os = new FileOutputStream(fname);
			writeToStream(new PrintStream(os), doc);
			os.close();
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
	}

	/**
	 * write JDOM as XML to file
	 * 
	 * @param fname
	 *            file to write to
	 * @param root
	 *            JDom element to write as document
	 */
	public static void writeToFile(String fname, Element root) {
		try {
			FileOutputStream os = new FileOutputStream(fname);
			writeToStream(new PrintStream(os), root);
			os.close();
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
	}

	/**
	 * read JDOM document from XML input stream
	 * 
	 * @param is
	 *            stream to read from
	 * @return JDOM document
	 */
	public static Document readFromStream(InputStream is) {
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(is);
		} catch (JDOMException e) {
			System.out.println("input is not well-formed.");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
		return null;
	}

	/**
	 * read JDOM document from XML file
	 * 
	 * @param fname
	 *            file to read from
	 * @return JDOM document
	 */
	public static Document readFromFile(String fname) {
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(new File(fname));
		} catch (JDOMException e) {
			System.out.println(fname + " is not well-formed.");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
		return null;
	}

	/**
	 * convert object into JDOM representation
	 * 
	 * @param nm
	 *            name to give this element
	 * @param o
	 *            Object to transform into element
	 * @return JDOM element (serialized object)
	 */
	public static Element toElement(String nm, Object o) throws Exception {
		XMLSerializer xmls = new XMLSerializer(null);
		LinkedList dups = new LinkedList();
		return xmls.toElementX(nm, o, dups);
	}

	/**
	 * convert JDOM element into an object
	 * 
	 * @param m
	 *            JDOM element to convert
	 * @param control_
	 *            (optional) list of allowed object types
	 * @return new object (deserialized object)
	 */
	public static Object toObject(Element m, ClassControl control_)
			throws Exception {
		XMLSerializer xmls = new XMLSerializer(control_);
		return xmls.toObjectX(m);
	}

	/**
	 * convert JDOM element into an object
	 * 
	 * @param d
	 *            JDOM document to convert
	 * @param control_
	 *            (optional) list of allowed object types
	 * @return new object (deserialized object)
	 */
	public static Object toObject(Document d, ClassControl control_)
			throws Exception {
		Element m = d.getRootElement();
		return toObject(m, control_);
	}

	private ClassControl control;

	private XMLSerializer(ClassControl control_) {
		control = control_;
	}

	private static void pushObject(Object o, LinkedList dups) throws Exception {
		if (o == null) {
			return;
		}
		boolean contains = false;
		if (!dups.isEmpty()) {
			Iterator it = dups.iterator();
			while ((!contains) && (it.hasNext())) {
				Object oi = it.next();
				if (o == oi) {
					contains = true;
				}
			}
		}
		if (contains) {
			throw new Exception("cyclic object reference");
		}
		dups.addLast(o);
	}

	private static void popObject(Object o, LinkedList dups) {
		if (o == null) {
			return;
		}
		dups.removeLast();
	}

	private static boolean passThrough(Object val) {
		return (val == null) || (val.getClass().isPrimitive())
				|| (val instanceof Integer) || (val instanceof Long)
				|| (val instanceof Float) || (val instanceof Double)
				|| (val instanceof Boolean) || (val instanceof String)
				|| (val instanceof Byte) || (val instanceof Character);
	}

	public static final String fieldPrefix = "ser.";

	public static final String classNameField = fieldPrefix + "className";

	public static final String fieldName = fieldPrefix + "field";

	public static final String payloadField = fieldPrefix + "value";

	public static final String arrayEltField = fieldPrefix + "arrayelt";

	public static final String mapEltField = fieldPrefix + "mapelt";

	public static final String mapEltFieldKey = fieldPrefix + "mapeltkey";

	public static final String mapEltFieldVal = fieldPrefix + "mapeltval";

	public static final String collectionEltField = fieldPrefix
			+ "collectionelt";

	static SortedMap getFieldMap(Class c) {
		SortedMap flist = new TreeMap();
		while (c != null) {
			Field[] flds = c.getDeclaredFields();
			if (flds != null) {
				for (int i = 0; i < flds.length; ++i) {
					int mod = flds[i].getModifiers();
					if ((!Modifier.isStatic(mod))
							&& (!Modifier.isTransient(mod))) {
						String fn = null;
						try {
							fn = flds[i].getName();
						} catch (Exception e) {
						}
						if ((fn != null) && (!flist.containsKey(fn))) {
							flist.put(fn, flds[i]);
						}
					}
				}
			}
			c = c.getSuperclass();
		}
		return flist;
	}

	private Element objectToElement(Element e, String nm, Object o,
			LinkedList dups) throws Exception {
		SortedMap fieldList = getFieldMap(o.getClass());
		Iterator fit = fieldList.entrySet().iterator();
		while (fit.hasNext()) {
			Map.Entry me = (Map.Entry) fit.next();
			String fn = (String) me.getKey();
			Field fi = (Field) me.getValue();
			Object val = fi.get(o);
			Element row = toElementX(fn, val, dups);
			e.addContent(row);
		}
		return e;
	}

	private Element arrayToElement(Element e, String nm, Object o,
			LinkedList dups) throws Exception {
		int l = Array.getLength(o);
		for (int i = 0; i < l; ++i) {
			Object oi = Array.get(o, i);
			Element ei = toElementX(arrayEltField, oi, dups);
			e.addContent(ei);
		}
		return e;
	}

	private Element collectionToElement(Element e, String nm, Collection o,
			LinkedList dups) throws Exception {
		if (!o.isEmpty()) {
			Iterator it = o.iterator();
			while (it.hasNext()) {
				Object oi = it.next();
				Element ei = toElementX(collectionEltField, oi, dups);
				e.addContent(ei);
			}
		}
		return e;
	}

	private Element mapToElement(Element e, String nm, Map o, LinkedList dups)
			throws Exception {
		if (!o.isEmpty()) {
			Iterator mit = o.entrySet().iterator();
			while (mit.hasNext()) {
				Map.Entry me = (Map.Entry) mit.next();
				Object key = me.getKey();
				Object val = me.getValue();
				Element mapent = new Element(mapEltField);
				mapent.addContent(toElementX(mapEltFieldKey, key, dups));
				mapent.addContent(toElementX(mapEltFieldVal, val, dups));
				e.addContent(mapent);
			}
		}
		return e;
	}

	private Element toElementX(String nm, Object o, LinkedList dups)
			throws Exception {
		if (o != null) {
			if (o instanceof Element) {
				return (Element) o;
			}
			// if we want self-handling objects they go here
		}
		Element e = new Element(nm);
		if (o == null) {
			return e;
		}
		Element cl = new Element(classNameField);
		cl.setText(o.getClass().getName());
		e.addContent(cl);
		if (passThrough(o)) {
			Element oe = new Element(payloadField);
			oe.setText(o.toString());
			e.addContent(oe);
			return e;
		}
		pushObject(o, dups); // pushes or throws-out
		Element rv = null;
		Exception ex = null;
		try {
			Class c = o.getClass();
			if (c.isArray()) {
				rv = arrayToElement(e, nm, o, dups);
			} else if (o instanceof Collection) {
				rv = collectionToElement(e, nm, (Collection) o, dups);
			} else if (o instanceof Map) {
				rv = mapToElement(e, nm, (Map) o, dups);
			} else {
				rv = objectToElement(e, nm, o, dups);
			}
		} catch (Exception ec) {
			ex = ec;
		}
		popObject(o, dups); // pushes or throws-out
		if (ex != null) {
			throw ex;
		}
		return rv;
	}

	private static Class cForName(String s) {
		try {
			return Class.forName(s);
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
		return null;
	}

	private static final Class stringClass = cForName("java.lang.String");

	private static final Class integerClass = cForName("java.lang.Integer");

	private static final Class longClass = cForName("java.lang.Long");

	private static final Class floatClass = cForName("java.lang.Float");

	private static final Class doubleClass = cForName("java.lang.Double");

	private static final Class byteClass = cForName("java.lang.Byte");

	private static final Class characterClass = cForName("java.lang.Character");

	private static final Class booleanClass = cForName("java.lang.Boolean");

	static boolean equivClass(Class a, Class b) {
		return a.isAssignableFrom(b) && b.isAssignableFrom(a);
	}

	static boolean equivString(Class a) {
		return equivClass(a, stringClass);
	}

	static Object promoteString(Class c, String s) throws Exception {
		if (equivClass(c, stringClass)) {
			return s;
		}
		if (equivClass(c, Integer.TYPE) || equivClass(c, integerClass)) {
			return new Integer(s);
		}
		if (equivClass(c, Long.TYPE) || equivClass(c, longClass)) {
			return new Long(s);
		}
		if (equivClass(c, Float.TYPE) || equivClass(c, floatClass)) {
			return new Float(s);
		}
		if (equivClass(c, Double.TYPE) || equivClass(c, doubleClass)) {
			return new Double(s);
		}
		if (equivClass(c, Boolean.TYPE) || equivClass(c, booleanClass)) {
			return new Boolean(s);
		}
		if (equivClass(c, Byte.TYPE) || equivClass(c, byteClass)) {
			return new Byte(s);
		}
		if (equivClass(c, Character.TYPE) || equivClass(c, characterClass)) {
			if (s.length() > 0) {
				return new Character(s.charAt(0));
			}
		}
		// should not be reached
		throw new Exception("promoteString ate shit");
	}

	private Object toArray(Class c, Element m) throws Exception {
		Class ci = c.getComponentType();
		List elts = m.getChildren(arrayEltField);
		int l = 0;
		if ((elts != null) && (!elts.isEmpty())) {
			l = elts.size();
		}
		Object r = Array.newInstance(ci, l);
		//System.out.println("r: " + r.getClass().getName() + " l: " + l);
		if (l > 0) {
			int i = 0;
			Iterator it = elts.iterator();
			while (it.hasNext()) {
				Element ei = (Element) it.next();
				Object oi = toObjectX(ei);
				//System.out.println("set(" + r + "," + i + "," + oi + ")");
				try {
					Array.set(r, i, oi);
				} catch (Exception e) {
					/*Object blow = */toObjectX(ei);
				}
				++i;
			}
		}
		return r;
	}

	private Collection toCollection(Collection o, Element m) throws Exception {
		List elts = m.getChildren(collectionEltField);
		if ((elts != null) && (!elts.isEmpty())) {
			Iterator it = elts.iterator();
			while (it.hasNext()) {
				Element ei = (Element) it.next();
				Object oi = toObjectX(ei);
				o.add(oi);
			}
		}
		return o;
	}

	private Map toMap(Map o, Element m) throws Exception {
		List elts = m.getChildren(mapEltField);
		if ((elts != null) && (!elts.isEmpty())) {
			Iterator it = elts.iterator();
			while (it.hasNext()) {
				Element ei = (Element) it.next();
				Element ki = ei.getChild(mapEltFieldKey);
				Element vi = ei.getChild(mapEltFieldVal);
				Object kio = toObjectX(ki);
				Object vio = toObjectX(vi);
				o.put(kio, vio);
			}
		}
		return o;
	}

	private Object toObject(Class c, Object o, Element m) throws Exception {
		SortedMap fieldList = getFieldMap(o.getClass());
		Iterator fit = fieldList.entrySet().iterator();
		while (fit.hasNext()) {
			Map.Entry me = (Map.Entry) fit.next();
			String fn = (String) me.getKey();
			Field fi = (Field) me.getValue();
			Element fv = m.getChild(fn);
			if (fv != null) {
				Object oi = toObjectX(fv);
				fi.set(o, oi);
			}
		}
		return o;
	}

	private Object toObjectX(Element m) throws Exception {
		Element cl = m.getChild(classNameField);
		if (cl == null) {
			return null;
		}
		String className = cl.getText().trim();
		Class recordClass = null;
		if (className.charAt(0) == '[') {
			// array, can avoid class control
			recordClass = Class.forName(className);
			if (recordClass == null) {
				throw new Exception("unexepcted class: " + className);
			}
			return toArray(recordClass, m);
		} else {
			if (control != null) {
				recordClass = control.getClass(className);
			} else {
				recordClass = Class.forName(className);
			}
		}
		if (recordClass == null) {
			throw new Exception("unexepcted class: " + className);
		}
		if (recordClass.isArray()) {
			return toArray(recordClass, m);
		}
		Element oe = m.getChild(payloadField);
		if (oe != null) {
			List children = oe.getChildren();
			if ((children == null) || (children.isEmpty())) {
				String txt = oe.getText().trim();
				return promoteString(recordClass, txt);
			}
		}
		Object o = recordClass.newInstance();
		if (o instanceof Collection) {
			return toCollection((Collection) o, m);
		}
		if (o instanceof Map) {
			return toMap((Map) o, m);
		}
		return toObject(recordClass, o, m);
	}
}

