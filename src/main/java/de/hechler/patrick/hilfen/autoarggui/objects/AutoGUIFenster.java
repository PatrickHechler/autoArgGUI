package de.hechler.patrick.hilfen.autoarggui.objects;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import de.hechler.patrick.hilfen.autoarggui.enums.GUIArt;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Arguments;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Line;
import de.hechler.patrick.hilfen.autoarggui.interfaces.MainMethod;
import de.hechler.patrick.serial.Deserializer;
import de.hechler.patrick.serial.Serializer;

public class AutoGUIFenster {
	
	private static final Serializer   SERIALIZER;
	private static final Deserializer DESERIALIZER;
	
	static {
		SERIALIZER = new Serializer(false, true, true);
		DESERIALIZER = new Deserializer(Collections.emptyMap());
	}
	
	private final AutoGUIFenster                         superWindow;
	private final int                                    high;
	private final int                                    whidh;
	private final int                                    empty;
	private MainMethod                                   main;
	private Arguments                                    args;
	private Line[]                                       lines;
	private Line                                         parentLine;
	private int                                          parentLineIndex;
	private final JFrame                                 frame;
	private final JButton                                finish;
	private final JButton                                save;                           /* if subWindow: addNew */
	private final JButton                                load;                           /* if subWindow: delAll */
	private final JFileChooser                           normalFC;
	private final JFileChooser                           argsFC;
	private int                                          shownOptions = 0;
	private Map <Integer, Map <Integer, AutoGUIFenster>> subWindows   = new HashMap <>();
	
	public AutoGUIFenster(String title, MainMethod main, Arguments args) {
		this(title, main, args, 20, 500, 10);
	}
	
	public AutoGUIFenster(String title, MainMethod main, Arguments args, int high, int whidh, int empty) {
		this.superWindow = null;
		this.high = high;
		this.whidh = whidh;
		this.empty = empty;
		this.main = main;
		this.args = args;
		this.frame = new JFrame(title);
		this.save = new JButton("save");
		this.load = new JButton("load");
		this.finish = new JButton("finish");
		this.normalFC = new JFileChooser();
		this.argsFC = new JFileChooser();
	}
	
	private AutoGUIFenster(AutoGUIFenster sw, String title, String addNewText, String delAllText, int high, int whidh, int empty) {
		this.superWindow = sw;
		this.high = high;
		this.whidh = whidh;
		this.empty = empty;
		this.lines = null;
		this.frame = new JFrame(title);
		this.save = new JButton(addNewText);
		this.load = new JButton(delAllText);
		this.finish = null;
		this.normalFC = new JFileChooser();
		this.argsFC = null;
	}
	
	private AutoGUIFenster subWindowLoad() {
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setLayout(null);
		save.addActionListener(ae -> {
			parentLine.addLine(parentLineIndex);
			superWindow.rebuild(false);
		});
		frame.add(save);
		load.addActionListener(ae -> {
			parentLine.removeAllLines(parentLineIndex);
			superWindow.rebuild(false);
		});
		frame.add(load);
		return this;
	}
	
	public void load() {
		load(null, null);
	}
	
