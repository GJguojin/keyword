package net.qiyuesuo.tool.position;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import net.qiyuesuo.tool.position.KeywordSearchOptions.PositionType;



public class PdfboxKeywordPositionProvider implements KeywordPositionProvider {

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
	public List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword, int keyIndex)   throws KeywordSearchException{
		KeywordSearchOptions options = new KeywordSearchOptions();
		options.setKeyIndex(keyIndex);
		options.setKeyword(keyword);
		return queryKeyword(pdfBytes,options).get(keyword);
	}
	
	@Override
	public Map<String,ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes,KeywordSearchOptions options ) throws KeywordSearchException{
		Map<String,ArrayList<KeywordPosition>> result = null;
		try {
			result =  new PdfboxKeyPosition(pdfBytes,options).getPositions();
		}  catch (Exception e) {
			throw new KeywordSearchException(e);
		}
		return result;
	}
	
	class PdfboxKeyPosition extends PDFTextStripper {

		private Map<String,char[]> keys = new HashMap<>();
		
		private byte[] pdfBytes;
		private KeywordSearchOptions options;
		
		
		private Map<String,ArrayList<KeywordPosition>> results = new HashMap<>();//查询结果
		
		private double height = 841.92;
		private double width = 595.32;
		private int currentPage; //当前页
		private Map<String,Position> startPositions = new HashMap<>(); //开始位置
		private Map<String,ArrayList<Position>> globals = new HashMap<>();
		private Map<String,Boolean> crossPages = new HashMap<>(); //反向查询跨页问题
		private final static double DEF_LINE_FIX = 0.002; //行高校验值
		
		public PdfboxKeyPosition(byte[] pdfBytes, KeywordSearchOptions options) throws IOException, KeywordSearchException{
			this.options = options;
			if(this.options == null) {
				throw new KeywordSearchException("KeywordSearchOptions不能为空");
			}
			super.setSortByPosition(true);
			this.pdfBytes = pdfBytes;
			Set<String> keywords = options.getKeywords();
			for(String keyword : keywords) {
				String newKeyword = new String(keyword);
				newKeyword = newKeyword.trim();
				if(options.isIgnoreKeywordSpace()) {
					newKeyword = keyword.replace(" ", "");
				}
				if("".equals(newKeyword)) {
					throw new KeywordSearchException("关键字不能全为空格");
				}
				keys.put(keyword, newKeyword.toCharArray());
				results.put(keyword, new ArrayList<>());
				globals.put(keyword, new ArrayList<>());
			}
			if(keys.size() == 0) {
				throw new KeywordSearchException("没有要查询的关键字");
			}
		}
		
		public List<KeywordPosition> getPosition() throws KeywordSearchException {
			Map<String, ArrayList<KeywordPosition>> positions = getPositions();
			String keyword = options.getKeyword();
			if(positions.containsKey(keyword)) {
				return positions.get(keyword);
			}else {
				return new ArrayList<>();
			}
			
		}
		
		public Map<String,ArrayList<KeywordPosition>> getPositions() throws KeywordSearchException {
			if(keys.size() == 0) {
				return results;
			}
			try {
				document = PDDocument.load(pdfBytes);
				
				//得到查询范围
				int pageStart = 1;
				int pageEnd = document.getNumberOfPages();
				if(options.getPage() != 0 && options.getPage() <= pageEnd) {
					pageStart = options.getPage();
					if(options.getPage() <= options.getPageEnd()) {
						pageEnd = pageEnd > options.getPageEnd()?options.getPageEnd():pageEnd;
					}
				}
				
				if(options.getKeyIndex() >=0) { //查询全部 或 第n个关键字
					for(int i=pageStart;i<=pageEnd;i++) {
						currentPage = i;
						handlePageSize(i-1);
						super.setSortByPosition(true);
						super.setStartPage(i);
						super.setEndPage(i);
						if(canStop(null)) {
							break;
						}
						Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
						super.writeText(document, dummy);
						handleNewpage();
					}
				}else { //查询倒数第n个关键字
					for(int i=pageEnd;i>=pageStart;i--) {
						currentPage = i;
						handlePageSize(i-1);
						super.setSortByPosition(true);
						super.setStartPage(i);
						super.setEndPage(i);
						if(canStop(null)) {
							break;
						}
						Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
						super.writeText(document, dummy);
						handleNewpage();
						
						//处理跨页问题
						if(i != pageEnd && !this.options.isIgnoreNewpage()) {
							Set<String> globalsKeys = globals.keySet();
							for(String globalsKey : globalsKeys) {
								if(globals.get(globalsKey).size() > 0) {
									crossPages.put(globalsKey, true);
									int tempPage = i+1;
									currentPage = tempPage;
									super.setSortByPosition(true);
									super.setStartPage(tempPage);
									super.setEndPage(tempPage);
									Writer tempDummy = new OutputStreamWriter(new ByteArrayOutputStream());
									super.writeText(document, tempDummy);
									crossPages.clear();
								}
							}
						}
					}
					
				}
				return getResults();
			} catch (Exception e) {
				throw new KeywordSearchException(e);
			}finally {
				if (document != null) {
					try {
						document.close();
					} catch (IOException e) {
						throw new KeywordSearchException(e);
					}
				}
			}
		}
		
		@Override
		protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
//			System.out.println("=="+String.format("%03d", this.currentPage)+"=="+String.format("%03d",textPositions.size())+"===="+string);
			List<Position> positions = this.getkeyPositions(textPositions);
			Set<String> keywords = globals.keySet();
			for(String keyword :keywords) {
				if(canStop(keyword)) {
					continue;
				}
				ArrayList<Position> globalPositions = globals.get(keyword);
				globalPositions.addAll(positions);
				findPosition(globalPositions,keyword);
			}
		}
		
		private  Map<String,ArrayList<KeywordPosition>> getResults(){
			int keyIndex = options.getKeyIndex();
			if(keyIndex == 0) {
				return results;
			}
			Set<String> wkeys = results.keySet();
			for(String key : wkeys) {
				ArrayList<KeywordPosition> positions = results.get(key);
				if(Math.abs(keyIndex) > positions.size()) {
					positions.clear();
				}else {
					if(keyIndex < 0) {
						//先排序
						Collections.sort(positions, new Comparator<KeywordPosition>() {
							@Override
							public int compare(KeywordPosition p1, KeywordPosition p2) {
								int result = p2.getPage()-p1.getPage();
								if(result == 0) {
									if(Math.abs(p1.getY()-p2.getY()) <= DEF_SAME_LINE) {
										result = Math.abs(p1.getX()-p2.getX()) <= DEF_SAME_LINE?0: p1.getX()-p2.getX()>0?-1:1;
									}else {
										result = p1.getY()-p2.getY()>0?1:-1;
									}
								}
								return result;
							}
						});
					}
					KeywordPosition position = positions.get(Math.abs(keyIndex)-1);
					positions.clear();
					positions.add(position);
				}
			}
			return results;
		}
		
		//判断是否能结束查询
		private boolean canStop(String keyword) {
			int keyIndex = options.getKeyIndex();
			if(keyIndex == 0) {
				return false;
			}
			if(keyIndex < 0 && keyword != null) {
				return false;
			}
			
			boolean stop = true;
			if(keyword == null) {
				Set<String> wkeys = results.keySet();
				for(String key : wkeys) {
					if(Math.abs(keyIndex) > results.get(key).size()) {
						stop = false;
						break;
					}
				}
			}else {
				if(keyIndex > results.get(keyword).size()) {
					stop = false;
				}
			}
			return stop;
		}
		
		private void findPosition(ArrayList<Position> globalPositions,String keyword) {
			int count =0;
			while(globalPositions.size() > 0) {
				count++;
				if(count > 100000) {
					throw new RuntimeException("关键字查询失败");
				}
				Position position = globalPositions.get(0);
				//从后向前查询跨页处理
				if(crossPages.containsKey(keyword) && crossPages.get(keyword).booleanValue() && position.getPage() == this.currentPage) {
					globalPositions.clear();
					break;
				}
				
//				System.out.println("|" + position.getText() + "|");
				boolean stopLoop = false;
				char[] key = keys.get(keyword);
				if(position.getText() == key[0]) {//匹配到第一个
					startPositions.put(keyword, position);
					if(key.length == 1) {
						ArrayList<KeywordPosition> result = results.get(keyword);
						Position startPosition = startPositions.get(keyword);
						Position endPosition = startPosition;
						if (options.getPosition()==PositionType.LEFT_BOTTOM) {
							result.add(startPosition);
						} else if(options.getPosition()==PositionType.RIGHT_BOTTOM){
							result.add(endPosition);
						}
						startPositions.put(keyword, null);
						globalPositions.remove(0);
						break;
					}
					for(int i=1;i<key.length;i++) {
						if(globalPositions.size() <= i ) {
							stopLoop = true;
							break;
						}
						
						//忽略空格
						if(globalPositions.get(i).getText()==' ' &&  options.isIgnoreContentSpace()) {
							if(!options.isIgnoreKeywordSpace() && key[i] == ' ') { //关键字不省略空格
								continue;
							}
							globalPositions.remove(0);
							if(globalPositions.size() == 0) {
								stopLoop = true;
								break;
							}else {
								i--;
								continue;
							}
						}
						
						
						if(globalPositions.get(i).getText() == key[i] ) {
							if(i == key.length-1) { //查询到
								ArrayList<KeywordPosition> result = results.get(keyword);
								
								Position startPosition = startPositions.get(keyword);
								Position endPosition = globalPositions.get(i);
								if(options.isIgnoreNewline()) {//忽略换行关键字
									if(Math.abs(startPosition.getY()-endPosition.getY()) <= DEF_SAME_LINE) {
										if (options.getPosition()==PositionType.LEFT_BOTTOM){
											result.add(startPosition);
										}else if(options.getPosition()==PositionType.RIGHT_BOTTOM){
											result.add(endPosition);
										}
									}
								}else {
									if(options.getPosition()==PositionType.LEFT_BOTTOM) {
										result.add(startPosition);
									}else if(options.getPosition()==PositionType.RIGHT_BOTTOM) {
										result.add(endPosition);
									}
								}
								startPositions.put(keyword, null);
								globalPositions.remove(0);
								break;
							}
						}else {
							globalPositions.remove(0);
							startPositions.put(keyword, null);
							break;
						}
					}
				}else {
					globalPositions.remove(0);
				}
				if(stopLoop) {
					break;
				}
			}
		}
		
		/**
		 * 位置转化
		 * @param textPositions
		 * @return
		 */
		private List<Position> getkeyPositions(List<TextPosition> textPositions){
			List<Position> positions = new ArrayList<>();
			if(textPositions == null) {
				return positions;
			}
			for(TextPosition textPosition : textPositions) {
				Position position = new Position();
				position.setText(textPosition.getUnicode().charAt(0));
				position.setPage(this.currentPage);
				if(options.getPosition()==PositionType.LEFT_BOTTOM) {
					position.setCoordinateX(textPosition.getX());
					position.setX(textPosition.getX()/width);
				}else if(options.getPosition()==PositionType.RIGHT_BOTTOM){
					position.setCoordinateX((textPosition.getX()+textPosition.getWidth()));
					position.setX((textPosition.getX()+textPosition.getWidth())/width);
				}
				position.setCoordinateY(height-textPosition.getY()-height*DEF_LINE_FIX);
				position.setY(1 - textPosition.getY()/height-DEF_LINE_FIX);
				positions.add(position);
				
			}
			return positions;
		}
		
		private void handleNewpage() {
			if(this.options.isIgnoreNewpage()) {
				for(String keyword:globals.keySet()) {
					globals.get(keyword).clear();
				}
			}
		}
		
		/**
		 * 设置文档长宽
		 * @param pageNum
		 */
		private void handlePageSize(int pageNum) {
			PDPage page = document.getPage(pageNum);
			PDRectangle cropbBox  = page.getCropBox();
			if(page.getRotation() == 90 || page.getRotation() == 270) {
				width = cropbBox.getHeight();
				height = cropbBox.getWidth();
			}else {
				 width = cropbBox.getWidth();
				 height = cropbBox.getHeight();
			}
		}
		
	}
	
	class Position extends KeywordPosition{
		private char text;
		public char getText() {
			return text;
		}
		public void setText(char text) {
			this.text = text;
		}
		
	}
}
