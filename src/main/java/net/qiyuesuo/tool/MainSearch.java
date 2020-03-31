package net.qiyuesuo.tool;

import javax.swing.UIManager;

import net.qiyuesuo.tool.gui.CompSize;
import net.qiyuesuo.tool.gui.MainFrame;

public class MainSearch {
	
	public static void main(String[] args) {
		new MainSearch();
	}

	
	public MainSearch() {
		UIManager.put("TabbedPane.selected", CompSize.BASE_COLOR_TABLE_SELECTED);
		MainFrame init = new MainFrame("PDF关键字搜索器").init();
		init.setVisible(true);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	

	

}
