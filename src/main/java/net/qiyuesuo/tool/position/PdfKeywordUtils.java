package net.qiyuesuo.tool.position;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import net.qiyuesuo.tool.position.KeywordSearchOptions.PositionType;

public class PdfKeywordUtils {

	final static double DEF_SAME_LINE = 0.004;

	/**
	 * 
	 * @param pdfBytes pdf bytes数组
	 * @param keyword  要查询的关键字
	 * @return
	 * @throws KeywordSearchException
	 */
	public static List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword) throws KeywordSearchException {
		return queryKeyword(pdfBytes, keyword, 1, false);
	}

	/**
	 * 
	 * @param pdfBytes pdf bytes数组
	 * @param keyword  要查询的关键字
	 * @param keyIndex 第几个关键字 0:全部 -1:最后一个 其他:第keyIndex个
	 * @return
	 * @throws KeywordSearchException
	 */
	public static List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword, int keyIndex) throws KeywordSearchException {
		KeywordSearchOptions options = new KeywordSearchOptions();
		options.setKeyIndex(keyIndex);
		options.setKeyword(keyword);
		return queryKeyword(pdfBytes, options).get(keyword);
	}

	/**
	 * 
	 * @param pdfBytes pdf bytes数组
	 * @param keyword  要查询的关键字
	 * @param keyIndex 第几个关键字 0:全部 -1:最后一个 其他:第keyIndex个
	 * @param fromHead true: 表示返回关键字开始位置坐标 false: 关键字字结尾位置坐标
	 * @return
	 * @throws KeywordSearchException
	 */
	public static List<KeywordPosition> queryKeyword(byte[] pdfBytes, String keyword, int keyIndex, boolean fromHead) throws KeywordSearchException {
		KeywordSearchOptions options = new KeywordSearchOptions();
		options.setKeyIndex(keyIndex);
		options.setKeyword(keyword);
		if (fromHead) {
			options.setPosition(KeywordSearchOptions.PositionType.LEFT_BOTTOM);
		} else {
			options.setPosition(KeywordSearchOptions.PositionType.RIGHT_BOTTOM);
		}
		return queryKeyword(pdfBytes, options).get(keyword);
	}

	/**
	 * 
	 * @param pdfBytes pdf bytes数组
	 * @param options  查询选项 （设置多个关键字，返回每个关键类的位置信息）
	 * @return
	 * @throws KeywordSearchException
	 */
	public static Map<String, ArrayList<KeywordPosition>> queryKeyword(byte[] pdfBytes, KeywordSearchOptions options) throws KeywordSearchException {
		try {
			Map<String, ArrayList<ResultPosition>> positions = new ITextKeyPosition(pdfBytes, options).getPositions();
			Map<String, ArrayList<KeywordPosition>> keywords = new HashMap<>();
			positions.forEach((k, v) -> {
				List<KeywordPosition> collect = v.stream().map(p -> p.convert(options)).collect(Collectors.toList());
				keywords.put(k, (ArrayList<KeywordPosition>) collect);
			});
			return keywords;
		} catch (Exception e) {
			throw new KeywordSearchException(e);
		}
	}

	private static class ITextKeyPosition {
		private PdfReader pdfReader;
		private KeywordSearchOptions options;

		Map<String, char[]> keys = new HashMap<>();
		Map<String, ArrayList<ResultPosition>> results = new HashMap<>(); // 返回结果
		Map<String, Position> startPositions = new HashMap<>(); // 开始位置
		Map<String, ArrayList<Position>> globals = new HashMap<>();
		Map<String, Boolean> crossPages = new HashMap<>(); // 反向查询跨页问题

		public ITextKeyPosition(byte[] pdfBytes, KeywordSearchOptions options) throws IOException, KeywordSearchException {
			this.options = options;
			if (this.options == null) {
				throw new KeywordSearchException("KeywordSearchOptions不能为空");
			}
			this.pdfReader = new PdfReader(pdfBytes);
			Set<String> keywords = options.getKeywords();
			for (String keyword : keywords) {
				String newKeyword = new String(keyword);
				newKeyword = newKeyword.trim();
				if (options.isIgnoreKeywordSpace()) {
					newKeyword = keyword.replace(" ", "");
				}
				if ("".equals(newKeyword)) {
					throw new KeywordSearchException("关键字不能全为空格");
				}
				keys.put(keyword, newKeyword.toCharArray());
				results.put(keyword, new ArrayList<>());
				globals.put(keyword, new ArrayList<>());
			}
			if (keys.size() == 0) {
				throw new KeywordSearchException("没有要查询的关键字");
			}
		}

		public Map<String, ArrayList<ResultPosition>> getPositions() throws KeywordSearchException {
			try {
				// 得到查询范围
				int pageStart = 1;
				int pageEnd = pdfReader.getNumberOfPages();
				if (options.getPage() != 0 && options.getPage() <= pageEnd) {
					pageStart = options.getPage();
					if (options.getPage() <= options.getPageEnd()) {
						pageEnd = pageEnd > options.getPageEnd() ? options.getPageEnd() : pageEnd;
					}
				}

				PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
				if (options.getKeyIndex() >= 0) { // 查询全部 或 第n个关键字
					for (int i = pageStart; i <= pageEnd; i++) {
						if (canStop(null)) {
							break;
						}
						findPositions(parser, i);
					}
				} else {
					for (int i = pageEnd; i >= pageStart; i--) {
						if (canStop(null)) {
							break;
						}
						findPositions(parser, i);
						// 处理跨页问题
						if (i != pageEnd && !this.options.isIgnoreNewpage()) {
							Set<String> globalsKeys = globals.keySet();
							for (String globalsKey : globalsKeys) {
								if (globals.get(globalsKey).size() > 0) {
									crossPages.put(globalsKey, true);
									int tempPage = i + 1;
									findPositions(parser, tempPage);
									crossPages.clear();
								}
							}
						}
					}
				}
			} catch (IOException e) {
				throw new KeywordSearchException(e);
			} finally {
				if (pdfReader != null) {
					pdfReader.close();
				}
			}

			return getResults();
		}

		private void findPositions(PdfReaderContentParser parser, int currentPage) throws IOException {
			// 得到文档宽高
			Rectangle pageSize = pdfReader.getPageSizeWithRotation(currentPage);
			double width = pageSize.getWidth();
			double height = pageSize.getHeight();
			int rotation = pdfReader.getPageRotation(currentPage);

			parser.processContent(currentPage, new TextMarginFinder() {
				@Override
				public void renderText(TextRenderInfo renderInfo) {
					List<Position> positions = getkeyPositions(renderInfo, currentPage, width, height, rotation);
					for (String keyword : globals.keySet()) {
						if (canStop(keyword)) {
							continue;
						}
						ArrayList<Position> globalPositions = globals.get(keyword);
						globalPositions.addAll(positions);
						findPosition(globalPositions, keyword, currentPage);
					}
				}
			});
		}

		private void findPosition(ArrayList<Position> globalPositions, String keyword, int currentPage) {
			int count = 0;
			while (globalPositions.size() > 0) {
				count++;
				if (count > 100000) {
					throw new RuntimeException("关键字查询失败");
				}
				Position position = globalPositions.get(0);
				// 从后向前查询跨页处理
				if (crossPages.containsKey(keyword) && crossPages.get(keyword).booleanValue() && position.getPage() == currentPage) {
					globalPositions.clear();
					break;
				}

				boolean stopLoop = false;
				char[] key = keys.get(keyword);
				if (position.getText() == key[0]) {// 匹配到第一个
					startPositions.put(keyword, position);
					if (key.length == 1) {
						ArrayList<ResultPosition> result = results.get(keyword);
						Position startPosition = startPositions.get(keyword);
						result.add(new ResultPosition(startPosition, startPosition));
						startPositions.put(keyword, null);
						globalPositions.remove(0);
						break;
						/*ArrayList<KeywordPosition> result = results.get(keyword);
						Position startPosition = startPositions.get(keyword).calculate(this.options);
						Position endPosition = startPosition;
						if (options.getPosition().isLeft()) {
							result.add(startPosition);
						} else {
							result.add(endPosition);
						}
						startPositions.put(keyword, null);
						globalPositions.remove(0);
						break;*/
					}
					for (int i = 1; i < key.length; i++) {
						if (globalPositions.size() <= i) {
							stopLoop = true;
							break;
						}

						// 忽略空格
						if (globalPositions.get(i).getText() == ' ' && options.isIgnoreContentSpace()) {
							if (!options.isIgnoreKeywordSpace() && key[i] == ' ') { // 关键字不省略空格
								continue;
							}
							globalPositions.remove(i);
							if (globalPositions.size() == 0) {
								stopLoop = true;
								break;
							} else {
								i--;
								continue;
							}
						}

						if (globalPositions.get(i).getText() == key[i]) {
							if (i == key.length - 1) { // 查询到
								ArrayList<ResultPosition> result = results.get(keyword);
								ResultPosition resultPosition = new ResultPosition(startPositions.get(keyword), globalPositions.get(i));
								if (options.isIgnoreNewline()) {// 忽略换行关键字
									if (!resultPosition.isNewline()) {
										result.add(resultPosition);
									}
								} else {
									result.add(resultPosition);
								}
								startPositions.put(keyword, null);
								globalPositions.remove(0);
								break;
								/*Position startPosition = startPositions.get(keyword).calculate(this.options);
								Position endPosition = globalPositions.get(i).calculate(this.options);
								if (options.isIgnoreNewline()) {// 忽略换行关键字
									if (Math.abs(startPosition.getY() - endPosition.getY()) <= DEF_SAME_LINE) {
										if (options.getPosition().isLeft()) {
											result.add(startPosition);
										} else {
											result.add(endPosition);
										}
									}
								} else {
									if (options.getPosition().isLeft()) {
										result.add(startPosition);
									} else {
										result.add(endPosition);
									}
								}
								startPositions.put(keyword, null);
								globalPositions.remove(0);
								break;*/
							}
						} else {
							globalPositions.remove(0);
							startPositions.put(keyword, null);
							break;
						}
					}
				} else {
					globalPositions.remove(0);
				}
				if (stopLoop) {
					break;
				}
			}
		}

		// 判断是否能结束查询
		private boolean canStop(String keyword) {
			int keyIndex = options.getKeyIndex();
			if (keyIndex == 0) {
				return false;
			}
			if (keyIndex < 0 && keyword != null) {
				return false;
			}

			boolean stop = true;
			if (keyword == null) {
				Set<String> wkeys = results.keySet();
				for (String key : wkeys) {
					if (Math.abs(keyIndex) > results.get(key).size()) {
						stop = false;
						break;
					}
				}
			} else {
				if (keyIndex > results.get(keyword).size()) {
					stop = false;
				}
			}
			return stop;
		}

		private Map<String, ArrayList<ResultPosition>> getResults() {
			int keyIndex = options.getKeyIndex();
			if (keyIndex == 0) {
				return results;
			}
			Set<String> wkeys = results.keySet();
			for (String key : wkeys) {
				ArrayList<ResultPosition> positions = results.get(key);
				if (Math.abs(keyIndex) > positions.size()) {
					positions.clear();
				} else {
					ResultPosition position = null;
					if (keyIndex < 0) {
						position = positions.get(positions.size()+keyIndex);
					}else {
						position = positions.get(Math.abs(keyIndex) - 1);
					}
					positions.clear();
					positions.add(position);
				}
			}
			return results;
		}
	}

	private static List<Position> getkeyPositions(TextRenderInfo textRenderInfo, int page, double width, double height, int rotation) {
		String textStr = textRenderInfo.getText();
		char[] chars = textStr.toCharArray();
		List<Position> positions = new ArrayList<>();
		for (int i = 0; i < chars.length; i++) {
			positions.add(new Position(textRenderInfo, chars[i], i, page, width, height, rotation));
		}
		return positions;
	}

	static class ResultPosition {
		private Position startPosition;
		private Position endPosition;

		public ResultPosition(Position startPosition, Position endPosition) {
			super();
			this.startPosition = startPosition;
			this.endPosition = endPosition;
		}

		/**
		 * 检验是否换行
		 * 
		 * @return
		 */
		public boolean isNewline() {
			if (isNewpage()) {
				return true;
			}
			int rotation = startPosition.getRotation();
			double startY = startPosition.getTextRenderInfo().getDescentLine().getBoundingRectange().getY();
			double endY = endPosition.getTextRenderInfo().getAscentLine().getBoundingRectange().getY();
			
			double startX = startPosition.getTextRenderInfo().getDescentLine().getBoundingRectange().getX();
			double endX = endPosition.getTextRenderInfo().getAscentLine().getBoundingRectange().getX();
			switch (rotation) {
			case 90:
				if (startX < endX) {
					return true;
				}
				break;
			case 180:
				if (startY < endY) {
					return true;
				}
				break;
			case 270:
				if (startX > endX) {
					return true;
				}
				break;
			default:
				if (startY > endY) {
					return true;
				}
				break;
			}

			return false;
		}

		/**
		 * 是否换页
		 * 
		 * @return
		 */
		public boolean isNewpage() {
			if (this.getEndPosition().getPage() != this.getStartPosition().getPage()) {
				return true;
			}
			return false;
		}

		public KeywordPosition convert(KeywordSearchOptions options) {
			PositionType positionType = options.getPosition();
			Position position = calc(positionType);
			KeywordPosition keywordPosition = new KeywordPosition(position,positionType);
			Set<PositionType> otherPosition = options.getOtherPosition();
			if(otherPosition != null) {
				otherPosition.forEach(o ->{
					keywordPosition.putOtherPosition(o, new KeywordPosition(calc(o),o));
				});
			}
			return keywordPosition;
		}
		
		private Position calc(PositionType positionType) {
			Position position = null;
			switch (positionType) {
			case LEFT_TOP:
			case LEFT_BOTTOM:
				position = calcApex(positionType, startPosition);
				break;
			case RIGHT_TOP:
			case RIGHT_BOTTOM:
				position = calcApex(positionType, endPosition);
				break;

			case LEFT_CENTER:
				Position leftTop = calcApex(PositionType.LEFT_TOP, startPosition);
				Position leftBottom = calcApex(PositionType.LEFT_BOTTOM, startPosition);
				position = calcCenter(new Position(startPosition), leftTop, leftBottom,false);
				break;

			case RIGHT_CENTER:
				Position rightTop = calcApex(PositionType.RIGHT_TOP, endPosition);
				Position rightBottom = calcApex(PositionType.RIGHT_BOTTOM, endPosition);
				position = calcCenter(new Position(endPosition), rightTop, rightBottom,false);
				break;

			case TOP_CENTER:
				Position leftT = calcApex(PositionType.LEFT_TOP, startPosition);
				Position rightT = calcApex(PositionType.RIGHT_TOP, endPosition);
				position = calcCenter(new Position(endPosition), leftT, rightT,true);
				break;

			case BOTTOM_CENTER:
				Position leftB = calcApex(PositionType.LEFT_BOTTOM, startPosition);
				Position rightB = calcApex(PositionType.RIGHT_BOTTOM, endPosition);
				position = calcCenter(new Position(endPosition), leftB, rightB,true);
				break;

			case CENTER_CENTER:
				Position rightBo = calcApex(PositionType.RIGHT_BOTTOM, endPosition);
				if (isNewline() || isNewpage()) {
					Position rightTo = calcApex(PositionType.RIGHT_TOP, endPosition);
					position = calcCenter(new Position(endPosition), rightTo, rightBo,false);
				} else {
					Position leftTo = calcApex(PositionType.LEFT_TOP, startPosition);
					position = calcCenter(new Position(endPosition), leftTo, rightBo,false);
				}
				break;
			default:
				break;
			}
			return position;
		}

		public Position calcCenter(Position position, Position aP, Position bP,boolean check) {
			if (check && (isNewline() || isNewpage())) {
				return bP;
			}
			position.setCoordinateX((aP.getCoordinateX() + bP.getCoordinateX()) / 2);
			position.setCoordinateY((aP.getCoordinateY() + bP.getCoordinateY()) / 2);
			position.setX((aP.getX() + bP.getX()) / 2);
			position.setY((aP.getY() + bP.getY()) / 2);
			return position;

		}

		private Position calcApex(PositionType positionType, Position basePosition) {
			Position position = null;
			switch (positionType) {
			case LEFT_TOP:
				position = new Position(startPosition);
				calcApex(position, position.getTextRenderInfo().getAscentLine().getBoundingRectange(), true);
				break;
			case LEFT_BOTTOM:
				position = new Position(startPosition);
				calcApex(position, position.getTextRenderInfo().getDescentLine().getBoundingRectange(), true);
				break;
			case RIGHT_TOP:
				position = new Position(endPosition);
				calcApex(position, position.getTextRenderInfo().getAscentLine().getBoundingRectange(), false);
				break;
			case RIGHT_BOTTOM:
				position = new Position(endPosition);
				calcApex(position, position.getTextRenderInfo().getDescentLine().getBoundingRectange(), false);
				break;
			default:
				break;
			}
			return position;
		}

		private void calcApex(Position position, Float f, boolean isLeft) {
			double coordinateX = 0;
			switch (position.getRotation()) {
			case 90:
				position.setY(1 - f.x / position.getHeight());
				position.setCoordinateY(position.getHeight() - f.x);
				coordinateX = availableWidth(position, isLeft);
				position.setCoordinateX(coordinateX);
				position.setX(coordinateX / position.getWidth());

				break;
			case 180:
				position.setY(1 - f.y / position.getHeight());
				position.setCoordinateY(position.getHeight() - f.y);
				coordinateX = availableWidth(position, isLeft);
				position.setCoordinateX(position.getWidth() - coordinateX);
				position.setX(1 - coordinateX / position.getWidth());
				break;
			case 270:
				position.setY(f.x / position.getHeight());
				position.setCoordinateY(f.x);
				coordinateX = availableWidth(position, isLeft);
				position.setCoordinateX(position.getWidth() - coordinateX);
				position.setX(1 - coordinateX / position.getWidth());
				break;
			default:
				position.setY(f.y / position.getHeight());
				position.setCoordinateY(f.y);
				coordinateX = availableWidth(position, isLeft);
				position.setCoordinateX(coordinateX);
				position.setX(coordinateX / position.getWidth());
				break;
			}
		}

		private double availableWidth(Position position, boolean fromHead) {
			TextRenderInfo textRenderInfo = position.getTextRenderInfo();
			int index = position.getIndex();
			int rotation = position.getRotation();

			String textStr = textRenderInfo.getText();
			Float boundingRectange = textRenderInfo.getBaseline().getBoundingRectange();

			double textCount = textStr.length();
			double contentCount = index;
			if (!fromHead) {
				contentCount = index + 1;
			}
			double rectangeWidth = 0;
			double calcWidth = 0;
			switch (rotation) {
			case 90:
				rectangeWidth = boundingRectange.getHeight();
				calcWidth = contentCount / textCount * rectangeWidth;
				calcWidth = boundingRectange.y + calcWidth;
				break;
			case 180:
				rectangeWidth = boundingRectange.getWidth();
				calcWidth = contentCount / textCount * rectangeWidth;
				calcWidth = boundingRectange.x - calcWidth + rectangeWidth;
				break;
			case 270:
				rectangeWidth = boundingRectange.getHeight();
				calcWidth = contentCount / textCount * rectangeWidth;
				calcWidth = boundingRectange.y - calcWidth + rectangeWidth;
				break;
			default:
				rectangeWidth = boundingRectange.getWidth();
				calcWidth = contentCount / textCount * rectangeWidth;
				calcWidth = calcWidth + boundingRectange.x;
				break;
			}
			return calcWidth;
		}

		public Position getStartPosition() {
			return startPosition;
		}

		public void setStartPosition(Position startPosition) {
			this.startPosition = startPosition;
		}

		public Position getEndPosition() {
			return endPosition;
		}

		public void setEndPosition(Position endPosition) {
			this.endPosition = endPosition;
		}
	}

	static class Position extends KeywordPosition {
		private TextRenderInfo textRenderInfo;
		private int index;
		private char text;
		private double height = 841.92;
		private double width = 595.32;
		private int rotation = 0;

		public Position(TextRenderInfo textRenderInfo, char text, int index, int page, double width, double height, int rotation) {
			this.textRenderInfo = textRenderInfo;
			this.text = text;
			this.index = index;
			this.width = width;
			this.height = height;
			this.rotation = rotation;
			this.setPage(page);
		}

		public Position(Position position) {
			this.textRenderInfo = position.getTextRenderInfo();
			this.text = position.getText();
			this.index = position.getIndex();
			this.width = position.getWidth();
			this.height = position.getHeight();
			this.rotation = position.getRotation();
			this.setPage(position.getPage());
		}

		public char getText() {
			return text;
		}

		public TextRenderInfo getTextRenderInfo() {
			return textRenderInfo;
		}

		public void setTextRenderInfo(TextRenderInfo textRenderInfo) {
			this.textRenderInfo = textRenderInfo;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public double getHeight() {
			return height;
		}

		@Override
		public void setHeight(double height) {
			this.height = height;
		}

		@Override
		public double getWidth() {
			return width;
		}

		@Override
		public void setWidth(double width) {
			this.width = width;
		}

		public int getRotation() {
			return rotation;
		}

		public void setRotation(int rotation) {
			this.rotation = rotation;
		}

		public void setText(char text) {
			this.text = text;
		}
	}
}
