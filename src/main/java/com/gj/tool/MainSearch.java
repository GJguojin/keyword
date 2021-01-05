package com.gj.tool;

import javax.swing.UIManager;

import com.gj.tool.gui.CompSize;
import com.gj.tool.gui.MainFrame;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.log.SysoLogger;

public class MainSearch {
	
	public static void main(String[] args) {
		new MainSearch();
	}

	
	public MainSearch() {
		LoggerFactory instance = LoggerFactory.getInstance();
		instance.setLogger(new SysoLogger());
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
