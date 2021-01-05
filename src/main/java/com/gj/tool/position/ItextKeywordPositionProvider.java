package com.gj.tool.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;



public class ItextKeywordPositionProvider implements KeywordPositionProvider {

	@Override
	public Map<String, ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes, List<String> keywords, int keyIndex) throws KeywordSearchException {
		KeywordSearchOptions options = new KeywordSearchOptions();
		options.setKeyIndex(keyIndex);
		keywords = keywords.stream().filter(s->s!=null && !"".equals(s)).collect(Collectors.toList());
		Set<String> keywordsSet = new TreeSet<>(keywords);
		options.setKeywords(keywordsSet);
		return queryKeyword(pdfBytes,options);
	}
	
	@Override
	public List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword, int keyIndex) throws KeywordSearchException {
		KeywordSearchOptions options = new KeywordSearchOptions();
		options.setKeyIndex(keyIndex);
		options.setKeyword(keyword);
		return queryKeyword(pdfBytes,options).get(keyword);
	}

	@Override
	public Map<String, ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes, KeywordSearchOptions options) throws KeywordSearchException {
		Map<String,ArrayList<KeywordPosition>> result = null;
		try {
			result = PdfKeywordUtils.queryKeyword(pdfBytes, options);
		}  catch (Exception e) {
			throw new KeywordSearchException(e);
		}
		return result;
	}
}
