package mzlabs.gart.util;

/**
 * Object loader
 *   Copyright (C) 2002-2003  John Mount, Nina Zumel (j@mzlabs.com)
 */

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * class to hold what classes system is allowed to load
 */
public final class ClassControl {
	private ClassLoader loader;

	private Map<String,Class<?>> allowed;

	/**
	 * @param ldr
	 *            optional class loader
	 */
	public ClassControl(ClassLoader ldr) {
		allowed = new TreeMap<String, Class<?>>();
		if (ldr != null) {
			loader = ldr;
		} else {
			loader = this.getClass().getClassLoader();
		}
		addCommonClasses();
	}

	/**
	 * add class to allowed map (shallow)
	 * 
	 * @param c
	 *            class
	 */
	public void addClass(Class<?> c) {
		if (c != null) {
			String nm = c.getName();
			if (!allowed.containsKey(nm)) {
				allowed.put(c.getName(), c);
			}
		}
	}

	/**
	 * add class to allowed map (shallow)
	 * 
	 * @param s
	 *            classname
	 */
	public void addClass(String s) {
		try {
			Class<?> c = Class.forName(s, true, loader);
			addClass(c);
		} catch (Exception e) {
			System.out.println("caught: " + e);
		}
	}

	private static final String[] commonClasses = { "java.lang.String",
			"java.lang.Integer", "java.lang.Long", "java.lang.Float",
			"java.lang.Double", "java.lang.Byte", "java.lang.Character",
			"java.lang.Boolean", "java.util.Set", "java.util.SortedSet",
			"java.util.TreeSet", "java.util.HashSet", "java.util.Map",
			"java.util.SortedMap", "java.util.TreeMap", "java.util.HashMap", };

	private void addCommonClasses() {
		for (int i = 0; i < commonClasses.length; ++i) {
			addClass(commonClasses[i]);
		}
	}

	/**
	 * @param s
	 *            classname
	 * @return class if in allowed set
	 */
	public Class<?> getClass(String s) {
		return allowed.get(s);
	}

	/**
	 * add class c and any fields recursively
	 * 
	 * @param c
	 *            class to add
	 */
	public void addClassDeep(Class<?> c) {
		if (c == null) {
			return;
		}
		String nm = c.getName();
		if (allowed.containsKey(nm)) {
			return;
		}
		addClass(c);
		if (c.isArray()) {
			addClassDeep(c.getComponentType());
			return;
		}
		SortedMap<String, Field> m = XMLSerializer.getFieldMap(c);
		if ((m == null) || (m.isEmpty())) {
			return;
		}
		Iterator<Entry<String, Field>> it = m.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Field> me = it.next();
			Field fi = me.getValue();
			Class<?> t = fi.getType();
			addClassDeep(t);
		}
	}

	/**
	 * @param c
	 *            class to start at
	 * @return basic list of class names (plus c if not null)
	 */
	public static ClassControl buildClassControl(Class<?> c) {
		ClassControl control = null;
		if (c != null) {
			control = new ClassControl(c.getClassLoader());
			control.addClassDeep(c);
		} else {
			control = new ClassControl(null);
		}
		return control;
	}
}