package de.hechler.patrick.hilfen.autoarggui.interfaces;

import de.hechler.patrick.hilfen.autoarggui.enums.GUIArt;

@SuppressWarnings("javadoc")
public interface Line {
	
	default GUIArt art(int index) {
		return arten()[index];
	}
	
	GUIArt[] arten();
	
	default String normalText(int index) {
		switch (art(index)) {
		case fileChoose:
		case showBelowOptionsButton:
		case ownWindow:
		case comboBox:
			throw new IllegalStateException("this is no normal element!");
		// $CASES-OMITTED$
		default:
			return compatibleText(index)[0];
		}
	}
	
	default String twoValuesFirstText(int index) {
		switch (art(index)) {
		case fileChoose:
		case showBelowOptionsButton:
			return compatibleText(index)[0];
		// $CASES-OMITTED$
		default:
			throw new IllegalStateException("this is no tow values element!");
		}
	}
	
	default String twoValuesSecondText(int index) {
		switch (art(index)) {
		case fileChoose:
		case showBelowOptionsButton:
			return compatibleText(index)[1];
		// $CASES-OMITTED$
		default:
			throw new IllegalStateException("this is no tow values element!");
		}
	}
	
	default String fourValuesFirstText(int index) {
		if (art(index) != GUIArt.ownWindow) {
			throw new IllegalStateException("this is no four values element!");
		}
		return compatibleText(index)[0];
	}
	
	default String fourValuesSecondText(int index) {
		if (art(index) != GUIArt.ownWindow) {
			throw new IllegalStateException("this is no four values element!");
		}
		return compatibleText(index)[1];
	}
	
	default String fourValuesThirdText(int index) {
		if (art(index) != GUIArt.ownWindow) {
			throw new IllegalStateException("this is no four values element!");
		}
		return compatibleText(index)[2];
	}
	
	default String fourValuesFourthText(int index) {
		if (art(index) != GUIArt.ownWindow) {
			throw new IllegalStateException("this is no four values element!");
		}
		return compatibleText(index)[3];
	}
	
	default String[] comboBoxText(int index) {
		if (art(index) != GUIArt.comboBox) {
			throw new IllegalStateException("this is no combo box element!");
		}
		return compatibleText(index);
	}
	
	String[] compatibleText(int index);
	
	int size();
	
	default boolean hasValue(int index) {
		return getValue(index) != null;
	}
	
	Object getValue(int index);
	
	Class<?> getType(int index);
	
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
