package com.gj.tool.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

public class ImageUtil {

	public static BufferedImage getImage(byte[] pdfBtyes, int pageNo) {
		pageNo = pageNo - 1;
		PDDocument document = null;
		try {
			document = PDDocument.load(pdfBtyes);
			PDFRenderer renderer = new PDFRenderer(document);
			PDPage page = document.getPage(pageNo);
			PDRectangle cropbBox = page.getCropBox();
			float width = cropbBox.getWidth();
			float height = cropbBox.getHeight();
			float scale = ImageScaleUtil.scale(width, height);
			return renderer.renderImage(pageNo, scale);
		} catch (Exception e) {
			return null;
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static Map<Integer, BufferedImage> getImages(byte[] pdfBtyes, int startPage, int endPage) {
		Map<Integer, BufferedImage> map = new TreeMap<Integer, BufferedImage>();
		PDDocument document = null;
		try {
			document = PDDocument.load(pdfBtyes);
			int numberOfPages = document.getNumberOfPages();
			endPage = endPage == 0 ? numberOfPages : endPage > numberOfPages ? numberOfPages : endPage;
			startPage = startPage == 0 ? 1 : startPage > numberOfPages ? 1 : startPage;
			PDFRenderer renderer = new PDFRenderer(document);

			for (int i = 1; i <= numberOfPages; i++) {
				if (i >= Math.min(startPage, endPage) && i <= Math.max(startPage, endPage)) {
					BufferedImage image = renderer.renderImageWithDPI(i - 1, 144);
					map.put(i, image);
				} else {
					map.put(i, null);
				}
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
