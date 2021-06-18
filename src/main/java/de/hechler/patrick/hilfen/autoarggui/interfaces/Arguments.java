package de.hechler.patrick.hilfen.autoarggui.interfaces;


public interface Arguments {
	
	Line[] getAll();
	
	Line get(int indx);
	
	int length();
	
	String[] toArgs();
	
}
