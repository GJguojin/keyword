package net.qiyuesuo.tool.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

public class EastPanle extends JPanel  implements BasePanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ComContext comContext;
	
	public EastPanle(ComContext comContext) {
		super();
		this.comContext = comContext;
		comContext.setEastPanle(this);
		this.setBackground(Color.yellow); 
		FlowLayout f=(FlowLayout)this.getLayout();
		f.setVgap(0);
		this.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH,comContext.getMainFrame().getHeight()));
		
		SearchFormPanel searchFormPanel = new SearchFormPanel(comContext);
		comContext.setSearchFormPanel(searchFormPanel);
		this.add(searchFormPanel);

	}

	@Override
	public ComContext getComContext() {
		return comContext;
	}
	
	
}
