package net.qiyuesuo.tool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import net.qiyuesuo.tool.position.KeywordPosition;
import net.qiyuesuo.tool.utils.PositionUtil;

public class PositionPanel extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	private static ComContext comContext;

	private static Map<String, JTable> tableMap = new HashMap<>();

	public PositionPanel(ComContext comContext) {
		super();
		setComContext(comContext);
		this.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH - 5, CompSize.SEARCH_POSITION_HEIGHT));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}

	public void paintPosition() {
		Map<String, ArrayList<KeywordPosition>> positionMap = comContext.getCenterPanel().getPositionMap();
		Set<String> keywords = positionMap.keySet();
		this.removeAll();
		tableMap.clear();
		for (String keyword : keywords) {
			this.addTab(keyword, keywordPanel(keyword));
		}
	}

	static JPanel keywordPanel(String keyword) {
		JPanel keywordPanel = new JPanel();
		// 设置BorderLayout布局方式
		keywordPanel.setLayout(new BorderLayout());
		keywordPanel.setPreferredSize(new Dimension(CompSize.EAST_PANEL_WIDTH - 5, CompSize.SEARCH_POSITION_HEIGHT));
		// 创建表格
		JTable table = configTable(keyword);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // 水平滚动条不显示
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); // 垂直滚动条总是显示
		// 使用普通的中间容器添加表格时，表头 和 内容 需要分开添加
		keywordPanel.add(table.getTableHeader(), BorderLayout.NORTH);
		keywordPanel.add(scrollPane, BorderLayout.CENTER);
		return keywordPanel;
	}

	private static JTable configTable(String keyword) {
		// 创建 table
		JTable table = new JTable();
		tableMap.put(keyword, table);
		// 一次只能选择一项
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// 获取 model
		DefaultTableModel model = getTableModel(keyword);
		table.setModel(model);

		DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table1, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if (row % 2 == 0) {
					setBackground(CompSize.BASE_COLOR_DARK);
				} else {
					setBackground(new Color(255, 255, 255));
				}
				setHorizontalAlignment(JLabel.CENTER);// 表格内容居中
				((DefaultTableCellRenderer) table1.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(DefaultTableCellRenderer.CENTER);// 列头内容居中
//				table.getTableHeader().setFont(new Font("微软雅黑", Font.PLAIN, 13));
				table1.getTableHeader().setResizingAllowed(true);
				table1.setRowHeight(26);// 设置行高

				return super.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, column);
			}
		};

		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.setReorderingAllowed(false);// 设置表头不可移动
		Dimension size = table.getTableHeader().getPreferredSize();
		size.height = 32;// 设置新的表头高度32
		tableHeader.setPreferredSize(size);
		DefaultTableCellRenderer headRenderer = new DefaultTableCellRenderer();
		headRenderer.setHorizontalAlignment(JLabel.CENTER);
		headRenderer.setBackground(CompSize.BASE_COLOR_LIGHT);

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn col = table.getColumn(table.getColumnName(i));
			if (i == 0) {
				col.setPreferredWidth(40);
			}
			col.setCellRenderer(tableRenderer);
			col.setHeaderRenderer(headRenderer);
		}
		hideColumn(table,5);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow == -1) {
					return;
				}
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					String tableKey = (String) table.getValueAt(selectedRow, 5);
					CenterPanel centerPanel = comContext.getCenterPanel();
					Map<Integer, ImgPanel> imgPanels = centerPanel.getImgPanels();
					imgPanels.forEach((k,v)->{
						v.paintPosition(tableKey);
					});
					
					JScrollPane js = comContext.getCenterScrollPane();
					JScrollBar jsVB = js.getVerticalScrollBar();
					jsVB.setValue(centerPanel.getRealHeight(tableKey));
				}
			}
		});
		return table;
	}

	private static void hideColumn(JTable table, int index) {
		TableColumn tc = table.getColumnModel().getColumn(index);
		tc.setMaxWidth(0);
		tc.setPreferredWidth(0);
		tc.setWidth(0);
		tc.setMinWidth(0);

		table.getTableHeader().getColumnModel().getColumn(index).setMaxWidth(0);
		table.getTableHeader().getColumnModel().getColumn(index).setMinWidth(0);
	}

	private static DefaultTableModel getTableModel(String keyword) {
		Object[] columnNames = { "页数", "x占比", "y占比", "x长度", "y长度", "key" };
		Map<String, ArrayList<KeywordPosition>> positionMap = comContext.getCenterPanel().getPositionMap();
		ArrayList<KeywordPosition> positions = positionMap.get(keyword);
		int rowCount = positions.size();
		Object[][] rowData = new Object[rowCount][columnNames.length];
		// 向表格中填充数据
		for (int i = 0; i < rowCount; i++) {
			KeywordPosition position = positions.get(i);
			for (int j = 0; j < columnNames.length; j++) {
				switch (j) {
				case 0:
					rowData[i][j] = position.getPage();
					break;
				case 1:
					rowData[i][j] = String.format("%.4f", position.getX());
					break;
				case 2:
					rowData[i][j] = String.format("%.4f", position.getY());
					break;
				case 3:
					rowData[i][j] = String.format("%.2f", position.getCoordinateX());
					break;
				case 4:
					rowData[i][j] = String.format("%.2f", position.getCoordinateY());
					break;
				case 5:
					rowData[i][j] = PositionUtil.getKey(position);
					break;
				default:
					rowData[i][j] = null;
				}
			}
		}
		DefaultTableModel defaultTableModel = new DefaultTableModel(rowData, columnNames);
		return defaultTableModel;
	}

	public JTable getTable(String keyword) {
		return tableMap.get(keyword);
	}

	public static ComContext getComContext() {
		return comContext;
	}

	public static void setComContext(ComContext comContext) {
		PositionPanel.comContext = comContext;
	}
}
