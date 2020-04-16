package net.qiyuesuo.tool.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.qiyuesuo.tool.position.KeywordPosition;
import net.qiyuesuo.tool.utils.PositionUtil;

public class ImgPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private int page;
	private BufferedImage bufferedImage;
	private int realWidth = CompSize.IMAGE_WIDTH;
	private int realHeight;

	private ComContext comContext; // 主面板

	private Map<String,JPanel> positionPanels = new TreeMap<>();

	public ImgPanel(int page, BufferedImage bufferedImage, ComContext comContext) {
		super();
		this.comContext = comContext;
		this.page = page;
		this.bufferedImage = bufferedImage;
		if (bufferedImage != null) {
			reSize();
		} else {
			this.setVisible(false);
		}
		this.setLayout(null);

		this.add(textButton());
	}

	private JButton textButton() {
		JButton jButton = new JButton("查询原文");
		jButton.setBackground(new Color(230, 230, 230));
		jButton.setBounds(0, 0, 100, 50);
		jButton.setFocusPainted(false);
		jButton.setMargin(new Insets(0, 0, 0, 0));
		jButton.addActionListener((e) -> {
			PdfTextPanel pdfTextPanel = new PdfTextPanel(comContext,page);
			JOptionPane.showMessageDialog(comContext.getCenterScrollPane(),new JScrollPane(pdfTextPanel) , "第" + page + "内容", -1);
		});
		return jButton;

	}

	public void reSize() {
		this.realHeight = bufferedImage.getHeight() * CompSize.IMAGE_WIDTH / bufferedImage.getWidth();
		this.setSize(realWidth, realHeight);
		this.setPreferredSize(new Dimension(realWidth, realHeight));
		this.setBounds(0, 0, realWidth, realHeight);
		this.setVisible(true);
	}

	/*@Override
	public void paint(Graphics g) {
		System.out.println(page + " paintComponent");
		super.paint(g);
		// 清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
		g.clearRect(0, 0, realWidth, realHeight);
		g.drawImage(bufferedImage, 0, 0, realWidth, realHeight, null);
		printPage(g);
	}*/

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// 清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
		g.clearRect(0, 0, realWidth, realHeight);
		g.drawImage(bufferedImage, 0, 0, realWidth, realHeight, null);
		printPage(g);
	}
	
	public void paintPosition(String tableKey) {
		positionPanels.forEach((k,v) -> {
			this.remove(v);
		});
		positionPanels.clear();

		if (!this.isViewable()) {
			return;
		}

		int rectWidth = 100;
		int rectHeight = 30;
		Map<String, ArrayList<KeywordPosition>> positionMap = comContext.getCenterPanel().getPositionMap();
		positionMap.forEach((k,ps)->{
			List<KeywordPosition> positions = ps.stream().filter(p -> p.getPage() == page).collect(Collectors.toList());
			for (KeywordPosition position : positions) {
				JPanel jpanel = new JPanel();
				String key = PositionUtil.getKey(position);
				positionPanels.put(key,jpanel);
				if(key.equals(tableKey)) {
					jpanel.setBackground(CompSize.BASE_COLOR_GREEN_TRANPARENT);
				}else {
					jpanel.setBackground(CompSize.BASE_COLOR_RED_TRANPARENT);
				}
				jpanel.setBounds((int) (realWidth * position.getX()), (int) (realHeight * (1 - position.getY()) - rectHeight), rectWidth, rectHeight);
				jpanel.addMouseListener(new PositionMouseListener(k,key,comContext));
				this.add(jpanel);
			}
		});
		
		
		this.repaint();
	}
	
	static class PositionMouseListener implements MouseListener{
		
		private String keyword;
		private String key;
		private ComContext comContext;
		public PositionMouseListener(String keyword,String key,ComContext comContext) {
			super();
			this.key = key;
			this.keyword = keyword;
			this.comContext = comContext;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 1) {
				PositionPanel panel = comContext.getPositionPanel();
				panel.setSelectedComponent(panel.getPanel(keyword));
				
				JTable table = panel.getTable(keyword);
				if(table == null) {
					return;
				}
				int rowCount = table.getRowCount();
				String tableKey = null;
				for(int i =0;i<rowCount;i++) {
					tableKey = (String) table.getValueAt(i, 5);
					if(key.equals(tableKey)) {
						table.setRowSelectionInterval(i,i);
						break;
					}
				}
				if(tableKey != null) {
					CenterPanel centerPanel = comContext.getCenterPanel();
					Map<Integer, ImgPanel> imgPanels = centerPanel.getImgPanels();
					for(ImgPanel img : imgPanels.values()) {
						img.paintPosition(tableKey);
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public ComContext getComContext() {
			return comContext;
		}

		public void setComContext(ComContext comContext) {
			this.comContext = comContext;
		}
	}

	private void printPage(Graphics g) {
		int radius = 40;
		int fontSize = 18;
		Graphics2D g2 = (Graphics2D) g;// 强制类型转换
		g2.setColor(new Color(128, 128, 128, 60));// 设置当前绘图颜色
		g2.fillOval(realWidth - radius, realHeight - radius, radius * 2, radius * 2);
		g2.setColor(Color.BLACK);
		g2.setFont(new Font(null, Font.BOLD, fontSize));
		String pageStr = "" + page;
		g2.drawString(pageStr, realWidth - radius + (radius / (pageStr.length() + 1)), realHeight - radius / 4);// 绘制事件文本
	}

	public boolean isViewable() {
		return bufferedImage != null && this.isVisible();
	}
	
	public JPanel getPositionPanel(String tableKey) {
		return positionPanels.get(tableKey);
	}
	

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	public int getRealWidth() {
		return realWidth;
	}

	public void setRealWidth(int realWidth) {
		this.realWidth = realWidth;
	}

	public int getRealHeight() {
		return realHeight;
	}

	public void setRealHeight(int realHeight) {
		this.realHeight = realHeight;
	}

	public ComContext getComContext() {
		return comContext;
	}

	public void setComContext(ComContext comContext) {
		this.comContext = comContext;
	}

}
