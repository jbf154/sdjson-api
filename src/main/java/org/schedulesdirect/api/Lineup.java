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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.utils.UriUtils;

/**
 * A Lineup represents a single television lineup available in a Headend
 * 
 * <p>Only an EpgClient can instantiate objects of this class.</p>
 * 
 * <p>When there's a discrepancy between the descriptions of data found in this source code and
 * that found <a href="https://github.com/rkulagowski/tv_grab_na_sd/wiki">here</a> then the 
 * latter web source will always take precedence!</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class Lineup {
	
	private String id;
	private String name;
	private Date lastModified;
	private String location;
	private String uri;
	private String type;
	private JSONArray channelMap;
	private Map<String, List<String>> stationMap;
	private Map<String, List<String>> physicalStationMap;
	private Map<String, Station> stations;
	private EpgClient epgClnt;
	private boolean physicalMapping;
	private boolean detailsFetched;
	private boolean airingsFetched;
	
	/**
	 * Constructor
	 * @param name The name of the lineup; e.g. Shaw Direct Satellite
	 * @param location The location of the lineup; e.g. Madison, WI
	 * @param uri The URI where the remaining details of this lineup can be found
	 * @param clnt The EpgClient instance this object is to be attached to
	 */
	Lineup(final String name, final String location, final String uri, final String type, final EpgClient clnt) {
		this.stationMap = null;
		this.physicalStationMap = null;
		this.stations = null;
		epgClnt = clnt;
		this.uri = UriUtils.stripApiVersion(uri);
		this.name = name;
		this.location = location;
		this.type = type;
		channelMap = null;
		id = this.uri.substring(this.uri.lastIndexOf('/') + 1);
		detailsFetched = false;
		airingsFetched = false;
	}
	
	/**
	 * Fill in the details of this Lineup object
	 * <p>
	 *  You must call this method before calling any of the object's accessor
	 *  methods.  The Lineup object is built in a JIT manner.
	 * </p>
	 * <p>
	 * 	Until this method is called, a Lineup object only contains minimal data
	 *  to help identify the lineup it represents.  Since most of the details of a Lineup
	 *  are gathered thru the construction of other objects, their construction is
	 *  relatively expensive and is therefore implemented in a JIT manner.  EpgClients
	 *  that are accessing data from a local cache (i.e. ZipEpgClient) can usually safely
	 *  call this method with fetchAirings=true whereas EpgClients hitting the remote
	 *  Schedules Direct servers to fill this object in will probably want to do it on
	 *  demand unless you know you need access to all the airings in all the lineup's
	 *  stations.  Either way, building a complete Lineup object over the network is going
	 *  to be very slow; even slowing if you request all airings to be pulled down as well.
	 *  
	 *  Even ZipEpgClient should avoid pulling in all Airings for a Lineup unless you know
	 *  you're going to need them all.  Even with a local cache, building up the complete
	 *  Lineup with all metadata and airings is still very expensive.
	 * </p>
	 * @param fetchAirings If true, all Stations will fill in their Airings as well, otherwise Stations will fill Airings in as needed
	 * @throws IOException On any IO error
	 */
	public void fetchDetails(final boolean fetchAirings) throws IOException {
		if(!detailsFetched) {
			stationMap = new HashMap<String, List<String>>();
			physicalStationMap = new HashMap<String, List<String>>();
			try {
				JSONObject resp = new JSONObject(epgClnt.fetchChannelMapping(this));
				channelMap = resp.getJSONArray("map");
				Map<String, JSONObject> tuningData = getTuningData(resp.getJSONArray("map"));
				fillStations(resp.getJSONArray("stations"), tuningData);
				fillMetadata(resp.getJSONObject("metadata"));
				if(physicalMapping)
					buildChannelMapViaAtscData();
				else
					buildChannelMapViaJsonData();
			} catch(Exception e) {
				throw new IOException(e);
			}
			detailsFetched = true;
		}
		
		if(fetchAirings && !airingsFetched) {
			Map<Station, Airing[]> map = epgClnt.fetchSchedules(this);
			for(Station s : map.keySet()) {
				Airing[] a = map.get(s);
				stations.get(s.getId()).setAirings(a);
			}
			airingsFetched = true;
		}
	}
		
	private void fillMetadata(final JSONObject data) throws JSONException, ParseException {
		lastModified = Config.get().getDateTimeFormat().parse(data.getString("modified"));
	}
	
	private Map<String, JSONObject> getTuningData(final JSONArray data) throws JSONException {
		Map<String, JSONObject> result = new HashMap<>();
		for(int i = 0; i < data.length(); ++i) {
			JSONObject o = data.getJSONObject(i);
			result.put(o.getString("stationID"), o);
			if(o.has("uhfVhf"))
				physicalMapping = true;
		}
		return result;
	}
	
	private void fillStations(final JSONArray stationsArray, final Map<String, JSONObject> tuningData) throws JSONException {
		stations = new HashMap<String, Station>();
		for(int i = 0; i < stationsArray.length(); ++i) {
			JSONObject s = stationsArray.getJSONObject(i);
			String id = s.getString("stationID");
			stations.put(id, new Station(s, tuningData.get(id), epgClnt));
		}
	}
	
	private void buildChannelMapViaJsonData() throws JSONException {
		for(int i = 0; i < channelMap.length(); ++i) {
			JSONObject o = channelMap.getJSONObject(i);
			List<String> list = stationMap.get(o.getInt("stationID"));
			if(list == null) {
				list = new ArrayList<String>();
				stationMap.put(o.getString("stationID"), list);
			}
			list.add(o.getString("channel").replaceAll("\\.", "-"));
		}
	}

	private void buildChannelMapViaAtscData() {
		boolean diffLogicalVsPhysical = false;
		for(Station s : stations.values()) {
			String pNum = s.getPhysicalChannelNumber();
			String lNum = s.getLogicalChannelNumber();
			if(lNum == null)
				lNum = pNum;
			if(lNum != null) {
				List<String> list = stationMap.get(s.getId());
				if(list == null) {
					list = new ArrayList<String>();
					stationMap.put(s.getId(), list);
				}
				list.add(lNum);
			}
			if(pNum != null) {
				List<String> list = physicalStationMap.get(s.getId());
				if(list == null) {
					list = new ArrayList<String>();
					physicalStationMap.put(s.getId(), list);
				}
				list.add(pNum);
			}
			if(pNum != null && lNum != null && !pNum.equals(lNum))
				diffLogicalVsPhysical = true;
		}
		physicalMapping = diffLogicalVsPhysical;
	}

	/**
	 * @return Returns true if this lineup has a physical channel mapping that differs from its logical channel mapping; this will usually occur for most OTA lineups
	 */
	public boolean hasPhysicalMapping() {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return physicalMapping;
	}
	
	/**
	 * @return The last time this lineup was modified upstream; use this to cache lineup data
	 */
	public Date getLastModified() {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return lastModified;
	}

	/**
	 * @return The physical location of this headend (i.e. city/country/etc.); may not always be available
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return The name of this lineup; may not always be available
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the mapping of station ids to channel numbers for this lineup; keys are station ids, values are arrays of channel numbers to which the station maps to
	 */
	public Map<String, List<String>> getStationMap() {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return stationMap;
	}
	
	/**
	 * @return Return the physical station mapping for this lineup; if there is no physical mapping for this lineup then the logical mapping is returned instead
	 */
	public Map<String, List<String>> getPhysicalStationMap() {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return physicalMapping ? physicalStationMap : stationMap;
	}

	/**
	 * @return The array of stations available on this lineup
	 */
	public Station[] getStations() {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return stations.values().toArray(new Station[0]);
	}

	/**
	 * Return the Station object for a given Station id in the lineup
	 * @param stationId The station id to fetch
	 * @return The Station object for the given station id or null if it's not available
	 */
	public Station getStation(final String stationId) {
		if(channelMap == null)
			throw new IllegalStateException("Must call fetchDetails() before calling this method!");
		return stations.get(stationId);
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 2;
		return "Lineup [id="
				+ id
				+ ", name="
				+ name
				+ ", lastModified="
				+ lastModified
				+ ", location="
				+ location
				+ ", uri="
				+ uri
				+ ", type="
				+ type
				+ ", channelMap="
				+ channelMap
				+ ", stationMap="
				+ (stationMap != null ? toString(stationMap.entrySet(), maxLen)
						: null)
				+ ", physicalStationMap="
				+ (physicalStationMap != null ? toString(
						physicalStationMap.entrySet(), maxLen) : null)
				+ ", stations="
				+ (stations != null ? toString(stations.entrySet(), maxLen)
						: null) + ", physicalMapping=" + physicalMapping
				+ ", epgClnt=" + epgClnt + "]";
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
