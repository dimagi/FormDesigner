package org.purc.purcforms.client.util;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;


/**
 * 
 * @author daniel
 *
 */
public class StyleUtil {

	/** The default background colour for groupbox header label. */
	public static final String COLOR_GROUP_HEADER = "#8FABC7";
	
	public static void setFontStyleIndex(String fontSyle, ListBox listBox){
		if("italic".equalsIgnoreCase(fontSyle))
			listBox.setSelectedIndex(1);
		else if("oblique".equalsIgnoreCase(fontSyle))
			listBox.setSelectedIndex(2);
		else if("".equalsIgnoreCase(fontSyle) || fontSyle == null )
			listBox.setSelectedIndex(3);
		else
			listBox.setSelectedIndex(0);
	}
	
	public static void setTextDecorationIndex(String textDecoration, ListBox listBox){
		if("underline".equalsIgnoreCase(textDecoration))
			listBox.setSelectedIndex(1);
		else if("overline".equalsIgnoreCase(textDecoration))
			listBox.setSelectedIndex(2);
		else if("line-through".equalsIgnoreCase(textDecoration))
			listBox.setSelectedIndex(3);
		else if("blink".equalsIgnoreCase(textDecoration))
			listBox.setSelectedIndex(4);
		else if("".equalsIgnoreCase(textDecoration) || textDecoration == null )
			listBox.setSelectedIndex(5);
		else
			listBox.setSelectedIndex(0);
	}
	
	public static void setTextAlignIndex(String textAlign, ListBox listBox){
		if("right".equalsIgnoreCase(textAlign))
			listBox.setSelectedIndex(1);
		else if("center".equalsIgnoreCase(textAlign))
			listBox.setSelectedIndex(2);
		else if("justify".equalsIgnoreCase(textAlign))
			listBox.setSelectedIndex(3);
		else
			listBox.setSelectedIndex(0); //left
	}
	
