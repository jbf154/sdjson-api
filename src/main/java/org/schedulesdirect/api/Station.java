/*
 *      Copyright 2012-2014 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package org.schedulesdirect.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.api.exception.SilentInvalidJsonObjectException;

/**
 * A Station represents a single station available on a lineup
 * 
 * <p>Only EpgClient can instantiate instances of this class</p>
 * 
 * <p>When there's a discrepancy between the descriptions of data found in this source code and
 * that found <a href="https://github.com/rkulagowski/tv_grab_na_sd/wiki">here</a> then the 
 * latter web source will always take precedence!</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class Station {
	
	public class Logo {
				
		private URL url;
		private int width;
		private int height;
		private String md5;
		
		private Logo(JSONObject src) throws InvalidJsonObjectException {
			try {
				url = new URL(src.getString("URL"));
				width = src.getInt("width");
				height = src.getInt("height");
				md5 = src.getString("md5");
			} catch (Throwable e) {
				throw new SilentInvalidJsonObjectException(e);
			}
		}
		
		/**
		 * Grab the logo image for this station as a raw input stream; caller is responsible for closing this stream when done with it.
		 * @return An input stream containing the raw image data for this station's logo
		 * @throws IOException On any IO error
		 */
		public InputStream getImage() throws IOException {
			return Station.this.epgClnt.fetchLogoStream(Station.this);
		}
		
		/**
		 * Save the logo image to a file
		 * @param dest The file to write the image to
		 * @throws IOException On any IO error
		 */
		public void writeImageToFile(File dest) throws IOException {
			Station.this.epgClnt.writeLogoToFile(Station.this, dest);
		}
		
		/**
		 * @return the url
		 */
		public URL getUrl() {
			return url;
		}
		/**
		 * @param url the url to set
		 */
		public void setUrl(URL url) {
			this.url = url;
		}
		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}
		/**
		 * @param width the width to set
		 */
		public void setWidth(int width) {
			this.width = width;
		}
		/**
		 * @return the height
		 */
		public int getHeight() {
			return height;
		}
		/**
		 * @param height the height to set
		 */
		public void setLength(int height) {
			this.height = height;
		}
