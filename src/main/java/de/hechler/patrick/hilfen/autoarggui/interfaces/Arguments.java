package de.hechler.patrick.hilfen.autoarggui.interfaces;


public interface Arguments {
	
	Line[] getAllLines();
	
	Line getLine(int indx);
	
	int size();

	default String[] toArgs() {
		return toArgList().toArgs();
	}

	ArgList toArgList();
	
}
