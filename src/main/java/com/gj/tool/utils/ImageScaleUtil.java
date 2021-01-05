package com.gj.tool.utils;

public class ImageScaleUtil {
	
	private static final float DEF_SCALE = 2.0f;
	private static final float DEF_BASE_WIDTH =1000.0f;
	private static final float DEF_BASE_MAX_WIDTH =2000.0f;
	
	private static final float DEF_WIDTH =1500.0f;
	private static final float[] DEF_SCALES = {2.0f,1.0f,0.75f,0.5f,0.25f,0.125f};
	
	public static float scale(float width,float height) {
		if(width == 0 || height == 0) {
			return DEF_SCALE;
		}
		width = Math.min(Math.abs(width), Math.abs(height));
		if(width <= DEF_BASE_WIDTH ){
			return DEF_SCALE;
		}
		
		for(int i=1;i<DEF_SCALES.length;i++) {
			if(width*DEF_SCALES[i] > DEF_BASE_WIDTH && width*DEF_SCALES[i] <= DEF_BASE_MAX_WIDTH) {
				return DEF_SCALES[i];
			}
		}
		return DEF_WIDTH/width;
	}

}
