package net.qiyuesuo.tool;

import javax.swing.UIManager;

import net.qiyuesuo.tool.gui.MainFrame;

public class MainSearch {
	
	public static void main(String[] args) {
		new MainSearch();
	}

	
	public MainSearch() {
		MainFrame init = new MainFrame("PDF关键字搜索器").init();
		init.setVisible(true);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	

	

}
