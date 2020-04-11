package net.qiyuesuo.tool.gui;

import java.awt.Color;

public class CompSize {
	public static int MAIN_FRAME_WIDTH =1600;
	public static int MAIN_FRAME_HEIGHT =900;

	public static int IMAGE_WIDTH =1200;
	
	public static int CENTER_PANEL_WIDTH =1235;
	
	public static int EAST_PANEL_WIDTH =MAIN_FRAME_WIDTH-CENTER_PANEL_WIDTH;
	
	
	public static int BASE_FORM_PANEL_HEIGHT=30;
	public static int BASE_LABEL_WIDTH=70;
	
	public static int FILE_CHOORE_BUTTON_WIDTH = BASE_LABEL_WIDTH;
	public static int FILE_CHOORE_BUTTON_HEIGHT = BASE_FORM_PANEL_HEIGHT;
	
	public static int PDF_PATH_FIELD_WIDTH = EAST_PANEL_WIDTH - FILE_CHOORE_BUTTON_WIDTH-10;
	
	
	public static int PAGE_FIELD_WIDTH =35;
	
	
	public static int SEARCH_FORM_HEIGHT =350;
	
	
	public static int SEARCH_POSITION_HEIGHT =MAIN_FRAME_HEIGHT -SEARCH_FORM_HEIGHT-30;
	
	public static Color BASE_COLOR_RED_TRANPARENT = new Color(255, 0, 0,100);
	public static Color BASE_COLOR_GREEN_TRANPARENT = new Color(0, 255, 0,100);
	
	public static Color BASE_COLOR_DARK = new Color(238, 238, 238);
	
	public static Color BASE_COLOR_TABLE_HEAD = new Color(215, 215, 215);
	
	public static Color BASE_COLOR_TABLE_SELECTED = new Color(190, 190, 190);

}
