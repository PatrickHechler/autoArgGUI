package de.hechler.patrick.hilfen.autoarggui.objects;

import de.hechler.patrick.hilfen.autoarggui.interfaces.ArgList;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Line;

public abstract class AbstractLine implements Line {
	
	protected String[][] texts;
	
	public AbstractLine(String[][] texts) {
		this.texts = texts;
	}
	
	@Override
	public int size() {
		return arten().length;
	}
	
	@Override
	public String[] compatibleText(int index) {
		return texts[index];
	}
	
	@Override
	public Line[] subLines(int index) {
		throw new UnsupportedOperationException("subLines(int) " + getClass().getName());
	}
	
	@Override
	public void addLine(int index) {
		throw new UnsupportedOperationException("addLine(int) " + getClass().getName());
	}
	
	@Override
	public void removeAllLines(int index) {
		throw new UnsupportedOperationException("removeAllLines(int) " + getClass().getName());
	}
	
	@Override
	public void removeThisLine() {
		throw new UnsupportedOperationException("removeThisLine() " + getClass().getName());
	}
	
	@Override
	public void addArgs(ArgList list) { 
		throw new UnsupportedOperationException("addArgs() " + getClass().getName());
	}
	
}
