package de.hechler.patrick.hilfen.autoarggui.objects;

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
		throw new UnsupportedOperationException("subLines(int)");
	}
	
	@Override
	public void addLine(int index) {
		throw new UnsupportedOperationException("addLine(int)");
	}
	
	@Override
	public void removeAllLines(int index) {
		throw new UnsupportedOperationException("removeAllLines(int)");
	}
	
	@Override
	public void removeThisLine() {
		throw new UnsupportedOperationException("removeThisLine()");
	}
	
	@Override
	public String[] toArgs() {
		throw new UnsupportedOperationException("toArgs()");
	}
	
}
