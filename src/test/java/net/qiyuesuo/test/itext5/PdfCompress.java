package net.qiyuesuo.test.itext5;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import net.coobird.thumbnailator.Thumbnails;

public class PdfCompress {

	public static float FACTOR =0.5f;

	public static void compress(String src, String dest) throws Exception {
		PdfName key = new PdfName("ITXT_SpecialId");
		PdfName value = new PdfName("123456789");
		// 读取pdf文件
		PdfReader reader = new PdfReader(src);
		int n = reader.getXrefSize();
		PdfObject object;
		PRStream stream;
		// Look for image and manipulate image stream
		for (int i = 0; i < n; i++) {

			object = reader.getPdfObject(i);
			if (object == null || !object.isStream()) {
				continue;
			}
			stream = (PRStream) object;
			PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
			if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
				try {
					PdfNumber sizeNum = stream.getAsNumber(new PdfName("Length"));
					PdfNumber widthNum = stream.getAsNumber(new PdfName("Width"));
					PdfNumber heightNum = stream.getAsNumber(new PdfName("Height"));
					// 判断文件流的大小，超过500k的才进行压缩，否则不进行压缩
					if (sizeNum.intValue() >  20000 && widthNum.intValue() > 1000) {
						int width = (int) (widthNum.intValue() * FACTOR);
						int height = (int) (heightNum.intValue() * FACTOR);
						
						
						ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
						PdfImageObject image = new PdfImageObject(stream);
						Thumbnails.of(image.getBufferedImage()).width(width).height(height).outputFormat(image.getFileType()).toOutputStream(imgBytes);
						byte[] byteArray = imgBytes.toByteArray();
						System.out.println("compress "+i+" oldSize "+sizeNum.intValue()+" newSize "+byteArray.length+" width "+widthNum.intValue()+" height "+heightNum.intValue());
						stream.clear();
						stream.setData(byteArray, false, PRStream.BEST_COMPRESSION);
						stream.put(PdfName.TYPE, PdfName.XOBJECT);
						stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
						stream.put(key, value);
						stream.put(PdfName.FILTER, PdfName.DCTDECODE);
						stream.put(PdfName.WIDTH, new PdfNumber(width));
						stream.put(PdfName.HEIGHT, new PdfNumber(height));
						stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
						stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// Save altered PDF
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		stamper.close();
		reader.close();
	}
	
	public static void main(String[] args) throws Exception {
		compress("C:\\Users\\gj\\Desktop\\测试用文档\\项目编号：YZZCG-2019109投标文件.pdf","C:\\Users\\gj\\Desktop\\pdf597899273668644575.pdf");
	}

}
