package net.qiyuesuo.test.itext5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.log.SysoLogger;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.fonts.cmaps.CMapCache;
import com.itextpdf.text.pdf.fonts.cmaps.CMapCidUni;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class FontTest {
	
	@Test
	public void testFont() throws Exception {
		PdfReader pdfReader = null;
		try {
			LoggerFactory instance = LoggerFactory.getInstance();
			instance.setLogger(new SysoLogger());
			int registerDirectories = FontFactory.registerDirectories();
			FontFactory.registerDirectory("C:\\Users\\gj\\Desktop\\AdobeFonts_downcc\\Adobe Fonts", true);
			System.out.println(registerDirectories);
			byte[] pdfBtyes = Files.readAllBytes(new File("C:\\Users\\gj\\Desktop\\contract_id_17834_1.pdf").toPath());
//			byte[] pdfBtyes = Files.readAllBytes(new File("C:\\Users\\gj\\Desktop\\小标宋字体上海市护理学会.pdf").toPath());
			pdfReader =new PdfReader(pdfBtyes);
			String textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, 1);
			System.out.println(textFromPage);
		} finally {
			if (pdfReader != null) {
				pdfReader.close();
			}
			pdfReader = null;
		}
	}
	
	public static void main(String[] args) throws IOException {
		CMapCidUni cachedCMapCidUni = CMapCache.getCachedCMapCidUni("GBK-EUC-H");
		
		PdfReader pdfReader = new PdfReader("C:\\Users\\gj\\Desktop\\wk\\pdf_test1.pdf");
		String textFromPage = PdfTextExtractor.getTextFromPage(pdfReader, 1);
		System.out.println(textFromPage);
		pdfReader.close();
	}

}
