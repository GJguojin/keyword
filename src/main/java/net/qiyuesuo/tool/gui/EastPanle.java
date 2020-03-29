package net.qiyuesuo.tool.gui;

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
		FlowLayout f=(FlowLayout)this.getLayout();
		f.setVgap(0);
		this.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH,comContext.getMainFrame().getHeight()));
		
		SearchPanel searchFormPanel = new SearchPanel(comContext);
		comContext.setSearchPanel(searchFormPanel);
		this.add(searchFormPanel);
		
		PositionPanel positionPanel = new PositionPanel(comContext);
		comContext.setPositionPanel(positionPanel);
		this.add(positionPanel);

	}

	@Override
	public ComContext getComContext() {
		return comContext;
	}
	
	
}