/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Logo [url=" + url + ", width=" + width + ", height="
					+ height + ", md5=" + md5 + "]";
		}

		/**
		 * @return the md5
		 */
		public String getMd5() {
			return md5;
		}

		/**
		 * @param height the height to set
		 */
		public void setHeight(int height) {
			this.height = height;
		}

		/**
		 * @param md5 the md5 to set
		 */
		public void setMd5(String md5) {
			this.md5 = md5;
		}
	}
	
	private String id;
	private String callsign;
	private String name;
	private String affiliate;
	private String broadcasterState;
	private String broadcasterCity;
	private String broadcasterZip;
	private String broadcasterCountry;
	private int uhfVhfNumber;
	private int atscMajorNumber;
	private int atscMinorNumber;
	private String language;
	private Airing[] airings;
	private boolean isCommercialFree;
	private Logo logo;
	private EpgClient epgClnt;
	
	/**
	 * Constructor
	 * @param src The JSON object from which to build this instance; received from raw upstream feed
	 * @param tuningDetails A JSON object with tuning details for this station within the context of the lineup to which it belongs
	 * @param clnt The EpgClient to which this instance belongs to
	 * @throws InvalidJsonObjectException Thrown if the JSON source is not in the expected format
	 */
	Station(final JSONObject src, JSONObject tuningDetails, final EpgClient clnt) throws InvalidJsonObjectException {
		epgClnt = clnt;
		airings = null;
		try {
			id = src.getString("stationID");
			callsign = src.getString("callsign");
			name = src.getString("name");
			affiliate = src.optString("affiliate");
			JSONObject o = src.optJSONObject("broadcaster");
			if(o != null) {
				broadcasterState = o.optString("state");
				broadcasterCity = o.optString("city");
				broadcasterZip = o.optString("postalcode");
				broadcasterCountry = o.optString("country");
			} else {
				broadcasterState = "";
				broadcasterCity = "";
				broadcasterZip = "";
				broadcasterCountry = "";
			}
			
			if(src.has("logo"))
				logo = new Logo(src.getJSONObject("logo"));
			else
				logo = null;
			if(tuningDetails == null)
				tuningDetails = new JSONObject();
			uhfVhfNumber = tuningDetails.optInt("uhfVhf", 0);
			atscMajorNumber = tuningDetails.optInt("atscMajor", 0);
			atscMinorNumber = tuningDetails.optInt("atscMinor", 0);
			language = src.optString("language");
			isCommercialFree = src.optBoolean("isCommercialFree", false);
		} catch (Throwable t) {
			throw new InvalidJsonObjectException(String.format("Station[%s]: %s", id, t.getMessage()), t, String.format("src:%n%s%n%ntuning:%s", src.toString(3), tuningDetails.toString(3)));
		}
	}

	/**
	 * @return The Station's unique id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return The station's language or empty string if not provided (assume English in this case)
	 */
	public String getLanguage() {
		return language;
	}
	
	/**
	 * @return The station's unique callsign
	 */
	public String getCallsign() {
		return callsign;
	}

	/**
	 * @return The station's full name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The station's parent affiliate; may be empty; never null
	 */
	public String getAffiliate() {
		return affiliate;
	}

	/**
	 * @return The state/province from which this station broadcasts
	 */
	public String getBroadcasterState() {
		return broadcasterState;
	}

	/**
	 * @return The city from which this station broadcasts
	 */
	public String getBroadcasterCity() {
		return broadcasterCity;
	}

	/**
	 * @return The zip/postal code from which this station broadcasts
	 */
	public String getBroadcasterZip() {
		return broadcasterZip;
	}

	/**
	 * @return The country from which this station broadcasts
	 */
	public String getBroadcasterCountry() {
		return broadcasterCountry;
	}

	/**
	 * @return The physical UHF/VHF number this station broadcasts on; only for OTA stations; zero if not applicable
	 */
	public int getUhfVhfNumber() {
		return uhfVhfNumber;
	}

	/**
	 * @return The ATSC major number this station broadcasts on; only for OTA stations; zero if not applicable
	 */
	public int getAtscMajorNumber() {
		return atscMajorNumber;
	}

	/**
	 * @return The ATSC minor number this station broadcasts on; only for OTA stations; zero if not applicable
	 */
	public int getAtscMinorNumber() {
		return atscMinorNumber;
	}

	/**
	 * @return A user friendly physical channel number for this station (i.e. 34-1-1); applicable only for OTA stations; null otherwise
	 */
	public String getPhysicalChannelNumber() {
		StringBuilder sb = new StringBuilder();
		if(uhfVhfNumber > 0) {
			sb.append(uhfVhfNumber);
			String logical = getLogicalChannelNumber();
			if(logical != null)
				sb.append(String.format("-%s", logical));
		}
		return sb.length() > 0 ? sb.toString() : null;
	}
	
	/**
	 * @return A user friendly logical channel number for this station (i.e. 5-2); applicable only for OTA stations; null otherwise
	 */
	public String getLogicalChannelNumber() {
		return atscMajorNumber > 0 && atscMinorNumber > 0 ? String.format("%d-%d", atscMajorNumber, atscMinorNumber) : null;
	}

	/**
	 * Get all available future airings scheduled for this station
	 * 
	 * <p>The airings are only downloaded and built on the first access of this array; lazy initialization</p>
	 * @return An array of Airing objects; never null; may be empty
	 * @throws IOException Thrown on any IO error accessing the upstream data feed
	 */
	public Airing[] getAirings() throws IOException {
		if(airings == null)
			downloadProgramsAndAirings();
		return airings;
	}
	
	/**
	 * Get all available programs scheduled to air on this station
	 * 
	 * <p>The programs are only downloaded and built on the first access of this array; lazy initialization</p>
	 * @return An array of Program objects; never null; may be empty
	 * @throws IOException Thrown on any IO error accessing the upstream data feed
	 */
	public Program[] getPrograms() throws IOException {
		if(airings == null)
			downloadProgramsAndAirings();
		Set<Program> progs = new HashSet<Program>();
		for(Airing a : airings)
			progs.add(a.getProgram());
		return progs.toArray(new Program[0]);
	}
	
	/**
	 * Downloads and builds the arrays of programs and airings as needed
	 * @throws IOException Thrown on any IO error accessing the upstream data feed
	 */
	protected void downloadProgramsAndAirings() throws IOException {
		airings = epgClnt.fetchSchedule(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 2;
		return "Station [id="
				+ id
				+ ", callsign="
				+ callsign
				+ ", name="
				+ name
				+ ", affiliate="
				+ affiliate
				+ ", broadcasterState="
				+ broadcasterState
				+ ", broadcasterCity="
				+ broadcasterCity
				+ ", broadcasterZip="
				+ broadcasterZip
				+ ", broadcasterCountry="
				+ broadcasterCountry
				+ ", uhfVhfNumber="
				+ uhfVhfNumber
				+ ", atscMajorNumber="
				+ atscMajorNumber
				+ ", atscMinorNumber="
				+ atscMinorNumber
				+ ", airings="
				+ (airings != null ? Arrays.asList(airings).subList(0,
						Math.min(airings.length, maxLen)) : null) + ", logo="
				+ logo + ", epgClnt=" + epgClnt + "]";
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param callsign the callsign to set
	 */
	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param affiliate the affiliate to set
	 */
	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	/**
	 * @param broadcasterState the broadcasterState to set
	 */
	public void setBroadcasterState(String broadcasterState) {
		this.broadcasterState = broadcasterState;
	}

	/**
	 * @param broadcasterCity the broadcasterCity to set
	 */
	public void setBroadcasterCity(String broadcasterCity) {
		this.broadcasterCity = broadcasterCity;
	}

	/**
	 * @param broadcasterZip the broadcasterZip to set
	 */
	public void setBroadcasterZip(String broadcasterZip) {
		this.broadcasterZip = broadcasterZip;
	}

	/**
	 * @param broadcasterCountry the broadcasterCountry to set
	 */
	public void setBroadcasterCountry(String broadcasterCountry) {
		this.broadcasterCountry = broadcasterCountry;
	}

	/**
	 * @param uhfVhfNumber the uhfVhfNumber to set
	 */
	public void setUhfVhfNumber(int uhfVhfNumber) {
		this.uhfVhfNumber = uhfVhfNumber;
	}

	/**
	 * @param atscMajorNumber the atscMajorNumber to set
	 */
	public void setAtscMajorNumber(int atscMajorNumber) {
		this.atscMajorNumber = atscMajorNumber;
	}

	/**
	 * @param atscMinorNumber the atscMinorNumber to set
	 */
	public void setAtscMinorNumber(int atscMinorNumber) {
		this.atscMinorNumber = atscMinorNumber;
	}

	/**
	 * Set the Airings for this Station; all of the Airing's Station references will be changed to point to this Station instance
	 * @param airings the airings to set
	 */
	public void setAirings(Airing[] airings) {
		this.airings = airings;
		for(Airing a : airings)
			a.setStation(this);
	}

	/**
	 * @return the logo
	 */
	public Logo getLogo() {
		return logo;
	}

	/**
	 * @param logo the logo to set
	 */
	public void setLogo(Logo logo) {
		this.logo = logo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @return the isCommercialFree
	 */
	public boolean isCommercialFree() {
		return isCommercialFree;
	}

	/**
	 * @param isCommercialFree the isCommercialFree to set
	 */
	public void setCommercialFree(boolean isCommercialFree) {
		this.isCommercialFree = isCommercialFree;
	}
}
