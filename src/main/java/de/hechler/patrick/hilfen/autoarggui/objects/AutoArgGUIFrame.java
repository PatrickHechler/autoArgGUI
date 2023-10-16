package de.hechler.patrick.hilfen.autoarggui.objects;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import de.hechler.patrick.hilfen.autoarggui.enums.GUIArt;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Arguments;
import de.hechler.patrick.hilfen.autoarggui.interfaces.Line;
import de.hechler.patrick.hilfen.autoarggui.interfaces.MainMethod;

@SuppressWarnings("javadoc")
public class AutoArgGUIFrame {
	
	private final AutoArgGUIFrame                       superWindow;
	private final int                                   high;
	private final int                                   empty;
	private int                                         whidh;
	private MainMethod                                  main;
	private Arguments                                   args;
	private Line[]                                      lines;
	private Line                                        parentLine;
	private int                                         parentLineIndex;
	private final JFrame                                frame;
	private final JButton                               finish;
	private final JButton                               addNew;                          /* if subWindow: addNew */
	private final JButton                               delAll;                          /* if subWindow: delAll */
	private final JFileChooser                          normalFC;
	private final JFileChooser                          argsFC;
	private int                                         shownOptions = 0;
	private Map<Integer, Map<Integer, AutoArgGUIFrame>> subWindows   = new HashMap<>();
	
	public AutoArgGUIFrame(String title, MainMethod main, Arguments args) {
		this(title, main, args, 20, 500, 10);
	}
	
	public AutoArgGUIFrame(String title, MainMethod main, Arguments args, int high, int whidh, int empty) {
		this.superWindow = null;
		this.high        = high;
		this.whidh       = whidh;
		this.empty       = empty;
		this.main        = main;
		this.args        = args;
		this.frame       = new JFrame(title);
		this.addNew        = new JButton("save");
		this.delAll        = new JButton("load");
		this.finish      = new JButton("finish");
		this.normalFC    = new JFileChooser();
		this.argsFC      = new JFileChooser();
	}
	
	private AutoArgGUIFrame(AutoArgGUIFrame sw, String title, String addNewText, String delAllText, int high, int whidh, int empty) {
		this.superWindow = sw;
		this.high        = high;
		this.whidh       = whidh;
		this.empty       = empty;
		this.lines       = null;
		this.frame       = new JFrame(title);
		this.addNew        = new JButton(addNewText);
		this.delAll        = new JButton(delAllText);
		this.finish      = null;
		this.normalFC    = new JFileChooser();
		this.argsFC      = null;
	}
	
	private AutoArgGUIFrame subWindowLoad() {
		this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.frame.setLayout(null);
		this.addNew.addActionListener(ae -> {
			this.parentLine.addLine(this.parentLineIndex);
			this.superWindow.rebuild(false);
		});
		this.frame.add(this.addNew);
		this.delAll.addActionListener(ae -> {
			this.parentLine.removeAllLines(this.parentLineIndex);
			this.superWindow.rebuild(false);
		});
		this.frame.add(this.delAll);
		return this;
	}
	
	public void load() {
		load((Supplier<String>) null, null);
	}
	
	public void load(String finishMessage, String msgTitle) {
		load(() -> finishMessage, () -> msgTitle);
	}
	public void load(Supplier<String> finishMessage, Supplier<String> msgTitle) {
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setLayout(null);
		@Deprecated
		FileFilter ff = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "* [all except for hidden]";
			}
			
