package com.gj.tool.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 关键字查询接口
 * @author jin.guo
 *
 */
public interface KeywordPositionProvider {
	
	final static double DEF_SAME_LINE = 0.004; // 判断是否为同一行范围值

	/**
	 * pdf查询关键字
	 * @param pdfBytes pdf文档字节数组
	 * @param keyword  所要查询的关键字 
	 * @param keyIndex 第几个关键字 0:全部 -1:最后一个 其他:第keyIndex个
	 * @return
	 */
	List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword, int keyIndex) throws KeywordSearchException;
	
	/**
	 * pdf查询关键字
	 * @param pdfBytes pdf文档字节数组
	 * @param keywords 所要查询的关键字
	 * @param keyIndex 第几个关键字 0:全部 -1:最后一个 其他:第keyIndex个
	 * @throws KeywordSearchException
	 */
	Map<String, ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes, List<String> keywords, int keyIndex) throws KeywordSearchException; 

	/**
	 * pdf查询关键字
	 * @param pdfBytes
	 * @param options 查询选项 （设置多个关键字，返回每个关键类的位置信息）
	 * @return
	 * @throws KeywordSearchException
	 */
	Map<String, ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes, KeywordSearchOptions options) throws KeywordSearchException;

}
