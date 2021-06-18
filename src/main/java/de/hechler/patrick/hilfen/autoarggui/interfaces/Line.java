package de.hechler.patrick.hilfen.autoarggui.interfaces;

import java.lang.reflect.Array;

import de.hechler.patrick.hilfen.autoarggui.enums.GUIArt;

public interface Line {
	
	default GUIArt art(int index) {
		return arten()[index];
	}
	
	GUIArt[] arten();
	
	default String normalText(int index) {
		switch (art(index)) {
		default:
			return compatibleText(index)[0];
		case showBelowOptionsButton:
		case comboBoxFalseTrue:
		case comboBoxTrueFalse:
			throw new IllegalStateException("this is no normal element!");
		}
	}
	
	default String showOptsFirstText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no normal element!");
		case showBelowOptionsButton:
			return compatibleText(index)[0];
		}
	}
	
	default String showOptsSecondText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no normal element!");
		case showBelowOptionsButton:
			return compatibleText(index)[1];
		}
	}
	
	default String[] comboBoxText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no combo box element!");
		case comboBoxFalseTrue:
		case comboBoxTrueFalse:
			return compatibleText(index);
		}
	}
	
	String[] compatibleText(int index);
	
	int length();
	
	Object getValue(int index);
	
	Class <?> getType(int index);
	
	default Class <?> getSubType(int index) {
		return getType(index).getComponentType();
	}
	
	default Object getSubValue(int index, int subIndex, Object val) {
		Object arr = getValue(subIndex);
		return Array.get(arr, subIndex);
	}
	
	void setValue(int index, Object val);
	
	default void setSubValue(int index, int subIndex, Object val) {
		Object arr = getValue(subIndex);
		Array.set(arr, subIndex, val);
	}
	
}
