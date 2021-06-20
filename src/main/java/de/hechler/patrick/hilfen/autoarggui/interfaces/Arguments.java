package de.hechler.patrick.hilfen.autoarggui.interfaces;


public interface Arguments {
	
	Line[] getAllLines();
	
	Line getLine(int indx);
	
	int size();
	
	String[] toArgs();
	
}
