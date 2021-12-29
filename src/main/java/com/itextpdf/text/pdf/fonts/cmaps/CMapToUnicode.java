/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package com.itextpdf.text.pdf.fonts.cmaps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfString;

/**
 * This class represents a CMap file.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @since 2.1.4
 */
public class CMapToUnicode extends AbstractCMap {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CMapToUnicode.class);

	private Map<Integer, String> singleByteMappings = new HashMap<Integer, String>();
	private Map<Integer, String> doubleByteMappings = new HashMap<Integer, String>();

	/**
	 * Creates a new instance of CMap.
	 */
	public CMapToUnicode() {
		// default constructor
	}

	/**
	 * This will tell if this cmap has any one byte mappings.
	 *
	 * @return true If there are any one byte mappings, false otherwise.
	 */
	public boolean hasOneByteMappings() {
		return !singleByteMappings.isEmpty();
	}

	/**
	 * This will tell if this cmap has any two byte mappings.
	 *
	 * @return true If there are any two byte mappings, false otherwise.
	 */
	public boolean hasTwoByteMappings() {
		return !doubleByteMappings.isEmpty();
	}

	/**
	 * This will perform a lookup into the map.
	 *
	 * @param code   The code used to lookup.
	 * @param offset The offset into the byte array.
	 * @param length The length of the data we are getting.
	 *
	 * @return The string that matches the lookup.
	 */
	public String lookup(byte[] code, int offset, int length) {

		String result = null;
		Integer key = null;
		if (length == 1) {

			key = Integer.valueOf(code[offset] & 0xff);
			result = singleByteMappings.get(key);
		} else if (length == 2) {
			int intKey = code[offset] & 0xff;
			intKey <<= 8;
			intKey += code[offset + 1] & 0xff;
			key = Integer.valueOf(intKey);

			result = doubleByteMappings.get(key);
		}

		return result;
	}

	public Map<Integer, Integer> createReverseMapping() throws IOException {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Map.Entry<Integer, String> entry : singleByteMappings.entrySet()) {
			result.put(convertToInt(entry.getValue()), entry.getKey());
		}
		for (Map.Entry<Integer, String> entry : doubleByteMappings.entrySet()) {
			result.put(convertToInt(entry.getValue()), entry.getKey());
		}
		return result;
	}

	public Map<Integer, Integer> createDirectMapping() throws IOException {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Map.Entry<Integer, String> entry : singleByteMappings.entrySet()) {
			result.put(entry.getKey(), convertToInt(entry.getValue()));
		}
		for (Map.Entry<Integer, String> entry : doubleByteMappings.entrySet()) {
			result.put(entry.getKey(), convertToInt(entry.getValue()));
		}
		return result;
	}

	private int convertToInt(String s) throws IOException {
		byte[] b = s.getBytes("UTF-16BE");
		int value = 0;
		for (int i = 0; i < b.length - 1; i++) {
			value += b[i] & 0xff;
			value <<= 8;
		}
		value += b[b.length - 1] & 0xff;
		return value;
	}

	void addChar(int cid, String uni) {
		doubleByteMappings.put(Integer.valueOf(cid), uni);
	}

	@Override
	void addChar(PdfString mark, PdfObject code) {
		try {
			byte[] src = mark.getBytes();
			String dest = createStringFromBytes(code.getBytes());
			if (src.length == 1) {
				singleByteMappings.put(Integer.valueOf(src[0] & 0xff), dest);
			} else if (src.length == 2) {
				int intSrc = src[0] & 0xFF;
				intSrc <<= 8;
				intSrc |= src[1] & 0xFF;
				doubleByteMappings.put(Integer.valueOf(intSrc), dest);
			} else {
				throw new IOException(MessageLocalization.getComposedMessage("mapping.code.should.be.1.or.two.bytes.and.not.1", src.length));
			}
		} catch (Exception ex) {
			throw new ExceptionConverter(ex);
		}
	}

	private String createStringFromBytes(byte[] bytes) throws IOException {
		String retval = null;
		if (bytes.length == 1) {
			retval = new String(bytes);
		} else {
			retval = new String(bytes, "UTF-16BE");
		}
		return retval;
	}

	public static CMapToUnicode getIdentity() {
		CMapToUnicode uni = new CMapToUnicode();
		for (int i = 0; i < 65537; i++) {
			uni.addChar(i, Utilities.convertFromUtf32(i));
		}
		return uni;
	}

	@Override
	void addRange(PdfString from, PdfString to, PdfObject code) {
		byte[] a1 = decodeStringToByte(from);
		byte[] a2 = decodeStringToByte(to);
		if (a1.length != a2.length || a1.length == 0) {
			throw new IllegalArgumentException("Invalid map.");
		}
		byte[] sout = null;
		if (code instanceof PdfString) {
			sout = decodeStringToByte((PdfString) code);
		}
		
		int start = byteArrayToInt(a1);
		int end = byteArrayToInt(a2);
		
		//解决mpdf生成pdf 字体ToUnicode字典 1 beginbfrange<0000> <ffff> <0000> endbfrange导致itext读取乱码问题
		if (code instanceof PdfString) {
			int val = byteArrayToInt(decodeStringToByte((PdfString) code));
			if (start == 0 && val == 0 && end == 65535) {
				LOGGER.warn("ToUnicode error use Utilities.convertFromUtf32 form "+start+" to "+end);
				for (int k = start; k <= end; ++k) {
					addChar(k, Utilities.convertFromUtf32(k));
				}
				return;
			}
		}

		for (int k = start; k <= end; ++k) {
			intToByteArray(k, a1);
			PdfString s = new PdfString(a1);
			s.setHexWriting(true);
			if (code instanceof PdfArray) {
				addChar(s, ((PdfArray) code).getPdfObject(k - start));
			} else if (code instanceof PdfNumber) {
				int nn = ((PdfNumber) code).intValue() + k - start;
				addChar(s, new PdfNumber(nn));
			} else if (code instanceof PdfString) {
				PdfString s1 = new PdfString(sout);
				s1.setHexWriting(true);
				++sout[sout.length - 1];
				addChar(s, s1);
			}
		}
	}

	private static int byteArrayToInt(byte[] b) {
		int v = 0;
		for (int k = 0; k < b.length; ++k) {
			v = v << 8;
			v |= b[k] & 0xff;
		}
		return v;
	}

	private static void intToByteArray(int v, byte[] b) {
		for (int k = b.length - 1; k >= 0; --k) {
			b[k] = (byte) v;
			v = v >>> 8;
		}
	}
}
