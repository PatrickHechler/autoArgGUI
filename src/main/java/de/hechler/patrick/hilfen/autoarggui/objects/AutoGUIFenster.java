package de.hechler.patrick.hilfen.autoarggui.objects;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import javax.swing.*;
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
	
	private final int    high;
	private final int    whidh;
	private final int    empty;
	private MainMethod   main;
	private Arguments    args;
	private JFrame       frame;
	private JButton      finish;
	private JButton      save;
	private JButton      load;
	private JFileChooser normalFC;
	private JFileChooser argsFC;
	private int          shownOptions = 0;
	private Line[]       lines;
	
	public AutoGUIFenster(MainMethod main, Arguments args, int high, int whidh, int empty) {
		this.main = main;
		this.args = args;
		this.high = high;
		this.whidh = whidh;
		this.empty = empty;
	}
	
	public void load(String title) {
		load(title, null, null);
	}
	
	public void load(String title, final String finishMessage, final String msgTitle) {
		frame = new JFrame();
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
		frame.setTitle(title);
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
		lines = args.getAll();
		rebuild();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void rebuild() throws AssertionError {
		int xPos, yPos;
		yPos = empty + high + empty;
		for (Line line : lines) {
			xPos = empty;
			GUIArt[] arten = line.arten();
			final int singleWhidh = whidh - (empty * (1 + arten.length));
			int neededShownOptions = 0;
			for (int _li = 0; _li < arten.length && neededShownOptions <= shownOptions; _li ++ ) {
				final int needShownOptionsConst = neededShownOptions;
				final int li = _li;
				final Component comp;
				switch (arten[li]) {
				case comboBoxFalseTrue:
				case comboBoxTrueFalse:
					comp = new JComboBox <>(line.comboBoxText(li));
					comp.addFocusListener(new FocusAdapter() {
						
						@Override
						public void focusLost(FocusEvent e) {
							Class <?> type = line.getType(li);
							if (type == Boolean.TYPE || type == Boolean.class) {
								boolean reverse = arten[li] == GUIArt.comboBoxTrueFalse;
								boolean set = ((JComboBox <?>) comp).getSelectedIndex() == 0;
								set = set ^ reverse;
								line.setValue(li, set);
							} else if (type == Integer.TYPE || type == Integer.class) {
								line.setValue(li, ((JComboBox <?>) comp).getSelectedIndex());
							} else if (type == String.class) {
								line.setValue(li, ((JComboBox <?>) comp).getSelectedItem());
							} else {
								throw new ClassCastException("unknown type for a combo box: " + type.getName());
							}
						}
						
					});
					break;
				case showBelowOptionsButton:
					comp = new JButton(line.showOptsFirstText(li));
					((JButton) comp).addActionListener(new ActionListener() {
						
						boolean hide = false;
						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (hide) {
								((JButton) comp).setText(line.showOptsFirstText(li));
								shownOptions = needShownOptionsConst;
							} else {
								((JButton) comp).setText(line.showOptsSecondText(li));
								shownOptions = needShownOptionsConst + 1;
							}
							hide = !hide;
							rebuild();
						}
						
					});
					neededShownOptions ++;
					break;
				case deleteButton:
					//TODO
					break;
				case fileChoose:
					//TODO
					break;
				case number:
					//TODO
					break;
				case choosenFileModifiable:
				case modifiableText:
					//TODO
					break;
				case choosenFileunmodifiable:
				case unmodifiableText:
					//TODO
					break;
				case ownWindow:
					//TODO
					break;
				default:
					throw new AssertionError("unknown GUIArt: " + arten[li].name());
				}
			}
		}
	}
	
}