	public void load(String finishMessage, String msgTitle) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		FileFilter ff = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "*.args";
			}
			
			@Override
			public boolean accept(File f) {
				if (f.isHidden()) {
					return false;
				}
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(".args")) {
					return true;
				}
				return false;
			}
			
		};
		argsFC.addChoosableFileFilter(ff);
		argsFC.setFileFilter(ff);
		ff = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "*.args [ignore hidden flag]";
			}
			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(".args")) {
					return true;
				}
				return false;
			}
			
		};
		argsFC.addChoosableFileFilter(ff);
		argsFC.setAcceptAllFileFilterUsed(false);
		finish.addActionListener(ae -> {
			main.main(args.toArgs());
			if (finishMessage != null || msgTitle != null) {
				JOptionPane.showMessageDialog(frame, finishMessage, msgTitle, JOptionPane.INFORMATION_MESSAGE);
			}
			frame.setVisible(false);
		});
		save.addActionListener(ae -> {
			int ret = argsFC.showSaveDialog(frame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					File sf = argsFC.getSelectedFile();
					if ( !sf.getName().endsWith(".args")) {
						sf = new File(sf.getParentFile(), sf.getName() + ".args");
					}
					if (Files.exists(sf.toPath())) {
						ret = JOptionPane.showConfirmDialog(frame, "the file (" + sf.getPath() + ") exist already\nshould I overwrite it?", "existing target", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
						if (ret == JOptionPane.OK_OPTION) {
							System.out.println("overwirte save file");
							SERIALIZER.writeObject(new FileOutputStream(sf), args);
						} else {
							System.out.println("do'nt overwrite save file ret=" + ret);
						}
					} else {
						SERIALIZER.writeObject(new FileOutputStream(sf), args);
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		load.addActionListener(ae -> {
			int ret = argsFC.showOpenDialog(frame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					File sf = argsFC.getSelectedFile();
					if ( !sf.getName().endsWith(".args")) {
						sf = new File(sf.getParentFile(), sf.getName() + ".args");
					}
					args = (Arguments) DESERIALIZER.readObject(new FileInputStream(sf));
					rebuild(false);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		int xPos = empty, yPos = empty, singleWhidh = (whidh - (empty * 5)) / 3;
		finish.setBounds(xPos, yPos, singleWhidh, high);
		xPos += singleWhidh + empty;
		save.setBounds(xPos, yPos, singleWhidh, high);
		xPos += singleWhidh + empty;
		load.setBounds(xPos, yPos, singleWhidh, high);
		frame.add(finish);
		frame.add(save);
		frame.add(load);
		rebuild(true);
	}
	
	private final List <Component> removableComps = new ArrayList <>();
	
	private void rebuild(boolean setVisible) throws AssertionError {
		for (Component rem : removableComps) {
			frame.remove(rem);
		}
		removableComps.clear();
		int xPos, yPos;
		if (superWindow != null) {
			yPos = empty;
		} else {
			yPos = empty + high + empty;
		}
		Line[] all;
		if (superWindow != null) {
			all = lines;
		} else {
			all = args.getAllLines();
		}
		int neededShownOptions = 0;
		for (int _i = 0; _i < all.length && neededShownOptions >= shownOptions; _i ++ ) {
			final int i = _i;
			xPos = empty;
			GUIArt[] arten = all[i].arten();
			final int singleWhidh = (whidh - (empty * (2 + arten.length))) / arten.length;
			final int needShownOptionsConst = neededShownOptions;
			for (int _li = 0; _li < arten.length; _li ++ ) {
				final int li = _li;
				final Component comp;
				switch (arten[li]) {
				case comboBoxFalseTrue:
				case comboBoxTrueFalse:
					comp = new JComboBox <>(all[i].comboBoxText(li));
					if (all[i].hasValue(li)) {
						Class <?> zwType = all[i].getType(li);
						if (zwType == Integer.TYPE || zwType == Integer.class) {
							int index = (int) (Integer) all[i].getValue(li);
							((JComboBox <?>) comp).setSelectedIndex(index);
						} else if (zwType == Boolean.TYPE) {
							boolean reverse = arten[li] == GUIArt.comboBoxTrueFalse;
							boolean index = (boolean) (Boolean) all[i].getValue(li);
							index ^= reverse;
							((JComboBox <?>) comp).setSelectedIndex(index ? 1 : 0);
						} else if (zwType == Boolean.class) {
							boolean reverse = arten[li] == GUIArt.comboBoxTrueFalse;
							Boolean index = (Boolean) all[i].getValue(li);
							if (index == null) {
								((JComboBox <?>) comp).setSelectedIndex(2);
							} else {
								boolean _index = index.booleanValue() ^ reverse;
								((JComboBox <?>) comp).setSelectedIndex(_index ? 1 : 0);
							}
						} else {
							Object item = all[i].getValue(li);
							((JComboBox <?>) comp).setSelectedItem(item);
						}
					}
					comp.addFocusListener(new FocusAdapter() {
						
						@Override
						public void focusLost(FocusEvent e) {
							Class <?> type = all[i].getType(li);
							if (type == Boolean.TYPE || type == Boolean.class) {
								boolean reverse = arten[li] == GUIArt.comboBoxTrueFalse;
								boolean set = ((JComboBox <?>) comp).getSelectedIndex() == 0;
								set = set ^ reverse;
								all[i].setValue(li, set);
							} else if (type == Integer.TYPE || type == Integer.class) {
								all[i].setValue(li, ((JComboBox <?>) comp).getSelectedIndex());
							} else if (type == String.class) {
								all[i].setValue(li, ((JComboBox <?>) comp).getSelectedItem());
							} else {
								throw new ClassCastException("unknown type for a combo box: " + type.getName());
							}
						}
						
					});
					break;
				case showBelowOptionsButton:
					comp = new JButton(all[i].twoValuesFirstText(li));
					((JButton) comp).addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (shownOptions > needShownOptionsConst) {
								((JButton) comp).setText(all[i].twoValuesFirstText(li));
								shownOptions = needShownOptionsConst;
							} else {
								((JButton) comp).setText(all[i].twoValuesSecondText(li));
								shownOptions = needShownOptionsConst + 1;
							}
							rebuild(false);
						}
						
					});
					neededShownOptions ++ ;
					break;
				case deleteButton:
					comp = new JButton(all[i].normalText(li));
					((JButton) comp).addActionListener(ae -> {
						all[i].deleteValue(li);
						rebuild(false);
					});
					break;
				case fileChoose:
					comp = new JButton(all[i].twoValuesFirstText(li));
					((JButton) comp).addActionListener(ae -> {
						int ret = normalFC.showDialog(comp, all[i].twoValuesSecondText(li));
						if (ret == JFileChooser.APPROVE_OPTION) {
							Class <?> cls = all[i].getType(li);
							if (cls == String.class) {
								all[i].setValue(li, normalFC.getSelectedFile().getPath());
							} else {
								all[i].setValue(li, normalFC.getSelectedFile());
							}
						}
						rebuild(false);
					});
					break;
				case number: {
					comp = new JTextField();
					final String text;
					if (all[i].hasValue(li)) {
						text = all[i].getValue(li).toString();
					} else {
						text = all[i].normalText(li);
					}
					((JTextField) comp).setText(text);
					((JTextField) comp).addFocusListener(new FocusAdapter() {
						
						String oldText = text;
						
						@Override
						public void focusLost(FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							if (oldText.equals(newText)) {
								return;
							}
							Class <?> type = all[i].getType(li);
							try {
								if (type == Long.TYPE || type == Long.class) {
									all[i].setValue(li, Long.parseLong(newText));
								} else if (type == Integer.TYPE || type == Integer.class) {
									all[i].setValue(li, Integer.parseInt(newText));
								} else if (type == Short.TYPE || type == Short.class) {
									all[i].setValue(li, Short.parseShort(newText));
								} else if (type == Byte.TYPE || type == Byte.class) {
									all[i].setValue(li, Byte.parseByte(newText));
								} else if (type == Double.TYPE || type == Double.class) {
									all[i].setValue(li, Double.parseDouble(newText));
								} else if (type == Float.TYPE || type == Float.class) {
									all[i].setValue(li, Float.parseFloat(newText));
								} else if (type == BigInteger.class) {
									all[i].setValue(li, new BigInteger(newText));
								} else if (type == BigDecimal.class) {
									all[i].setValue(li, new BigDecimal(newText));
								} else {
									all[i].setValue(li, newText);
								}
								oldText = newText;
							} catch (Exception err) {
								JOptionPane.showMessageDialog(frame, err.getMessage(), err.getClass().getName(), JOptionPane.ERROR_MESSAGE);
								((JTextField) comp).setText(oldText);
							}
						}
						
					});
					break;
				}
				case choosenFileModifiable:
				case modifiableText: {
					comp = new JTextField();
					final String text;
					if (all[i].hasValue(li)) {
						text = all[i].getValue(li).toString();
					} else {
						text = all[i].normalText(li);
					}
					((JTextField) comp).setText(text);
					((JTextField) comp).addFocusListener(new FocusAdapter() {
						
						String oldText = text;
						
						@Override
						public void focusLost(FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							if (oldText.equals(newText)) {
								return;
							}
							all[i].setValue(li, newText);
							oldText = newText;
						}
						
					});
					break;
				}
				case choosenFileunmodifiable:
				case unmodifiableText: {
					comp = new JTextPane();
					final String text = all[i].normalText(li);
					((JTextPane) comp).setEditable(false);
					((JTextPane) comp).setText(text);
					// ((JTextPane) comp).addFocusListener(new FocusAdapter() {
					//
					// @Override
					// public void focusLost(FocusEvent e) {
					// String newText = ((JTextPane) comp).getText();
					// all[i].setValue(li, newText);
					// }
					//
					// });
					break;
				}
				case ownWindow: {
					comp = new JButton(all[i].fourValuesFirstText(li));
					final AutoGUIFenster sw;
					Map <Integer, AutoGUIFenster> zw = subWindows.get(i);
					all[i].initSubLines();
					if (zw == null) {
						zw = new HashMap <>();
						sw = new AutoGUIFenster(superWindow != null ? superWindow : this, all[i].fourValuesSecondText(li), all[i].fourValuesThirdText(li), all[i].fourValuesFourthText(li), high, whidh,
							empty);
						zw.put(li, sw);
						subWindows.put(i, zw);
					} else {
						AutoGUIFenster zw0 = zw.get(li);
						if (zw0 == null) {
							sw = new AutoGUIFenster(superWindow != null ? superWindow : this, all[i].fourValuesFirstText(li), all[i].fourValuesThirdText(li), all[i].fourValuesFourthText(li), high,
								whidh, empty);
						} else {
							sw = zw0;
						}
					}
					((JButton) comp).addActionListener(ae -> sw.rebuild(true));
					sw.lines = all[i].subLines(li);
					if (sw.parentLine == null) {
						sw.parentLine = all[i];
						sw.parentLineIndex = li;
						sw.subWindowLoad();
					} else if (sw.parentLine != all[i]) {
						sw.parentLine = all[i];
						sw.parentLineIndex = li;
						for (ActionListener al : sw.save.getActionListeners()) {
							sw.save.removeActionListener(al);
						}
						for (ActionListener al : sw.load.getActionListeners()) {
							sw.save.removeActionListener(al);
						}
						sw.subWindowLoad();
					}
					sw.rebuild(false);
					break;
				}
				default:
					throw new AssertionError("unknown GUIArt: " + arten[li].name());
				}
				comp.setBounds(xPos, yPos, singleWhidh, high);
				frame.add(comp);
				removableComps.add(comp);
				xPos += singleWhidh + empty;
			}
			yPos += high + empty;
		}
		if (superWindow != null) {
			xPos = empty;
			int singleWhidh = (whidh - (empty * 4)) / 2;
			save.setBounds(xPos, yPos, singleWhidh, high);
			xPos += singleWhidh + empty;
			load.setBounds(xPos, yPos, singleWhidh, high);
			yPos += high + empty;
		}
		frame.setBounds(0, 0, whidh, yPos + empty + 30);
		frame.setLocationRelativeTo(null);
		if (setVisible) {
			frame.setVisible(true);
		} else {
			frame.repaint();
		}
	}
	
}
