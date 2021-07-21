package com.gj.tool.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.gj.tool.position.KeywordPosition;
import com.gj.tool.position.KeywordSearchOptions;
import com.gj.tool.position.KeywordSearchOptions.PositionType;
import com.gj.tool.position.PdfKeywordUtils;
import com.gj.tool.utils.ImageUtil;
import com.gj.tool.utils.PdfTextUtil;

public class SearchPanel extends JPanel implements BasePanel {

	private static final long serialVersionUID = 1L;

	private static String CURRENT_DIRECTORY = System.getProperty("user.dir");

	private static byte[] pdfBytes = null;

	private static KeywordSearchOptions searchOptions = new KeywordSearchOptions();

	private JTextField keywordField;

	private JFormattedTextField startField;

	private JFormattedTextField endField;

	private JFormattedTextField indexField;

	private JTextField totalPageField;

	private ButtonGroup positionGroup;

	private JCheckBox ikCheckBox;

	private JCheckBox icCheckBox;

	private JCheckBox ihCheckBox;

	private JCheckBox ipCheckBox;

	private ComContext comContext;

	private JTextField pdfPathField;
	
	private JTextField offsetXField;
	
	private JTextField offsetYField;

	public SearchPanel(ComContext comContext) {
		this.comContext = comContext;
		this.comContext.setSearchPanel(this);

		this.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH, CompSize.SEARCH_FORM_HEIGHT));

		this.add(filechooserPanel());

		this.add(keywordPanel());

		this.add(pagePanel());

		this.add(positionPanel());

		this.add(otherPanel());

		this.add(searchPanel());
	}

	public static byte[] getPdfBytes() {
		return pdfBytes;
	}

	private JPanel searchPanel() {
		JPanel searchPanel = getBasePanel();
		JButton searchButton = new JButton("搜      索");
		searchButton.setFocusPainted(false);
		searchButton.setMargin(new Insets(0, 0, 0, 0));
//		searchButton.setBorder(BorderFactory.createRaisedBevelBorder());
		searchButton.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH/3 - 10, CompSize.FILE_CHOORE_BUTTON_HEIGHT));
		
		offsetXField  = new JTextField();
		offsetXField.setToolTipText("x偏移量");
		offsetXField.setText("0.0");
		offsetXField.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH/3 - 10, CompSize.FILE_CHOORE_BUTTON_HEIGHT));
		
		offsetYField  = new JTextField();
		offsetYField.setToolTipText("y偏移量");
		offsetYField.setText("0.0");
		offsetYField.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH/3 - 10, CompSize.FILE_CHOORE_BUTTON_HEIGHT));

		searchButton.addActionListener((e) -> {
			if (pdfBytes == null || pdfBytes.length == 0) {
//				paintPdfImage("C:\\Users\\gj\\Desktop\\test_rotate.pdf");
				JOptionPane.showMessageDialog(comContext.getMainFrame(), "请先选择文件");
				return;
			}

			String keyword = keywordField.getText();
			if (keyword == null || "".equals(keyword.trim())) {
				JOptionPane.showMessageDialog(comContext.getMainFrame(), "关键字不能不为空");
				return;
			}
			handleSearchOptions();
			try {
				PdfTextUtil.readPdf(pdfBytes,searchOptions.getPage(), searchOptions.getPageEnd());
				searchOptions.setOtherPosition(new HashSet(Arrays.asList(PositionType.values())));
				Map<String, ArrayList<KeywordPosition>> keywordMap = PdfKeywordUtils.queryKeyword(pdfBytes, searchOptions);
				comContext.getCenterPanel().setPositionMap(keywordMap);
				List<KeywordPosition> positions = comContext.getCenterPanel().getPositions();
				comContext.getCenterPanel().paintPositions(positions, searchOptions);
				comContext.getPositionPanel().paintPosition();
				if (positions.size() == 0) {
					JOptionPane.showMessageDialog(comContext.getMainFrame(), "未找到关键字");
				}
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		});
		searchPanel.add(offsetXField);
		searchPanel.add(offsetYField);
		searchPanel.add(searchButton);

		handleSearchOptions();

		return searchPanel;
	}

	private void handleSearchOptions() {
		String keyword = keywordField.getText();
		searchOptions.setKeywords(new HashSet<String>(Arrays.asList(keyword.split(","))));

		int start = 1;
		try {
			start = Integer.parseInt(startField.getText());
		} catch (Exception e1) {
		}
		searchOptions.setPage(start);

		int end = Integer.MAX_VALUE;
		try {
			end = Integer.parseInt(endField.getText());
		} catch (Exception e1) {
		}
		searchOptions.setPageEnd(end);

		int index = 1;
		try {
			index = Integer.parseInt(indexField.getText());
		} catch (Exception e1) {
		}
		searchOptions.setKeyIndex(index);

		PositionType positionType = PositionType.LEFT_BOTTOM;
		Enumeration<AbstractButton> elements = positionGroup.getElements();
		while (elements.hasMoreElements()) {
			JRadioButton jRadioButton = (JRadioButton) elements.nextElement();
			if (jRadioButton.isSelected()) {
				positionType = PositionType.valueOf(jRadioButton.getName());
				break;
			}
		}
		searchOptions.setPosition(positionType);

		searchOptions.setIgnoreKeywordSpace(ikCheckBox.isSelected());
		searchOptions.setIgnoreContentSpace(icCheckBox.isSelected());
		searchOptions.setIgnoreNewline(ihCheckBox.isSelected());
		searchOptions.setIgnoreNewpage(ipCheckBox.isSelected());

	}
	

	private JPanel otherPanel() {
		JPanel otherPanel = new JPanel();
		otherPanel.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT * 2));
		FlowLayout f = (FlowLayout) otherPanel.getLayout();
		f.setHgap(0);// 水平间距
		f.setVgap(0);

		otherPanel.add(getBaseLabel("其他条件："));
		ikCheckBox = getBaseCheckBox("忽略关键字空格", true);
		otherPanel.add(ikCheckBox);
		icCheckBox = getBaseCheckBox("忽略文档空格", true);
		otherPanel.add(icCheckBox);
		otherPanel.add(getBaseLabel(""));
		ihCheckBox = getBaseCheckBox("忽略换行", false);
		otherPanel.add(ihCheckBox);
		ipCheckBox = getBaseCheckBox("忽略换页", false);
		otherPanel.add(ipCheckBox);
		return otherPanel;
	}

	private JCheckBox getBaseCheckBox(String title, boolean selected) {
		JCheckBox baseCheckBox = new JCheckBox(title);
		baseCheckBox.setSelected(selected);
		baseCheckBox.setPreferredSize(new Dimension(CompSize.PDF_PATH_FIELD_WIDTH / 2, CompSize.BASE_FORM_PANEL_HEIGHT - 10));
		return baseCheckBox;

	}

	private JPanel positionPanel() {
		JPanel positionPanel = new JPanel();
		positionPanel.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT * 3));
		FlowLayout f = (FlowLayout) positionPanel.getLayout();
		f.setHgap(0);// 水平间距
		f.setVgap(0);

		positionPanel.add(getBaseLabel("位置："));
		positionGroup = new ButtonGroup();
		positionPanel.add(getJRadioButton("左上", PositionType.LEFT_TOP, positionGroup));
		positionPanel.add(getJRadioButton("左中", PositionType.LEFT_CENTER, positionGroup));
		JRadioButton ld = getJRadioButton("左下", PositionType.LEFT_BOTTOM, positionGroup);
		positionPanel.add(ld);
		ld.setSelected(true);
		
		positionPanel.add(getBaseLabel(""));
		positionPanel.add(getJRadioButton("中上", PositionType.TOP_CENTER, positionGroup));
		positionPanel.add(getJRadioButton("中中", PositionType.CENTER_CENTER, positionGroup));
		positionPanel.add(getJRadioButton("中下", PositionType.BOTTOM_CENTER, positionGroup));
		
		positionPanel.add(getBaseLabel(""));
		positionPanel.add(getJRadioButton("右上", PositionType.RIGHT_TOP, positionGroup));
		positionPanel.add(getJRadioButton("右中", PositionType.RIGHT_CENTER, positionGroup));
		positionPanel.add(getJRadioButton("右下", PositionType.RIGHT_BOTTOM, positionGroup));

		return positionPanel;
	}

	private JRadioButton getJRadioButton(String title, PositionType positionType, ButtonGroup bg) {
		JRadioButton jrb = new JRadioButton(title);
		jrb.setName(positionType.name());
		bg.add(jrb);
		jrb.setPreferredSize(new Dimension(CompSize.PDF_PATH_FIELD_WIDTH / 3, CompSize.BASE_FORM_PANEL_HEIGHT));
		jrb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					JRadioButton source = (JRadioButton) e.getSource();
					if(source.isSelected()) {
						try {
							CenterPanel centerPanel = comContext.getCenterPanel();
							Map<String, ArrayList<KeywordPosition>> positionMap = centerPanel.getPositionMap();
							if(positionMap.size() > 0) {
								positionMap.forEach((k,v)->{
									ArrayList<KeywordPosition> newPs =new ArrayList<>();
									v.forEach(ps ->{
										KeywordPosition otherPosition = ps.getOtherPosition(positionType);
										otherPosition.setOtherPositions(ps.getOtherPositions());
										newPs.add(otherPosition);
									});
									positionMap.replace(k, newPs);
								});
								comContext.getCenterPanel().setPositionMap(positionMap);
								List<KeywordPosition> positions = comContext.getCenterPanel().getPositions();
								comContext.getCenterPanel().paintPositions(positions, searchOptions);
								comContext.getPositionPanel().paintPosition();
							}
						} catch (Exception e1) {
						}
					}
				}
				
			});
		return jrb;
	}

	private JPanel pagePanel() {
		JPanel pagePanel = getBasePanel();
		pagePanel.add(getBaseLabel("页数："));

		JLabel label0 = new JLabel("从");
		pagePanel.add(label0);

		startField = getNumberTextField(true,1);
		startField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		startField.setText("1");
		startField.setHorizontalAlignment(SwingConstants.CENTER);
		startField.addFocusListener(new SeachOptionFocusListener(this));
		startField.addKeyListener(new VoteElectKeyListener(true, false));
		pagePanel.add(startField);

		JLabel label1 = new JLabel("~");
		label1.setPreferredSize(new Dimension(10, CompSize.BASE_FORM_PANEL_HEIGHT));
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		pagePanel.add(label1);

		endField = getNumberTextField(true,3);
		endField.setText("3");
		endField.setHorizontalAlignment(SwingConstants.CENTER);
		endField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		endField.addFocusListener(new SeachOptionFocusListener(this));
		endField.addKeyListener(new VoteElectKeyListener(true, false));
		pagePanel.add(endField);

		JLabel label2 = new JLabel("页 共");
		pagePanel.add(label2);

		totalPageField = new JTextField();
		totalPageField.setHorizontalAlignment(SwingConstants.CENTER);
		totalPageField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH - 15, CompSize.BASE_FORM_PANEL_HEIGHT));
		totalPageField.setEditable(false);