			@Override
			public boolean accept(File f) {
				return !f.isHidden();
			}
			
		};
		this.argsFC.addChoosableFileFilter(ff);
		this.argsFC.setFileFilter(ff);
		ff = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "* [all]";
			}
			
			@Override
			public boolean accept(@SuppressWarnings("unused") File f) {
				return true;
			}
			
		};
		this.argsFC.addChoosableFileFilter(ff);
		this.argsFC.setAcceptAllFileFilterUsed(false);
		this.finish.addActionListener(ae -> {
			this.main.main(this.args.toArgs());
			String fm = finishMessage == null ? null : finishMessage.get();
			String mt = msgTitle == null ? null : msgTitle.get();
			if (fm != null || mt != null) {
				JOptionPane.showMessageDialog(this.frame, fm, mt, JOptionPane.INFORMATION_MESSAGE);
			}
			this.frame.dispose();
		});
		this.addNew.addActionListener(ae -> {
			int ret = this.argsFC.showSaveDialog(this.frame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					File sf = this.argsFC.getSelectedFile();
					if (sf.exists()) {
						ret = JOptionPane.showConfirmDialog(this.frame, "the file (" + sf.getPath() + ") exist already\nshould I overwrite it?", "existing target",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (ret != JOptionPane.OK_OPTION) {
							return;
						}
					}
					try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sf)))) {
						out.writeObject(this.args);
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this.frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		this.delAll.addActionListener(ae -> {
			int ret = this.argsFC.showOpenDialog(this.frame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					File sf = this.argsFC.getSelectedFile();
					try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(sf)))) {
						this.args = (Arguments) in.readObject();
					}
					rebuild(false);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this.frame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		positionHeader();
		this.frame.add(this.finish);
		this.frame.add(this.addNew);
		this.frame.add(this.delAll);
		rebuild(true);
	}
	
	// only call when there should be an header
	private void positionHeader() {
		int xPos        = this.empty;
		int yPos        = this.empty;
		int singleWhidh = (this.whidh - (this.empty * 5)) / 3;
		this.finish.setBounds(xPos, yPos, singleWhidh, this.high);
		xPos += singleWhidh + this.empty;
		this.addNew.setBounds(xPos, yPos, singleWhidh, this.high);
		xPos += singleWhidh + this.empty;
		this.delAll.setBounds(xPos, yPos, singleWhidh, this.high);
	}
	
	private final List<Component> removableComps = new ArrayList<>();
	
	private Runnable finHook = null;
	
	private void rebuild(boolean setVisible) throws AssertionError {
		for (Component rem : this.removableComps) {
			this.frame.remove(rem);
		}
		this.removableComps.clear();
		int[] lineCounts;
		int   xPos;
		int   yPos;
		if (this.superWindow == null) {
			yPos = this.empty + this.high + this.empty;
		} else {
			yPos = this.empty;
		}
		Line[] all;
		if (this.superWindow == null) {
			all           = this.args.getAllLines();
			lineCounts    = new int[all.length + 1];
			lineCounts[0] = 3;
		} else {
			all                    = this.lines;
			lineCounts             = new int[all.length + 1];
			lineCounts[all.length] = 2;
		}
		int neededShownOptions = 0;
		for (int _i = 0; _i < all.length && neededShownOptions >= this.shownOptions; _i++) {
			final int i = _i;
			xPos = this.empty;
			GUIArt[] arten = all[i].arten();
			int      lci   = this.superWindow == null ? i + 1 : i;
			lineCounts[lci] = arten.length;
			final int singleWhidh           = width(arten.length);
			final int needShownOptionsConst = neededShownOptions;
			for (int _li = 0; _li < arten.length; _li++) {
				final int       li = _li;
				final Component comp;
				switch (arten[li]) {
				case comboBox:
					comp = new JComboBox<>(all[i].comboBoxText(li));
					if (all[i].hasValue(li)) {
						Class<?> zwType = all[i].getType(li);
						if (zwType == Integer.TYPE || zwType == Integer.class) {
							int index = ((Integer) all[i].getValue(li)).intValue();
							((JComboBox<?>) comp).setSelectedIndex(index);
						} else if (zwType == Boolean.TYPE || zwType == Boolean.class) {
							boolean index = ((Boolean) all[i].getValue(li)).booleanValue();
							((JComboBox<?>) comp).setSelectedIndex(index ? 1 : 0);
						} else {
							Object item = all[i].getValue(li);
							((JComboBox<?>) comp).setSelectedItem(item);
						}
					}
					comp.addFocusListener(new FocusAdapter() {
						
						@SuppressWarnings("unused")
						@Override
						public void focusLost(FocusEvent e) {
							Class<?> type = all[i].getType(li);
							if (type == Boolean.TYPE || type == Boolean.class) {
								boolean set = ((JComboBox<?>) comp).getSelectedIndex() == 0;
								all[i].setValue(li, Boolean.valueOf(set));
							} else if (type == Integer.TYPE || type == Integer.class) {
								all[i].setValue(li, Integer.valueOf(((JComboBox<?>) comp).getSelectedIndex()));
							} else if (type == String.class) {
								all[i].setValue(li, ((JComboBox<?>) comp).getSelectedItem());
							} else {
								throw new ClassCastException("unknown type for a combo box: " + type.getName());
							}
						}
						
					});
					break;
				case showBelowOptionsButton:
					comp = new JButton(all[i].twoValuesFirstText(li));
					((JButton) comp).addActionListener(e -> {
						if (AutoArgGUIFrame.this.shownOptions > needShownOptionsConst) {
							((JButton) comp).setText(all[i].twoValuesFirstText(li));
							AutoArgGUIFrame.this.shownOptions = needShownOptionsConst;
						} else {
							((JButton) comp).setText(all[i].twoValuesSecondText(li));
							AutoArgGUIFrame.this.shownOptions = needShownOptionsConst + 1;
						}
						rebuild(false);
					});
					neededShownOptions++;
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
						int ret = this.normalFC.showDialog(comp, all[i].twoValuesSecondText(li));
						if (ret == JFileChooser.APPROVE_OPTION) {
							Class<?> cls = all[i].getType(li);
							if (cls == String.class) {
								all[i].setValue(li, this.normalFC.getSelectedFile().getPath());
							} else {
								all[i].setValue(li, this.normalFC.getSelectedFile());
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
						public void focusLost(@SuppressWarnings("unused") FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							if (this.oldText.equals(newText)) {
								return;
							}
							Class<?> type = all[i].getType(li);
							try {
								if (type == Long.TYPE || type == Long.class) {
									all[i].setValue(li, Long.valueOf(newText));
								} else if (type == Integer.TYPE || type == Integer.class) {
									all[i].setValue(li, Integer.valueOf(newText));
								} else if (type == Short.TYPE || type == Short.class) {
									all[i].setValue(li, Short.valueOf(newText));
								} else if (type == Byte.TYPE || type == Byte.class) {
									all[i].setValue(li, Byte.valueOf(newText));
								} else if (type == Double.TYPE || type == Double.class) {
									all[i].setValue(li, Double.valueOf(newText));
								} else if (type == Float.TYPE || type == Float.class) {
									all[i].setValue(li, Float.valueOf(newText));
								} else if (type == BigInteger.class) {
									all[i].setValue(li, new BigInteger(newText));
								} else if (type == BigDecimal.class) {
									all[i].setValue(li, new BigDecimal(newText));
								} else {
									all[i].setValue(li, newText);
								}
								this.oldText = newText;
							} catch (Exception err) {
								JOptionPane.showMessageDialog(AutoArgGUIFrame.this.frame, err.getMessage(), err.getClass().getName(), JOptionPane.ERROR_MESSAGE);
								((JTextField) comp).setText(this.oldText);
							}
						}
						
					});
					break;
				}
				case passwordText: {
					comp = new JPasswordField();
					final String text = all[i].normalText(li);
					if (text != null && !text.isEmpty()) {
						((JPasswordField) comp).setText(text);
					}
					final Runnable r = this.finHook;
					this.finHook = () -> {
						if (r != null) {
							r.run();
						}
						char[] pw = ((JPasswordField) comp).getPassword();
						all[i].setValue(li, pw);
					};
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
						public void focusLost(@SuppressWarnings("unused") FocusEvent e) {
							String newText = ((JTextField) comp).getText();
							if (this.oldText.equals(newText)) {
								return;
							}
							all[i].setValue(li, newText);
							this.oldText = newText;
						}
						
					});
					break;
				}
				case choosenFileunmodifiable: {
					comp = new JTextPane();
					final String text = all[i].normalText(li);
					((JTextPane) comp).setEditable(false);
					((JTextPane) comp).setText(text);
					break;
				}
				case unmodifiableText: {
					comp = new JLabel();
					final String text = all[i].normalText(li);
					((JLabel) comp).setText(text);
					break;
				}
				case ownWindow: {
					comp = new JButton(all[i].fourValuesFirstText(li));
					final AutoArgGUIFrame         sw;
					all[i].initSubLines();
					Map<Integer, AutoArgGUIFrame> zw = this.subWindows.get(Integer.valueOf(i));
					if (zw == null) {
						zw = new HashMap<>();
						sw = new AutoArgGUIFrame(this.superWindow != null ? this.superWindow : this, all[i].fourValuesSecondText(li), all[i].fourValuesThirdText(li),
								all[i].fourValuesFourthText(li), this.high, this.whidh, this.empty);
						zw.put(Integer.valueOf(li), sw);
						this.subWindows.put(Integer.valueOf(i), zw);
					} else {
						AutoArgGUIFrame zw0 = zw.get(Integer.valueOf(li));
						if (zw0 == null) {
							sw = new AutoArgGUIFrame(this.superWindow != null ? this.superWindow : this, all[i].fourValuesFirstText(li),
									all[i].fourValuesThirdText(li), all[i].fourValuesFourthText(li), this.high, this.whidh, this.empty);
						} else {
							sw = zw0;
						}
					}
					((JButton) comp).addActionListener(ae -> sw.rebuild(true));
					sw.lines = all[i].subLines(li);
					if (sw.parentLine == null) {
						sw.parentLine      = all[i];
						sw.parentLineIndex = li;
						sw.subWindowLoad();
					} else if (sw.parentLine != all[i]) {
						sw.parentLine      = all[i];
						sw.parentLineIndex = li;
						for (ActionListener al : sw.addNew.getActionListeners()) {
							sw.addNew.removeActionListener(al);
						}
						for (ActionListener al : sw.delAll.getActionListeners()) {
							sw.addNew.removeActionListener(al);
						}
						sw.subWindowLoad();
					}
					sw.rebuild(false);
					break;
				}
				default:
					throw new AssertionError("unknown GUIArt: " + arten[li].name());
				}
				comp.setBounds(xPos, yPos, singleWhidh, this.high);
				this.frame.add(comp);
				this.removableComps.add(comp);
				xPos += singleWhidh + this.empty;
			}
			yPos += this.high + this.empty;
		}
		if (this.superWindow != null) {
			xPos = this.empty;
			int singleWhidh = (this.whidh - (this.empty * 4)) / 2;
			this.addNew.setBounds(xPos, yPos, singleWhidh, this.high);
			xPos += singleWhidh + this.empty;
			this.delAll.setBounds(xPos, yPos, singleWhidh, this.high);
			yPos += this.high + this.empty;
		}
		int w = this.whidh;
		int h = yPos + this.empty;
		this.frame.setSize(w, h);
		this.frame.setLocationRelativeTo(null);
		if (setVisible) {
			this.frame.setVisible(true);
		} else {
			this.frame.repaint();
		}
		Insets add = this.frame.getInsets();
		w += add.left + add.right;
		h += add.top + add.bottom;
		this.frame.setSize(w, h);
		checkMinSizes(setVisible, lineCounts);
	}
	
	private void checkMinSizes(boolean setVisible, int[] lineCounts) throws AssertionError {
		boolean   modify = false;
		Container pane   = this.frame.getContentPane();
		for (int i = 0, cnt = pane.getComponentCount(), i2 = -1, y2 = -1; i < cnt; i++) {
			Component c = pane.getComponent(i); // alternatively i2 = 0, y2 = this.empty
			if (y2 != c.getY()) {
				y2 = c.getY();
				i2++;
			}
			Dimension min = c.getMinimumSize();
			if (min.width > width(lineCounts[i2])) {
				modify     = true;
				this.whidh = calcWidth(min.width, lineCounts[i2]);
			}
		}
		if (modify) {
			if (this.superWindow == null) {
				positionHeader();
			}
			rebuild(setVisible);
		}
	}
	
	private int calcWidth(int width, int lineCount) {
		return (width * lineCount) + (this.empty * (lineCount + 1));
	}
	
	private int width(int lineCount) {
		return (this.whidh - (this.empty * (lineCount + 1))) / lineCount;
	}
	
}
