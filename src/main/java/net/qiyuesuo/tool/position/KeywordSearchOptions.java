package net.qiyuesuo.tool.position;

import java.util.HashSet;
import java.util.Set;


public class KeywordSearchOptions {
	private String keyword;             //关键字
	private Set<String> keywords;       //多个关键字
	private int page = 0;               //搜索页数开始 0:表示全部,其他表示相应的页数
	private int pageEnd;                //搜索页数结束
	PositionType position = PositionType.RIGHT_BOTTOM; //位置信息
	private int keyIndex=0;             //第几个关键字 0:全部 -1:最后一个 其他:第keyIndex个  
	private boolean ignoreContentSpace = true; //忽略文档空格
	private boolean ignoreKeywordSpace = true; //忽略文档空格
	private boolean ignoreNewline = false; //是否忽略换行
	private boolean ignoreNewpage = false; //是否忽略换页
	
	public enum PositionType{
		LEFT_TOP,     //左上
		LEFT_CENTER,//左中
		LEFT_BOTTOM,  //左下
		
		RIGHT_TOP,    //右上
		RIGHT_CENTER,//右中
		RIGHT_BOTTOM,  //右下
		
		TOP_CENTER,//上中
		CENTER_CENTER,
		BOTTOM_CENTER;//下中
		
		boolean isLeft() {
			return this == LEFT_TOP || this == LEFT_BOTTOM || this == LEFT_CENTER;
		}
		
		boolean isTop() {
			return this == LEFT_TOP || this == RIGHT_TOP || this == TOP_CENTER;
		}
	}
	
	public void setFromHead(boolean fromHead) {
		if(fromHead) {
			this.position = PositionType.LEFT_BOTTOM;
		}else {
			this.position = PositionType.RIGHT_BOTTOM;
		}
	}
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public Set<String> getKeywords() {
		if(keywords == null) {
			keywords = new HashSet<>();
		}
		if(this.keyword!=null && !"".equals(this.keyword)) {
			keywords.add(keyword);
		}
		return keywords;
	}
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPageEnd() {
		return pageEnd;
	}
	public void setPageEnd(int pageEnd) {
		this.pageEnd = pageEnd;
	}
	public boolean isIgnoreContentSpace() {
		return ignoreContentSpace;
	}
	public void setIgnoreContentSpace(boolean ignoreContentSpace) {
		this.ignoreContentSpace = ignoreContentSpace;
	}
	public boolean isIgnoreKeywordSpace() {
		return ignoreKeywordSpace;
	}
	public void setIgnoreKeywordSpace(boolean ignoreKeywordSpace) {
		this.ignoreKeywordSpace = ignoreKeywordSpace;
	}
	public int getKeyIndex() {
		return keyIndex;
	}
	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}
	public boolean isIgnoreNewline() {
		return ignoreNewline;
	}
	public void setIgnoreNewline(boolean ignoreNewline) {
		this.ignoreNewline = ignoreNewline;
	}
	public boolean isIgnoreNewpage() {
		return ignoreNewpage;
	}
	public void setIgnoreNewpage(boolean ignoreNewpage) {
		this.ignoreNewpage = ignoreNewpage;
	}
	public PositionType getPosition() {
		return position;
	}
	public void setPosition(PositionType position) {
		this.position = position;
	}
	
}
