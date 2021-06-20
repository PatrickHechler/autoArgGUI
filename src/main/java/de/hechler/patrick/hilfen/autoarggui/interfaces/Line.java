package de.hechler.patrick.hilfen.autoarggui.interfaces;

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
		case fileChoose:
		case showBelowOptionsButton:
		case ownWindow:
		case comboBoxFalseTrue:
		case comboBoxTrueFalse:
			throw new IllegalStateException("this is no normal element!");
		}
	}
	
	default String twoValuesFirstText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no tow values element!");
		case fileChoose:
		case showBelowOptionsButton:
			return compatibleText(index)[0];
		}
	}
	
	default String twoValuesSecondText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no tow values element!");
		case fileChoose:
		case showBelowOptionsButton:
			return compatibleText(index)[1];
		}
	}
	
	default String threeValuesFirstText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no tow values element!");
		case ownWindow:
			return compatibleText(index)[0];
		}
	}
	
	default String threeValuesSecondText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no tow values element!");
		case ownWindow:
			return compatibleText(index)[1];
		}
	}
	
	default String threeValuesThirdText(int index) {
		switch (art(index)) {
		default:
			throw new IllegalStateException("this is no tow values element!");
		case ownWindow:
			return compatibleText(index)[2];
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
	
	int size();
	
	Object getValue(int index);
	
	Class <?> getType(int index);
	
	void setValue(int index, Object val);
	
	default void deleteValue(int index) {
		setValue(index, null);
	}
	
	Line[] subLines(int index);
	
	void addLine(int index);
	
	void removeAllLines(int index);
	
	void removeThisLine();
	
	default void initSubLines() {}
	
	String[] toArgs();
	
}
