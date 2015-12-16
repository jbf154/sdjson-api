package org.schedulesdirect.api;

import org.json.JSONObject;

public class Artwork {

	public static enum Size {
		MASSIVE,
		LARGE,
		MEDIUM,
		SMALL,
		EXTRA_SMALL,
		UNKNOWN;
		
		public static Size fromString(String val) {
			if(val != null) {
				switch(val) {
					case "Ms":
						return Size.MASSIVE;
					case "Lg":
						return Size.LARGE;
					case "Md":
						return Size.MEDIUM;
					case "Sm":
						return Size.SMALL;
					case "Xs":
						return Size.EXTRA_SMALL;
				}
			}
			
			return Size.UNKNOWN;
		}
	}
	
	private String aspect;
	private int width;
	private int height;
	private boolean text;
	private String category;
	private String uri;
	private String tier;
	private Size size;
	
	public Artwork(JSONObject obj, EpgClient clnt) {
		aspect = obj.optString("aspect");
		String width = obj.optString("width");
		this.width = width.length() == 0 ? 0 : Integer.parseInt(width);
		String height = obj.optString("height");
		this.height = height.length() == 0 ? 0 : Integer.parseInt(height);
		text = "yes".equalsIgnoreCase(obj.optString("text"));
		category = obj.optString("category");
		tier = obj.optString("tier");
		String uri = obj.optString("uri");
		if(uri.matches("^https?:\\/\\/.*")) {
			this.uri = uri;
		}
		else {
			this.uri = String.format("%s/%s/image/%s", clnt.getBaseUrl(), EpgClient.API_VERSION, uri);
		}
		
		size = Size.fromString(obj.optString("size"));
	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isText() {
		return text;
	}

	public void setText(boolean text) {
		this.text = text;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getTier() {
		return tier;
	}
	
	public void setTier(String tier) {
		this.tier = tier;
	}
	
	public Size getSize() {
		return size;
	}
	
	public void setSize(Size size) {
		this.size = size;
	}
}
