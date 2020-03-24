package net.qiyuesuo.tool.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


public class ImageUtil {
	
	public static Map<Integer,BufferedImage> getImages(String pafPath,int startPage,int endPage){
		Map<Integer,BufferedImage> map = new TreeMap<Integer, BufferedImage>();
		PDDocument document = null;
		try {
			document = PDDocument.load(Files.readAllBytes(new File(pafPath).toPath()));
			int numberOfPages = document.getNumberOfPages();
			endPage = endPage ==0?numberOfPages: endPage > numberOfPages?numberOfPages:endPage;
			startPage = startPage ==0?1:startPage > numberOfPages?1:startPage;
			PDFRenderer renderer = new PDFRenderer(document);
			for(int i =Math.min(startPage, endPage);i<=Math.max(startPage, endPage);i++) {
				BufferedImage image = renderer.renderImageWithDPI(i-1, 144);
				map.put(i, image);
			}
		} catch (Exception e) {
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
				}
			}
		}
		return map;
	}

}
