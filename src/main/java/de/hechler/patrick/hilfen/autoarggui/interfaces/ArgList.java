package de.hechler.patrick.hilfen.autoarggui.interfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

@SuppressWarnings("javadoc")
public class ArgList {
	
	private final List<Object> list;
	private int                cnt;
	
	public ArgList() {
		this.list = new ArrayList<>();
		this.cnt  = 0;
	}
	
	public void add(String arg) {
		this.list.add(arg);
		this.cnt++;
	}
	
	public void add(String arg0, String arg1) {
		this.list.add(arg0);
		this.list.add(arg1);
		this.cnt += 2;
	}
	
	public void add(String arg0, String arg1, String arg2) {
		this.list.add(arg0);
		this.list.add(arg1);
		this.list.add(arg2);
		this.cnt += 3;
	}
	
	public void add(String... args) {
		this.list.add(args);
		this.cnt += args.length;
	}
	
	public void add(ArgList args) {
		this.list.add(args);
		this.cnt += args.cnt;
	}
	
	public void add(List<String> args) {
		this.list.add(args);
		this.cnt += args.size();
	}
	
	public String[] toArgs() {
		String[] a  = new String[this.cnt];
		int      ai = 0;
		toArgsImpl(a, ai);
		return a;
	}
	
	private int toArgsImpl(String[] a, int ai) {
		for (int i = 0, s = this.list.size(); i < s; i++) {
			Object obj = this.list.get(i);
			if (obj instanceof String[]) {
				String[]  sa  = (String[]) obj;
				final int len = sa.length;
				for (int ii = 0; ii < len; ii++) {
					if (sa[ii] != null) {
						a[ai++] = sa[ii];
					} else {
						a[ai++] = nullReplace(i, ai, obj, ii);
					}
				}
			} else if (obj instanceof ArgList) {
				ArgList al = (ArgList) obj;
				if (al.cnt + ai > this.cnt) {
					throw new IllegalStateException("to calculate my arguments I have to first calculate my arguments!");
				}
				ai = al.toArgsImpl(a, ai);
			} else if (obj instanceof List) {
				List<?> l = (List<?>) obj;
				if (l instanceof RandomAccess) {
					for (int ii = 0, len = l.size(); ii < len; ii++) {
						String val = (String) l.get(ii);
						if (val != null) {
							a[ai++] = val;
						} else {
							a[ai++] = nullReplace(i, ai, obj, ii);
						}
					}
				} else {
					for (Object o : l) {
						if (o != null) {
							a[ai++] = (String) o;
						} else {
							a[ai++] = nullReplace(i, ai, obj, -1);
						}
					}
				}
			} else if (obj instanceof String) {
				a[ai++] = (String) obj;
			} else if (obj == null) {
				a[ai++] = nullReplace(i, ai, null, 0);
			} else {
				throw new AssertionError("illegal element type in my list: " + obj.getClass().getName());
			}
		}
		return ai;
	}
	
	protected String nullReplace(int container, @SuppressWarnings("unused") int totalIndex, Object containerObj, int containerIndex) {
		if (containerObj instanceof String[]) {
			containerObj = Arrays.toString((String[]) containerObj);
		}
		System.err.println(
				"[ArgList]: null detected, replace with \"null\" (in the " + container + "th container: " + containerObj + " at index " + containerIndex + ")");
		return "null";
	}
	
}
