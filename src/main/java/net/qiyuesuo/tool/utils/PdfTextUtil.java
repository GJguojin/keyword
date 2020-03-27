package net.qiyuesuo.tool.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class PdfTextUtil {

	private static Map<Integer, StringBuffer> textMap = new HashMap<>();

	private static PdfReader pdfReader;
	
	private static TextRenderInfo lastRender;

	public static void clear() {
		textMap.clear();
		lastRender = null;
		pdfReader = null;
	}

	public static String getText(Integer page) {
		StringBuffer buffer = textMap.get(page);
		if (buffer == null) {
			return "";
		}
		return buffer.toString().replaceAll("(?m)^\\s*$(\\n|\\r\\n)", "");
	}

	public static void readPdf(byte[] pdfBtyes, int startPage, int endPage) {
		for (int i = Math.min(startPage, endPage); i <= Math.max(startPage, endPage); i++) {
			if (textMap.containsKey(i)) {
				continue;
			}
			try {
				pdfReader = getPdfReader(pdfBtyes);
				PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
				lastRender = null;
				parser.processContent(i, new PdfTextMarginFinder(i));
				lastRender = null;
			} catch (Exception e) {
			} finally {
				if (pdfReader != null) {
					pdfReader.close();
				}
				pdfReader = null;
			}
		}
	}
	
	static class PdfTextMarginFinder extends TextMarginFinder{
		
		private int page;
		
		public PdfTextMarginFinder(int page) {
			super();
			this.page = page;
		}
		@Override
		public void renderText(TextRenderInfo renderInfo) {
			StringBuffer buffer = textMap.get(page);
			if (buffer == null) {
				buffer = new StringBuffer();
				textMap.put(page, buffer);
			}
			String text = renderInfo.getText();
			if(text == null || "".equals(text)) {
				return;
			}
			if(lastRender != null && lastRender.getDescentLine().getBoundingRectange().getY() > renderInfo.getAscentLine().getBoundingRectange().getY()) {
				buffer.append("\n");
			}
			buffer.append(text);
			lastRender = renderInfo;
		}
	}

	private static PdfReader getPdfReader(byte[] pdfBtyes) throws IOException {
		if (pdfReader != null) {
			return pdfReader;
		}
		return new PdfReader(pdfBtyes);
	}
}
