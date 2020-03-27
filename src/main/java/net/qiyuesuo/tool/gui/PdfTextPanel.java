package net.qiyuesuo.tool.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import net.qiyuesuo.tool.position.KeywordPosition;
import net.qiyuesuo.tool.utils.PdfTextUtil;

public class PdfTextPanel extends JTextPane {

	private static final long serialVersionUID = 1L;
	private int pageNo;
	private DefaultHighlightPainter highlightPainter;
	private ComContext comContext;
	private DefaultHighlighter highlighter;

	public PdfTextPanel(ComContext comContext, int page) {
		this.comContext = comContext;
		this.pageNo = page;
		String text = PdfTextUtil.getText(page);
		this.setText(text);
		this.setFont(new Font(null, Font.PLAIN, 18));
		this.setEditable(false);
		highlighter = new DefaultHighlighter();
		highlightPainter = new DefaultHighlightPainter(new Color(0, 255, 0, 100));
		this.setHighlighter(highlighter);

		Map<String, ArrayList<KeywordPosition>> positionMap = comContext.getCenterPanel().getPositionMap();
		positionMap.forEach((keyword, v) -> {
			int wordlength = keyword.length();
			int index = 0;
			while ((index = text.indexOf(keyword, index)) != -1) {
				try {
					highlighter.addHighlight(index, index + wordlength, highlightPainter);
				} catch (BadLocationException e1) {
				}
				index += wordlength;
			}
		});
	}


	public int getPageNo() {
		return pageNo;
	}



	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}



	public ComContext getComContext() {
		return comContext;
	}

	public void setComContext(ComContext comContext) {
		this.comContext = comContext;
	}
	
}