	public static void setBorderStyleIndex(String borderStyle, ListBox listBox){
		if("hidden".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(1);
		else if("dotted".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(2);
		else if("dashed".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(3);
		else if("solid".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(4);
		else if("double".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(5);
		else if("groove".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(6);
		else if("ridge".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(7);
		else if("inset".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(8);
		else if("outset".equalsIgnoreCase(borderStyle))
			listBox.setSelectedIndex(9);
		else if("".equalsIgnoreCase(borderStyle) || borderStyle == null )
			listBox.setSelectedIndex(10);
		else
			listBox.setSelectedIndex(0);
	}
	
	public static void setFontWeightIndex(String fontWeight, ListBox listBox){
		if("bold".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(1);
		else if("bolder".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(2);
		else if("lighter".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(3);
		else if("100".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(4);
		else if("200".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(5);
		else if("300".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(6);
		else if("400".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(7);
		else if("500".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(8);
		else if("600".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(9);
		else if("700".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(10);
		else if("800".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(11);
		else if("900".equalsIgnoreCase(fontWeight))
			listBox.setSelectedIndex(12);
		else if("".equalsIgnoreCase(fontWeight) || fontWeight == null )
			listBox.setSelectedIndex(13);
		else
			listBox.setSelectedIndex(0);
	}
	
	public static void loadFontStyles(ListBox listBox){
		listBox.addItem("normal");
		listBox.addItem("italic");
		listBox.addItem("oblique");
		listBox.addItem("");
	}
	
	public static void loadTextDecoration(ListBox listBox){
		listBox.addItem("none");
		listBox.addItem("underline");
		listBox.addItem("overline");
		listBox.addItem("line-through");
		listBox.addItem("blink");
		listBox.addItem("");
	}
	
	public static void loadTextAlign(ListBox listBox){
		listBox.addItem("left");
		listBox.addItem("right");
		listBox.addItem("center");
		listBox.addItem("justify");
		listBox.addItem("");
	}
	
	public static void loadBorderStyles(ListBox listBox){
		listBox.addItem("none");
		listBox.addItem("hidden");
		listBox.addItem("dotted");
		listBox.addItem("dashed");
		listBox.addItem("solid");
		listBox.addItem("double");
		listBox.addItem("groove");
		listBox.addItem("ridge");
		listBox.addItem("inset");
		listBox.addItem("outset");
		listBox.addItem("");
	}
	
	public static void loadFontWeights(ListBox listBox){
		listBox.addItem("normal");
		listBox.addItem("bold");
		listBox.addItem("bolder");
		listBox.addItem("lighter");
		listBox.addItem("100");
		listBox.addItem("200");
		listBox.addItem("300");
		listBox.addItem("400");
		listBox.addItem("500");
		listBox.addItem("600");
		listBox.addItem("700");
		listBox.addItem("800");
		listBox.addItem("900");
		listBox.addItem("");
	}
	
	public static void loadColorNames(MultiWordSuggestOracle oracle){
		oracle.add("AliceBlue");  
		oracle.add("AntiqueWhite");
		oracle.add("Aqua"); 
		oracle.add("Aquamarine"); 
		oracle.add("Azure"); 
		oracle.add("Beige");  
		oracle.add("Bisque"); 
		oracle.add("Black");
		oracle.add("BlanchedAlmond"); 
		oracle.add("Blue"); 
		oracle.add("BlueViolet");  
		oracle.add("Brown");  
		oracle.add("BurlyWood"); 
		oracle.add("CadetBlue");  
		oracle.add("Chartreuse");  
		oracle.add("Chocolate"); 
		oracle.add("Coral"); 
		oracle.add("CornflowerBlue");   
		oracle.add("Cornsilk"); 
		oracle.add("Crimson");   
		oracle.add("Cyan"); 
		oracle.add("DarkBlue");
		oracle.add("DarkCyan");  
		oracle.add("DarkGoldenRod");  
		oracle.add("DarkGray");  
		oracle.add("DarkGreen");  
		oracle.add("DarkKhaki"); 
		oracle.add("DarkMagenta");  
		oracle.add("DarkOliveGreen");  
		oracle.add("Darkorange");  
		oracle.add("DarkOrchid");  
		oracle.add("DarkRed"); 
		oracle.add("DarkSalmon");   
		oracle.add("DarkSeaGreen"); 
		oracle.add("DarkSlateBlue"); 
		oracle.add("DarkSlateGray");  
		oracle.add("DarkTurquoise");  
		oracle.add("DarkViolet");   
		oracle.add("DeepPink"); 
		oracle.add("DeepSkyBlue"); 
		oracle.add("DimGray"); 
		oracle.add("DodgerBlue");  
		oracle.add("FireBrick");  
		oracle.add("FloralWhite");  
		oracle.add("ForestGreen"); 
		oracle.add("Fuchsia"); 
		oracle.add("Gainsboro"); 
		oracle.add("GhostWhite");  
		oracle.add("Gold"); 
		oracle.add("GoldenRod");  
		oracle.add("Gray");  
		oracle.add("Green");   
		oracle.add("GreenYellow");  
		oracle.add("HoneyDew");   
		oracle.add("HotPink");  
		oracle.add("IndianRed");  
		oracle.add("Indigo ");  
		oracle.add("Ivory");  
		oracle.add("Khaki");  
		oracle.add("Lavender");   
		oracle.add("LavenderBlush");   
		oracle.add("LawnGreen"); 
		oracle.add("LemonChiffon");  
		oracle.add("LightBlue");   
		oracle.add("LightCoral");  
		oracle.add("LightCyan");  
		oracle.add("LightGoldenRodYellow");   
		oracle.add("LightGrey");  
		oracle.add("LightGreen");  
		oracle.add("LightPink");  
		oracle.add("LightSalmon");  
		oracle.add("LightSeaGreen");  
		oracle.add("LightSkyBlue");   
		oracle.add("LightSlateGray");   
		oracle.add("LightSteelBlue");   
		oracle.add("LightYellow");   
		oracle.add("Lime");   
		oracle.add("LimeGreen");  
		oracle.add("Linen");   
		oracle.add("Magenta");   
		oracle.add("Maroon");   
		oracle.add("MediumAquaMarine");   
		oracle.add("MediumBlue");  
		oracle.add("MediumOrchid");  
		oracle.add("MediumPurple");   
		oracle.add("MediumSeaGreen");  
		oracle.add("MediumSlateBlue");   
		oracle.add("MediumSpringGreen");
		oracle.add("MediumTurquoise");   
		oracle.add("MediumVioletRed");  
		oracle.add("MidnightBlue");   
		oracle.add("MintCream");  
		oracle.add("MistyRose");  
		oracle.add("Moccasin");  
		oracle.add("NavajoWhite");   
		oracle.add("Navy");  
		oracle.add("OldLace");   
		oracle.add("Olive");   
		oracle.add("OliveDrab");   
		oracle.add("Orange");   
		oracle.add("OrangeRed");  
		oracle.add("Orchid");  
		oracle.add("PaleGoldenRod");  
		oracle.add("PaleGreen");  
		oracle.add("PaleTurquoise");   
		oracle.add("PaleVioletRed");   
		oracle.add("PapayaWhip");   
		oracle.add("PeachPuff");   
		oracle.add("Peru"); 
		oracle.add("Pink");   
		oracle.add("Plum");  
		oracle.add("PowderBlue"); 
		oracle.add("Purple");  
		oracle.add("Red"); 
		oracle.add("RosyBrown");  
		oracle.add("RoyalBlue");  
		oracle.add("SaddleBrown");   
		oracle.add("Salmon");  
		oracle.add("SandyBrown");   
		oracle.add("SeaGreen");   
		oracle.add("SeaShell");   
		oracle.add("Sienna");  
		oracle.add("Silver");  
		oracle.add("SkyBlue");   
		oracle.add("SlateBlue");   
		oracle.add("SlateGray");  
		oracle.add("Snow");   
		oracle.add("SpringGreen");  
		oracle.add("SteelBlue");  
		oracle.add("Tan");   
		oracle.add("Teal");   
		oracle.add("Thistle");   
		oracle.add("Tomato");  
		oracle.add("Turquoise"); 
		oracle.add("Violet");  
		oracle.add("Wheat"); 
		oracle.add("White");  
		oracle.add("WhiteSmoke");  
		oracle.add("Yellow");   
		oracle.add("YellowGreen");  
		oracle.add(""); 
	}
}
