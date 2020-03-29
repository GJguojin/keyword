package net.qiyuesuo.tool.utils;

import net.qiyuesuo.tool.position.KeywordPosition;

public class PositionUtil {
	
	public static String getKey(KeywordPosition position) {
		if(position == null) {
			return null;
		}
		return ""+position.getPage()+"#"+ String.format("%.4f", position.getX())+"#"+String.format("%.4f", position.getY());
	}

}
