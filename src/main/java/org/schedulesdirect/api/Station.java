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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

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
		private int length;
		private String md5;
		private Date lastModified;
		
		Logo(JSONObject src) throws InvalidJsonObjectException {
			try {
				url = new URL(src.getString("URL"));
				String[] dim = src.getString("dimension").split("x");
				width = Integer.parseInt(dim[0]);
				length = Integer.parseInt(dim[1]);
				md5 = src.getString("md5");
				lastModified = Config.get().getDateTimeFormat().parse(src.getString("modified"));
			} catch (Throwable e) {
				throw new InvalidJsonObjectException(e);
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
		URL getUrl() {
			return url;
		}
		/**
		 * @param url the url to set
		 */
		void setUrl(URL url) {
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
		 * @return the length
		 */
		public int getLength() {
			return length;
		}
		/**
		 * @param length the length to set
		 */
		public void setLength(int length) {
			this.length = length;
		}
		/**
		 * @return the md5
		 */
		public String getMd5() {
			return md5;
		}
		/**
		 * @param md5 the md5 to set
		 */
		public void setMd5(String md5) {
			this.md5 = md5;
		}
		/**
		 * @return the lastModified
		 */
		public Date getLastModified() {
			return lastModified;
		}
		/**
		 * @param lastModified the lastModified to set
		 */
		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Logo [width=");
			builder.append(width);
			builder.append(", length=");
			builder.append(length);
			builder.append(", md5=");
			builder.append(md5);
			builder.append(", lastModified=");
			builder.append(lastModified);
			builder.append("]");
			return builder.toString();
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
	private Airing[] airings;
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
			affiliate = src.getString("affiliate");
			JSONObject o = src.getJSONObject("broadcaster");
			broadcasterState = o.getString("state");
			broadcasterCity = o.getString("city");
			//TODO: Report bug: mixing postalcode and zipcode in station data
			broadcasterZip = o.optString("zipcode", null);
			if(broadcasterZip == null)
				broadcasterZip = o.optString("postalcode", null);
			if(broadcasterZip == null)
				broadcasterZip = "00000";
			broadcasterCountry = o.getString("country");
			if(o.has("logo"))
				logo = new Logo(o.getJSONObject("logo"));
			else
				logo = null;
			if(tuningDetails == null)
				tuningDetails = new JSONObject();
			uhfVhfNumber = tuningDetails.optInt("uhfVhf", 0);
			atscMajorNumber = tuningDetails.optInt("atscMajor", 0);
			atscMinorNumber = tuningDetails.optInt("atscMinor", 0);
		} catch (JSONException e) {
			throw new InvalidJsonObjectException(e);
		}
	}

	/**
	 * @return The Station's unique id
	 */
	public String getId() {
		return id;
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
		StringBuilder sb = new StringBuilder();
		if(atscMajorNumber > 0)
			sb.append(atscMajorNumber);
		if(atscMinorNumber > 0) {
			if(sb.length() > 0)
				sb.append("-");
			sb.append(atscMinorNumber);
		}
		return sb.length() > 0 ? sb.toString() : null;
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
}
