package com.gj.tool.gui;

import javax.swing.JScrollPane;

public class ComContext {
	
	private MainFrame mainFrame;
	
	private CenterPanel centerPanel;
	
	private EastPanle eastPanle;
	
	private SearchPanel searchPanel;
	
	private JScrollPane centerScrollPane;
	
	private PositionPanel positionPanel;

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


	public SearchPanel getSearchPanel() {
		return searchPanel;
	}

	public void setSearchPanel(SearchPanel searchPanel) {
		this.searchPanel = searchPanel;
	}

	public JScrollPane getCenterScrollPane() {
		return centerScrollPane;
	}

	public void setCenterScrollPane(JScrollPane centerScrollPane) {
		this.centerScrollPane = centerScrollPane;
	}

	public PositionPanel getPositionPanel() {
		return positionPanel;
	}

	public void setPositionPanel(PositionPanel positionPanel) {
		this.positionPanel = positionPanel;
	}
	
}
