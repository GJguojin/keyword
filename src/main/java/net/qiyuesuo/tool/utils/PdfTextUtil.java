package net.qiyuesuo.tool.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PdfTextUtil {

	private static Map<Integer, StringBuffer> textMap = new HashMap<>();

	private static PdfReader pdfReader;
	

	public static void clear() {
		textMap.clear();
		if(pdfReader != null) {
			pdfReader.close();
		}
		pdfReader = null;
	}

	public static String getText(Integer page) {
		StringBuffer buffer = textMap.get(page);
		if (buffer == null) {
			return "";
		}
//		return buffer.toString().replaceAll("(?m)^\\s*$(\\n|\\r\\n)", "");
		return buffer.toString();
	}

	public static void readPdf(byte[] pdfBtyes, int startPage, int endPage) {
		for (int i = Math.min(startPage, endPage); i <= Math.max(startPage, endPage); i++) {
			if (textMap.containsKey(i)) {
//				continue;
			}
			try {
				pdfReader = getPdfReader(pdfBtyes);
				String textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, i);
				textMap.put(i, new StringBuffer(textFromPage));
			} catch (Exception e) {
			} finally {
				if (pdfReader != null) {
					pdfReader.close();
				}
				pdfReader = null;
			}
		}
	}
	
	private static PdfReader getPdfReader(byte[] pdfBtyes) throws IOException {
		if (pdfReader != null) {
			return pdfReader;
		}
		return new PdfReader(pdfBtyes);
	}
}