//		totalPageField.setBorder(new EmptyBorder(0,0,0,0));
		pagePanel.add(totalPageField);

		JLabel label3 = new JLabel("页 第");
		pagePanel.add(label3);

		indexField = getNumberTextField(true,0);
		indexField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		indexField.setHorizontalAlignment(SwingConstants.CENTER);
		indexField.setToolTipText("<html>0：全部 <br>n：第n个<br>-n：表示倒数第n个</html>");
		indexField.addFocusListener(new SeachOptionFocusListener(this));
		indexField.addKeyListener(new VoteElectKeyListener(true, true));
		pagePanel.add(indexField);

		JLabel label4 = new JLabel("个关键字");
		pagePanel.add(label4);

		return pagePanel;
	}
	
	private JFormattedTextField getNumberTextField(boolean zeroable,int defValue) {
		NumberFormat integerInstance = NumberFormat.getIntegerInstance();
		if(!zeroable) {
			integerInstance.setMaximumIntegerDigits(1);
			integerInstance.setMinimumFractionDigits(0);
		}
		JFormattedTextField jFormattedTextField = new JFormattedTextField(integerInstance);
		jFormattedTextField.setValue(defValue);
		return jFormattedTextField;
	}
	
	static class VoteElectKeyListener implements KeyListener {
		private boolean subtractable;
		private boolean zeroable;
		public VoteElectKeyListener(boolean zeroable,boolean subtractable) {
			super();
			this.zeroable = zeroable;
			this.subtractable = subtractable;
		}
		@Override
		public void keyTyped(KeyEvent e) {
			int keyChar = e.getKeyChar();
			if (zeroable && keyChar == KeyEvent.VK_0 ||  keyChar > KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9 || subtractable && keyChar == 45) {
			} else {
				e.consume();
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {}
	}

	static class SeachOptionFocusListener implements FocusListener {

		private SearchPanel searchFormPanel;

		public SeachOptionFocusListener(SearchPanel searchFormPanel) {
			super();
			this.searchFormPanel = searchFormPanel;
		}

		@Override
		public void focusLost(FocusEvent e) {
			searchFormPanel.handleSearchOptions();
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

	}

	// 关键字选择框
	private JPanel keywordPanel() {
		JPanel keywordPanel = getBasePanel();
		keywordPanel.add(getBaseLabel("关键字："));

		keywordField = new JTextField();
//		keywordField.setText("关键字");
		keywordField.setPreferredSize(new Dimension(CompSize.PDF_PATH_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		keywordPanel.add(keywordField);
		keywordField.setToolTipText("多个关键字用‘,’隔开");
		return keywordPanel;
	}

	// 文件选择组件
	private JPanel filechooserPanel() {
		JPanel filePanel = getBasePanel();

		pdfPathField = new JTextField();
		pdfPathField.setEditable(false);
//		pdfPath.setBorder(new EmptyBorder(1,1,1,0));
		pdfPathField.setPreferredSize(new Dimension(CompSize.PDF_PATH_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));

		// 文件选择按钮
		JButton photoButton = new JButton("选择文件");
		photoButton.setFocusPainted(false);
		photoButton.setMargin(new Insets(0, 0, 0, 0));
//		photoButton.setBorder(BorderFactory.createRaisedBevelBorder());
		photoButton.setPreferredSize(new Dimension(CompSize.FILE_CHOORE_BUTTON_WIDTH, CompSize.FILE_CHOORE_BUTTON_HEIGHT));
		photoButton.addActionListener((e) -> {
			// 产生一个文件选择器
			JFileChooser filechooserOpen = new JFileChooser();
			SwingUtilities.updateComponentTreeUI(filechooserOpen);
			// 设置默认的打开目录,如果不设的话按照window的默认目录(我的文档)
			// 设置打开文件类型,此处设置成只能选择文件夹，不能选择文件
			filechooserOpen.setCurrentDirectory(new File(CURRENT_DIRECTORY));
			filechooserOpen.setFileSelectionMode(JFileChooser.FILES_ONLY);// 只能打开文件
			filechooserOpen.setAcceptAllFileFilterUsed(false);
			filechooserOpen.setFileFilter(new PdfFileFilter());
			// 打开一个对话框
			int index = filechooserOpen.showDialog(null, "打开文档");
			if (index == JFileChooser.APPROVE_OPTION) {
				// 把获取到的文件的绝对路径显示在文本编辑框中
				String pdfPathStr = filechooserOpen.getSelectedFile().getAbsolutePath();
				paintPdfImage(pdfPathStr);
			}
		});
		filePanel.add(photoButton);
		filePanel.add(pdfPathField);

		return filePanel;
	}

	public void paintPdfImage(String pdfPath) {
		pdfPathField.setText(pdfPath);
		CURRENT_DIRECTORY = pdfPath;
		try {
			pdfBytes = Files.readAllBytes(new File(pdfPath).toPath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(comContext.getMainFrame(), "文件加载错误");
			return;
		}
		PdfTextUtil.clear();
		PdfTextUtil.readPdf(pdfBytes,searchOptions.getPage(), searchOptions.getPageEnd());
		comContext.getPositionPanel().paintPosition();
		Map<Integer, BufferedImage> images = ImageUtil.getImages(pdfBytes, searchOptions.getPage(), searchOptions.getPageEnd());
		this.comContext.getCenterPanel().paintImage(images);
		String endText = endField.getText();
		if (endText == null || "".equals(endText.trim())) {
			endField.setText("" + images.size());
		} else {
			try {
				if (Integer.parseInt(endText) > Math.max(searchOptions.getPage(), searchOptions.getPageEnd())) {
					endField.setText("" + images.size());
				}
			} catch (Exception e) {
				endField.setText("" + images.size());
			}
		}
		totalPageField.setText("" + images.size());
	}

	private JLabel getBaseLabel(String title) {
		JLabel baseLabel = new JLabel(title);
		baseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		baseLabel.setPreferredSize(new Dimension(CompSize.BASE_LABEL_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		return baseLabel;
	}

	private JPanel getBasePanel() {
		JPanel basePanel = new JPanel();
		basePanel.getInsets(new Insets(0, 0, 0, 0));
		basePanel.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH, CompSize.FILE_CHOORE_BUTTON_HEIGHT + 10));
		basePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		FlowLayout f = (FlowLayout) basePanel.getLayout();
		f.setHgap(0);// 水平间距
		return basePanel;
	}

	// 重写文件过滤器，设置打开类型中几种可选的文件类型，这里设了两种，一种txt，一种xls
	private static class PdfFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			} else {
				String nameString = f.getName();
				return nameString.toLowerCase().endsWith(".pdf");
			}
		}

		@Override
		public String getDescription() {
			return "*.pdf(pdf文档)";
		}
	}
	
	public double getOffsetX() {
		try {
			return Double.parseDouble(offsetXField.getText());
		} catch (Exception e) {
		}
		return 0;
	}
	
	public double getOffsetY() {
		try {
			return Double.parseDouble(offsetYField.getText());
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public ComContext getComContext() {
		return comContext;
	}

	public JTextField getKeywordField() {
		return keywordField;
	}

	public JTextField getStartField() {
		return startField;
	}

	public JTextField getEndField() {
		return endField;
	}

	public JTextField getIndexField() {
		return indexField;
	}

	public ButtonGroup getPositionGroup() {
		return positionGroup;
	}

	public JCheckBox getIkCheckBox() {
		return ikCheckBox;
	}

	public JCheckBox getIcCheckBox() {
		return icCheckBox;
	}

	public JCheckBox getIhCheckBox() {
		return ihCheckBox;
	}

	public JCheckBox getIpCheckBox() {
		return ipCheckBox;
	}

	public JTextField getPdfPathField() {
		return pdfPathField;
	}

	public void setComContext(ComContext comContext) {
		this.comContext = comContext;
	}
	
}
