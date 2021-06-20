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

import de.hechler.patrick.fileparser.serial.Deserializer;
import de.hechler.patrick.fileparser.serial.Serializer;
import de.hechler.patrick.hilfen.autoarggui.enums.GUIArt;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Arguments;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Line;
import de.hechler.patrick.hilfen.autoarggui.interfaces.MainMethod;

public class AutoGUIFenster {
	
	private static final Serializer   SERIALIZER   = new Serializer(false, true, false, true, true);
	private static final Deserializer DESERIALIZER = new Deserializer(Collections.emptyMap());
	
	private final boolean                                isSubWindow;
	private final int                                    high;
	private final int                                    whidh;
	private final int                                    empty;
	private MainMethod                                   main;
	private Arguments                                    args;
	private Line[]                                       lines;
	private JFrame                                       frame;
	private JButton                                      finish;
	private JButton                                      save;                           /* if subWindow: addNew */
	private JButton                                      load;                           /* if subWindow: delAll */
	private JFileChooser                                 normalFC;
	private JFileChooser                                 argsFC;
	private int                                          shownOptions = 0;
	private Map <Integer, Map <Integer, AutoGUIFenster>> subWindows   = new HashMap <>();
	
	public AutoGUIFenster(MainMethod main, Arguments args) {
		this(main, args, 20, 250, 10);
	}
	
	public AutoGUIFenster(MainMethod main, Arguments args, int high, int whidh, int empty) {
		this.isSubWindow = false;
		this.high = high;
		this.whidh = whidh;
		this.empty = empty;
		this.main = main;
		this.args = args;
	}
	
	private AutoGUIFenster(String title, String addNewText, String delAllText, Line[] lines, int high, int whidh, int empty) {
		this.isSubWindow = true;
		this.high = high;
		this.whidh = whidh;
		this.empty = empty;
		this.lines = lines;
		this.frame = new JFrame(title);
		this.save = new JButton(addNewText);
		this.load = new JButton(delAllText);
		this.finish = null;
		this.normalFC = new JFileChooser();
		this.argsFC = null;
	}
	
	private AutoGUIFenster subWindowLoad(Line parent, int index) {
		save.addActionListener(ae -> {
			
		});
		frame.add(save);
		frame.add(load);
		return this;
	}
	
	public void load(String title) {
		load(title, null, null);
	}
	
	public void load(String title, String finishMessage, String msgTitle) {
		frame = new JFrame(title);
		finish = new JButton();
		save = new JButton();
		load = new JButton();
		normalFC = new JFileChooser();
		argsFC = new JFileChooser();
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
					SERIALIZER.writeObject(new FileOutputStream(argsFC.getSelectedFile()), args);
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
					args = (Arguments) DESERIALIZER.readObject(new FileInputStream(argsFC.getSelectedFile()));
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		int xPos = empty, yPos = empty, singleWhidh = whidh - (empty * 3);
		finish.setBounds(xPos, yPos, singleWhidh, high);
		xPos += singleWhidh + empty;
		save.setBounds(xPos, yPos, singleWhidh, high);
		xPos += singleWhidh + empty;
		load.setBounds(xPos, yPos, singleWhidh, high);
		rebuild(true);
	}
	
	private final List <Component> removableComps = new ArrayList <>();
	
