package net.qiyuesuo.tool.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import net.qiyuesuo.tool.position.KeywordSearchOptions;
import net.qiyuesuo.tool.position.KeywordSearchOptions.PositionType;
import net.qiyuesuo.tool.utils.ImageUtil;

public class SearchFormPanel extends JPanel implements BasePanel{

	private static final long serialVersionUID = 1L;

	private static String CURRENT_DIRECTORY = System.getProperty("user.dir");

	private static String pdfPathStr;

	private JTextField keywordField;

	private JTextField startField;

	private JTextField endField;

	private JTextField indexField;

	private ButtonGroup positionGroup;

	private JCheckBox ikCheckBox;

	private JCheckBox icCheckBox;

	private JCheckBox ihCheckBox;

	private JCheckBox ipCheckBox;
	
	private ComContext comContext;

	private JTextField pdfPathField;

	public SearchFormPanel(ComContext comContext) {
		this.comContext = comContext;
		this.comContext.setSearchFormPanel(this);
		
		this.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH, CompSize.SEARCH_FORM_HEIGHT));

		this.add(filechooserPanel());

		this.add(keywordPanel());

		this.add(pagePanel());

		this.add(positionPanel());

		this.add(otherPanel());

		this.add(searchPanel());
	}

	private JPanel searchPanel() {
		JPanel searchPanel = getBasePanel();
		JButton searchButton = new JButton("搜      索");
		searchButton.setFocusPainted(false);
		searchButton.setMargin(new Insets(0, 0, 0, 0));
//		searchButton.setBorder(BorderFactory.createRaisedBevelBorder());
		searchButton.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH - 30, CompSize.FILE_CHOORE_BUTTON_HEIGHT));

		searchButton.addActionListener((e) -> {
			KeywordSearchOptions searchOptions = getSearchOptions();
			if(searchOptions == null) {
				return;
			}
		});
		searchPanel.add(searchButton);

		return searchPanel;
	}

	private KeywordSearchOptions getSearchOptions() {
		KeywordSearchOptions searchOptions = new KeywordSearchOptions();
		String keyword = keywordField.getText();
		if (keyword == null || "".equals(keyword.trim())) {
			JOptionPane.showMessageDialog(this, "关键字不能不为空");
			return null;
		}
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
		while(elements.hasMoreElements()) {
			JRadioButton jRadioButton = (JRadioButton) elements.nextElement();
			if(jRadioButton.isSelected()) {
				positionType = PositionType.valueOf(jRadioButton.getName());
				break;
			}
		}
		searchOptions.setPosition(positionType);
		
		searchOptions.setIgnoreKeywordSpace(ikCheckBox.isSelected());
		searchOptions.setIgnoreContentSpace(icCheckBox.isSelected());
		searchOptions.setIgnoreNewline(ihCheckBox.isSelected());
		searchOptions.setIgnoreNewpage(ipCheckBox.isSelected());
		
		return searchOptions;
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
		JPanel positionPanel = getBasePanel();
		positionPanel.add(getBaseLabel("位置："));

		positionGroup = new ButtonGroup();
		positionPanel.add(getJRadioButton("左上",PositionType.LEFT_TOP, positionGroup));
		JRadioButton ld = getJRadioButton("左下",PositionType.LEFT_BOTTOM, positionGroup);
		positionPanel.add(ld);
		positionPanel.add(getJRadioButton("右上",PositionType.RIGHT_TOP, positionGroup));
		positionPanel.add(getJRadioButton("右下",PositionType.RIGHT_BOTTOM, positionGroup));

		ld.setSelected(true);
		return positionPanel;
	}

	private JRadioButton getJRadioButton(String title,PositionType positionType,ButtonGroup bg) {
		JRadioButton jrb = new JRadioButton(title);
		jrb.setName(positionType.name());
		bg.add(jrb);
		jrb.setPreferredSize(new Dimension(CompSize.PDF_PATH_FIELD_WIDTH / 4, CompSize.BASE_FORM_PANEL_HEIGHT));
		return jrb;
	}

	private JPanel pagePanel() {
		JPanel pagePanel = getBasePanel();
		pagePanel.add(getBaseLabel("页数："));

		startField = new JTextField();
		startField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		startField.setText("1");
		startField.setHorizontalAlignment(SwingConstants.CENTER);
		pagePanel.add(startField);

		JLabel label1 = new JLabel("~");
		label1.setPreferredSize(new Dimension(10, CompSize.BASE_FORM_PANEL_HEIGHT));
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		pagePanel.add(label1);

		endField = new JTextField();
		endField.setHorizontalAlignment(SwingConstants.CENTER);
		endField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		pagePanel.add(endField);

		JLabel label2 = new JLabel("页     查询第");
		pagePanel.add(label2);

		indexField = new JTextField();
		indexField.setPreferredSize(new Dimension(CompSize.PAGE_FIELD_WIDTH, CompSize.BASE_FORM_PANEL_HEIGHT));
		indexField.setText("1");
		indexField.setHorizontalAlignment(SwingConstants.CENTER);
		indexField.setToolTipText("<html>0：全部 <br>n：第n个<br>-n：表示倒数第n个</html>");
		pagePanel.add(indexField);

		JLabel label3 = new JLabel("个关键字");
		pagePanel.add(label3);

		return pagePanel;
	}

	// 关键字选择框
	private JPanel keywordPanel() {
		JPanel keywordPanel = getBasePanel();
		keywordPanel.add(getBaseLabel("关键字："));

		keywordField = new JTextField();
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
				pdfPathStr = filechooserOpen.getSelectedFile().getAbsolutePath();
				handlePdfFile(pdfPathStr);
			}
		});

		filePanel.add(photoButton);
		filePanel.add(pdfPathField);

		return filePanel;
	}

	public void handlePdfFile(String pdfPath) {
		this.pdfPathStr = pdfPath;
		pdfPathField.setText(pdfPathStr);
		CURRENT_DIRECTORY = pdfPathStr;
		Map<Integer, BufferedImage> images = ImageUtil.getImages(pdfPathStr, 1, 0);
		this.comContext.getCenterPanel().paintImage(images);
		String endText = endField.getText();
		if(endText == null || "".equals(endText.trim())) {
			endField.setText(""+images.size());
		}
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

	@Override
	public ComContext getComContext() {
		return comContext;
	}

	public static String getPdfPathStr() {
		return pdfPathStr;
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
