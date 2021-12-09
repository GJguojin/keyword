package com.gj.tool.position;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gj.tool.position.KeywordSearchOptions.PositionType;
import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

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
			// 兼容returnAll字段
			if (options.isReturnAll()) {
				options.setKeyIndex(0);
			}
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
						if (canStop()) {
							break;
						}
						try {
							findPositions(parser, i);
						} catch (Exception e) {
							if (!e.getMessage().contains("query keyword stop")) {
								throw e;
							}
						}
					}
				} else {
					for (int i = pageEnd; i >= pageStart; i--) {
						if (canStop()) {
							break;
						}
						try {
							findPositions(parser, i);
						} catch (Exception e) {
							if (!e.getMessage().contains("query keyword stop")) {
								throw e;
							}
						}
						// 处理跨页问题
						if (i != pageEnd && !this.options.isIgnoreNewpage()) {
							Set<String> globalsKeys = globals.keySet();
							for (String globalsKey : globalsKeys) {
								if (globals.get(globalsKey).size() > 0) {
									crossPages.put(globalsKey, true);
									int tempPage = i + 1;
									try {
										findPositions(parser, tempPage);
									} catch (Exception e) {
										if (!e.getMessage().contains("query keyword stop")) {
											throw e;
										}
									}
									crossPages.clear();
								}
							}
						}
						// 重新排序
						Set<String> keySet = results.keySet();
						for (String key : keySet) {
							ArrayList<ResultPosition> result = results.get(key);
							ArrayList<ResultPosition> tmp = new ArrayList<>();
							for (int j = result.size() - 1; j >= 0; j--) {
								ResultPosition resultPosition = result.get(j);
								if (resultPosition.getEndPosition().getPage() == i) {
									tmp.add(resultPosition);
									result.remove(j);
								}
							}
							if (tmp.size() > 0) {
								for (ResultPosition resultPosition : tmp) {
									result.add(resultPosition);
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
//					System.out.println("==" + String.format("%03d", currentPage) + "==" + String.format("%03d", renderInfo.getText().length()) + "====" + renderInfo.getText());
//					List<TextRenderInfo> characterRenderInfos = renderInfo.getCharacterRenderInfos();
//					for(TextRenderInfo tmep : characterRenderInfos) {
//						System.out.println("=====>  【"+tmep.getText()+"】  "+tmep.getBaseline().getBoundingRectange().getX());
//					}

					List<Position> positions = getkeyPositions(renderInfo, currentPage, width, height, rotation);
					for (String keyword : globals.keySet()) {
						if (canStop() && options.getKeyIndex() > 0) {
							throw new RuntimeException("query keyword stop");
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
								int keySplitIndex = options.getKeySplitIndex();
								ResultPosition resultPosition = null;
								int endIndex = i;
								if (keySplitIndex > 0 && keySplitIndex <= i + 1) {
									endIndex = keySplitIndex - 1;
								}
								resultPosition = new ResultPosition(startPositions.get(keyword), globalPositions.get(endIndex));
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
		private boolean canStop() {
			int keyIndex = options.getKeyIndex();
			if (keyIndex == 0) {
				return false;
			}
			Set<String> wkeys = results.keySet();
			for (String key : wkeys) {
				if (Math.abs(keyIndex) > results.get(key).size()) {
					return false;
				}
			}
			return true;
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
						position = positions.get(Math.abs(keyIndex) - 1);
					} else {
						position = positions.get(keyIndex - 1);
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
			TextRenderInfo startTextRenderInfo = getCharacterRenderInfo(startPosition.getTextRenderInfo(), startPosition.getIndex());
			if (startTextRenderInfo != null) {
				startTextRenderInfo.getText();
				this.startPosition.setCharacterRenderInfo(startTextRenderInfo);
			}
			this.endPosition = endPosition;
			TextRenderInfo endTextRenderInfo = getCharacterRenderInfo(endPosition.getTextRenderInfo(), endPosition.getIndex());
			if (endTextRenderInfo != null) {
				endTextRenderInfo.getText();
				this.endPosition.setCharacterRenderInfo(endTextRenderInfo);
			}
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
			KeywordPosition keywordPosition = new KeywordPosition(position, positionType);
			Set<PositionType> otherPosition = options.getOtherPosition();
			if (otherPosition != null) {
				otherPosition.forEach(o -> {
					keywordPosition.putOtherPosition(o, new KeywordPosition(calc(o), o));
				});
			}
			return keywordPosition;
		}

		private Position calc(PositionType positionType) {
			Position position = null;
			switch (positionType) {
			case LEFT_TOP:
			case LEFT_BOTTOM:
				position = calcApex(positionType);
				break;
			case RIGHT_TOP:
			case RIGHT_BOTTOM:
				position = calcApex(positionType);
				break;

			case LEFT_CENTER:
				Position leftTop = calcApex(PositionType.LEFT_TOP);
				Position leftBottom = calcApex(PositionType.LEFT_BOTTOM);
				position = calcCenter(new Position(startPosition), leftTop, leftBottom, false);
				break;

			case RIGHT_CENTER:
				Position rightTop = calcApex(PositionType.RIGHT_TOP);
				Position rightBottom = calcApex(PositionType.RIGHT_BOTTOM);
				position = calcCenter(new Position(endPosition), rightTop, rightBottom, false);
				break;

			case TOP_CENTER:
				Position leftT = calcApex(PositionType.LEFT_TOP);
				Position rightT = calcApex(PositionType.RIGHT_TOP);
				position = calcCenter(new Position(endPosition), leftT, rightT, true);
				break;

			case BOTTOM_CENTER:
				Position leftB = calcApex(PositionType.LEFT_BOTTOM);
				Position rightB = calcApex(PositionType.RIGHT_BOTTOM);
				position = calcCenter(new Position(endPosition), leftB, rightB, true);
				break;

			case CENTER_CENTER:
				Position rightBo = calcApex(PositionType.RIGHT_BOTTOM);
				if (isNewline() || isNewpage()) {
					Position rightTo = calcApex(PositionType.RIGHT_TOP);
					position = calcCenter(new Position(endPosition), rightTo, rightBo, false);
				} else {
					Position leftTo = calcApex(PositionType.LEFT_TOP);
					position = calcCenter(new Position(endPosition), leftTo, rightBo, false);
				}
				break;
			default:
				break;
			}
			return position;
		}

		public Position calcCenter(Position position, Position aP, Position bP, boolean check) {
			if (check && (isNewline() || isNewpage())) {
				return bP;
			}
			position.setCoordinateX((aP.getCoordinateX() + bP.getCoordinateX()) / 2);
			position.setCoordinateY((aP.getCoordinateY() + bP.getCoordinateY()) / 2);
			position.setX((aP.getX() + bP.getX()) / 2);
			position.setY((aP.getY() + bP.getY()) / 2);
			return position;

		}

		private Position calcApex(PositionType positionType) {
			Position position = null;
			switch (positionType) {
			case LEFT_TOP:
				position = new Position(startPosition);
				calcApex(position, position.handleAscentLine(), true);
				break;
			case LEFT_BOTTOM:
				position = new Position(startPosition);
				calcApex(position, position.handleDescentLine(), true);
				break;
			case RIGHT_TOP:
				position = new Position(endPosition);
				calcApex(position, position.handleAscentLine(), false);
				break;
			case RIGHT_BOTTOM:
				position = new Position(endPosition);
				calcApex(position, position.handleDescentLine(), false);
				break;
			default:
				break;
			}
			return position;
		}

		private void calcApex(Position position, LineSegment lineSegment, boolean isLeft) {
			Float f = lineSegment.getBoundingRectange();
			double coordinateX = 0;
			switch (position.getRotation()) {
			case 90:
				position.setCoordinateY(position.getHeight() - f.x);
				position.setY(1 - f.x / position.getHeight());
				coordinateX = calcWidth(position, isLeft);
				position.setCoordinateX(coordinateX);
				position.setX(coordinateX / position.getWidth());
				break;
			case 180:
				position.setCoordinateY(position.getHeight() - f.y);
				position.setY(1 - f.y / position.getHeight());
				coordinateX = calcWidth(position, isLeft);
				position.setCoordinateX(position.getWidth() - coordinateX);
				position.setX(1 - coordinateX / position.getWidth());
				break;
			case 270:
				position.setCoordinateY(f.x);
				position.setY(f.x / position.getHeight());
				coordinateX = calcWidth(position, isLeft);
				position.setCoordinateX(position.getWidth() - coordinateX);
				position.setX(1 - coordinateX / position.getWidth());
				break;
			default:
				position.setCoordinateY(f.y);
				position.setY(f.y / position.getHeight());
				position.setCoordinateX(calcWidth(position, isLeft));
				position.setX(position.getCoordinateX() / position.getWidth());
				break;
			}
		}

		private double calcWidth(Position position, boolean fromHead) {
			TextRenderInfo characterRenderInfo = position.getCharacterRenderInfo();
			if (characterRenderInfo == null || characterRenderInfo.getText() == null || "".equals(characterRenderInfo.getText())) {
				return availableWidth(position, fromHead);
			}
			Float boundingRectange = characterRenderInfo.getBaseline().getBoundingRectange();
			int rotation = position.getRotation();
			double calcWidth = 0;
			switch (rotation) {
			case 90:
				calcWidth = boundingRectange.y;
				if (!fromHead) {
					calcWidth = calcWidth + boundingRectange.getHeight();
				}
				break;
			case 180:
				calcWidth = boundingRectange.x + boundingRectange.getWidth();
				if (!fromHead) {
					calcWidth = calcWidth - boundingRectange.getWidth();
				}
				break;
			case 270:
				calcWidth = boundingRectange.y + boundingRectange.getHeight();
				if (!fromHead) {
					calcWidth = calcWidth - boundingRectange.getHeight();
				}
				break;
			default:
				calcWidth = boundingRectange.x;
				if (!fromHead) {
					calcWidth = calcWidth + boundingRectange.getWidth();
				}
				break;
			}
			return calcWidth;
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
		private TextRenderInfo characterRenderInfo;
		private int index;
		private char text;
		private int rotation = 0;

		public Position(TextRenderInfo textRenderInfo, char text, int index, int page, double width, double height, int rotation) {
			this.textRenderInfo = textRenderInfo;
			this.text = text;
			this.index = index;
			this.rotation = rotation;
			this.setPage(page);
			this.setWidth(width);
			this.setHeight(height);
		}

		public Position(Position position) {
			this.textRenderInfo = position.getTextRenderInfo();
			this.characterRenderInfo = position.getCharacterRenderInfo();
			this.text = position.getText();
			this.index = position.getIndex();
			this.rotation = position.getRotation();
			this.setPage(position.getPage());
			this.setWidth(position.getWidth());
			this.setHeight(position.getHeight());
		}

		public LineSegment handleAscentLine() {
			return handleLineSegment("ascent");
		}

		public LineSegment handleDescentLine() {
			return handleLineSegment("descent");
		}

		private double calcCentLine() {
			double baseY = characterRenderInfo.getBaseline().getBoundingRectange().getMaxY();
			double asY = characterRenderInfo.getAscentLine().getBoundingRectange().getMaxY();
			double desY = characterRenderInfo.getDescentLine().getBoundingRectange().getMaxY();
			return Math.min(Math.abs(baseY - asY), Math.abs(baseY - desY));
		}

		private LineSegment handleLineSegment(String type) {
			LineSegment result = null;
			try {
				float singleSpaceWidth = characterRenderInfo.getSingleSpaceWidth();
				if (singleSpaceWidth > 50 || calcCentLine() > 15) {// 这里判断存在问题需优化
					TextRenderInfo info = characterRenderInfo.getCharacterRenderInfos().get(0);
					Field nameField = TextRenderInfo.class.getDeclaredField("textToUserSpaceTransformMatrix");
					nameField.setAccessible(true);
					Matrix matrix = (Matrix) nameField.get(info);
					float i11 = matrix.get(Matrix.I11);
					float i22 = matrix.get(Matrix.I22);
					if (i11 != 1.0f || i22 != 1.0f) {
						Matrix newMatrix = new Matrix(1.0f, matrix.get(Matrix.I12), matrix.get(Matrix.I21), 1.0f, matrix.get(Matrix.I31),
								matrix.get(Matrix.I32));
						nameField.set(info, newMatrix);
						if ("ascent".equals(type)) {
							result = info.getAscentLine();
						} else {
							result = info.getDescentLine();
						}
					}
				}
			} catch (Exception e) {
			}

			if (result == null) {
				if ("ascent".equals(type)) {
					result = characterRenderInfo.getAscentLine();
				} else {
					result = characterRenderInfo.getDescentLine();
				}
			}
			return result;
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

		public int getRotation() {
			return rotation;
		}

		public void setRotation(int rotation) {
			this.rotation = rotation;
		}

		public void setText(char text) {
			this.text = text;
		}

		public TextRenderInfo getCharacterRenderInfo() {
			return characterRenderInfo;
		}

		public void setCharacterRenderInfo(TextRenderInfo characterRenderInfo) {
			this.characterRenderInfo = characterRenderInfo;
		}
	}

	private static TextRenderInfo getCharacterRenderInfo(TextRenderInfo textRenderInfo, int index) {
		List<TextRenderInfo> characterRenderInfos = textRenderInfo.getCharacterRenderInfos();
		String text = textRenderInfo.getText();
		if (text.length() != characterRenderInfos.size()) {
			char baseChar = text.charAt(index);
			int count = 0;
			for (int i = 0; i < index; i++) {
				if (baseChar == text.charAt(i)) {
					count++;
				}
			}
			int infoCount = 0;
			TextRenderInfo resultInfo = null;
			for (TextRenderInfo info : characterRenderInfos) {
				if (calcInfo(info.getText(), baseChar, count, infoCount)) {
					resultInfo = info;
					break;
				}
			}
			if (resultInfo == null) {
				infoCount = 0;
				for (TextRenderInfo info : characterRenderInfos) {
					if (calcInfo(info.getPdfString().toString(), baseChar, count, infoCount)) {
						resultInfo = info;
						break;
					}
				}
			}
			return resultInfo;
		} else {
			return characterRenderInfos.get(index);
		}
	}

	private static boolean calcInfo(String infoText, char baseChar, int count, int infoCount) {
		if (infoText == null || infoText.length() == 0) {
			return false;
		}
		for (char charInfo : infoText.toCharArray()) {
			if (charInfo == baseChar) {
				if (count == infoCount) {
					return true;
				} else {
					infoCount++;
				}
			}
		}
		return false;
	}
}