	private void rebuild(boolean setVisible) throws AssertionError {
		for (Component rem : removableComps) {
			frame.remove(rem);
		}
		removableComps.clear();
		int xPos, yPos;
		if (isSubWindow) {
			yPos = empty;
		} else {
			yPos = empty + high + empty;
		}
		Line[] all;
		if (isSubWindow) {
			all = lines;
		} else {
			all = args.getAllLines();
		}
		for (int _i = 0; _i < all.length; _i ++ ) {
			final int i = _i;
			xPos = empty;
			GUIArt[] arten = all[i].arten();
			final int singleWhidh = whidh - (empty * (1 + arten.length));
			int neededShownOptions = 0;
			for (int _li = 0; _li < arten.length && neededShownOptions <= shownOptions; _li ++ ) {
				final int needShownOptionsConst = neededShownOptions;
				final int li = _li;
				final Component comp;
				switch (arten[li]) {
				case comboBoxFalseTrue:
				case comboBoxTrueFalse:
					comp = new JComboBox <>(all[i].comboBoxText(li));
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
						
						boolean hide = false;
						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (hide) {
								((JButton) comp).setText(all[i].twoValuesFirstText(li));
								shownOptions = needShownOptionsConst;
							} else {
								((JButton) comp).setText(all[i].twoValuesSecondText(li));
								shownOptions = needShownOptionsConst + 1;
							}
							hide = !hide;
							rebuild(false);
						}
						
					});
					neededShownOptions ++ ;
					break;
				case deleteButton:
					comp = new JButton(all[i].twoValuesFirstText(li));
					((JButton) comp).addActionListener(ae -> {
						all[i].deleteValue(li);
						rebuild(false);
					});
					neededShownOptions ++ ;
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
					neededShownOptions ++ ;
					break;
				case number: {
					comp = new JTextField();
					final String defaultText = all[i].normalText(li);
					((JTextField) comp).setText(defaultText);
					((JTextField) comp).addFocusListener(new FocusAdapter() {
						
						String text = defaultText;
						
						@Override
						public void focusLost(FocusEvent e) {
							String newText = ((JTextField) comp).getText();
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
								text = newText;
							} catch (Exception err) {
								JOptionPane.showMessageDialog(frame, err.getMessage(), err.getClass().getName(), JOptionPane.ERROR_MESSAGE);
								((JTextField) comp).setText(text);
							}
						}
						
					});
					break;
				}
				case choosenFileModifiable:
				case modifiableText: {
					comp = new JTextField();
					final String defaultText = all[i].normalText(li);
					((JTextField) comp).setText(defaultText);
					((JTextField) comp).addFocusListener(new FocusAdapter() {
						
						@Override
						public void focusLost(FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							all[i].setValue(li, newText);
						}
						
					});
					break;
				}
				case choosenFileunmodifiable:
				case unmodifiableText: {
					comp = new JTextPane();
					final String defaultText = all[i].normalText(li);
					((JTextField) comp).setEditable(false);
					((JTextField) comp).setText(defaultText);
					((JTextField) comp).addFocusListener(new FocusAdapter() {
						
						@Override
						public void focusLost(FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							all[i].setValue(li, newText);
						}
						
					});
					break;
				}
				case ownWindow: {
					comp = new JButton(all[i].normalText(li));
					final AutoGUIFenster sw;
					Map <Integer, AutoGUIFenster> zw = subWindows.get(i);
					if (zw == null) {
						zw = new HashMap <>();
						sw = new AutoGUIFenster(all[i].threeValuesFirstText(li), all[i].threeValuesSecondText(li), all[i].threeValuesThirdText(li), all[i].subLines(li), high, whidh, empty)
							.subWindowLoad(all[i], li);
						zw.put(li, sw);
						subWindows.put(i, zw);
					} else {
						AutoGUIFenster zw0 = zw.get(li);
						if (zw0 == null) {
							sw = new AutoGUIFenster(all[i].threeValuesFirstText(li), all[i].threeValuesSecondText(li), all[i].threeValuesThirdText(li), all[i].subLines(li), high, whidh, empty)
								.subWindowLoad(all[i], li);
						} else {
							sw = zw0;
						}
					}
					((JButton) comp).addActionListener(ae -> sw.rebuild(true));
					break;
				}
				default:
					throw new AssertionError("unknown GUIArt: " + arten[li].name());
				}
				comp.setBounds(xPos, yPos, singleWhidh, high);
				frame.add(comp);
				removableComps.add(comp);
			}
			yPos += high + empty;
		}
		if (isSubWindow) {
			xPos = empty;
			int singleWhidh = whidh - (empty * 3);
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
