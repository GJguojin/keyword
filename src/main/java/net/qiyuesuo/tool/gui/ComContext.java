package net.qiyuesuo.tool.gui;

import javax.swing.JScrollPane;

public class ComContext {
	
	private MainFrame mainFrame;
	
	private CenterPanel centerPanel;
	
	private EastPanle eastPanle;
	
	private ImgPanel imgPanel;
	
	private SearchFormPanel searchFormPanel;
	
	private JScrollPane centerScrollPane;

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public CenterPanel getCenterPanel() {
		return centerPanel;
	}

	public void setCenterPanel(CenterPanel centerPanel) {
		this.centerPanel = centerPanel;
	}

	public EastPanle getEastPanle() {
		return eastPanle;
	}

	public void setEastPanle(EastPanle eastPanle) {
		this.eastPanle = eastPanle;
	}

	public ImgPanel getImgPanel() {
		return imgPanel;
	}

	public void setImgPanel(ImgPanel imgPanel) {
		this.imgPanel = imgPanel;
	}

	public SearchFormPanel getSearchFormPanel() {
		return searchFormPanel;
	}

	public void setSearchFormPanel(SearchFormPanel searchFormPanel) {
		this.searchFormPanel = searchFormPanel;
	}

	public JScrollPane getCenterScrollPane() {
		return centerScrollPane;
	}

	public void setCenterScrollPane(JScrollPane centerScrollPane) {
		this.centerScrollPane = centerScrollPane;
	}
}
