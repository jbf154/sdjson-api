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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

/**
 * An Airing represents a scheduled broadcast of a Program object.
 * 
 * <p>Only EpgClient can instantiate objects of this class.</p>
 * 
 * <p>
 * 	When there's a discrepancy between the descriptions of data found in this source code and
 * 	that found <a href="https://github.com/rkulagowski/tv_grab_na_sd/wiki">here</a> then the latter
 *  web source will always take precedence!
 * </p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class Airing {
	static private final Log LOG = LogFactory.getLog(Airing.class);
	
	/**
	 * Represents the possible values of the Dolby value of an airing
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum DolbyStatus {
		/**
		 * This airing is not in Dolby sound
		 */
		NONE,
		
		/**
		 * This airing is in Dolby Digital 5.1 sound
		 */
		DD51,
		
		/**
		 * This airing is in Dolby Digital sound
		 */
		DD,
		
		/**
		 * This airing is in Dolby sound
		 */
		DOLBY,
		
		/**
		 * An unknown value was provided for Dolby status; report the unknown value as a bug ticket for future inclusion
		 */
		UNKNOWN;
		
		public static DolbyStatus fromValue(String val) {
			switch(val.toLowerCase().replaceAll("\\W", "")) {
				case "dd": return DD;
				case "dd51": return DD51;
				case "dolby": return DOLBY;
			}
			
			return UNKNOWN;
		}
	}
	
	/**
	 * Represents the live status of an airing
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum LiveStatus {
		/**
		 * No status was provided; this is valid and acceptable
		 */
		NONE,
		
		/**
		 * This airing is live
		 */
		LIVE,
		
		/**
		 * This airing is on a delay; typically a rebroadcast of a live event
		 */
		DELAY,
		
		/**
		 * This airing is taped
		 */
		TAPE,
		
		/**
		 * An unknown value was provided; report the unknown value as a bug ticket for future inclusion
		 */
		UNKNOWN
	}
	
	/**
	 * Represents the premiere status of an airing
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum PremiereStatus {
		/**
		 * This airing is not a premiere airing
		 */
		NONE,
		
		/**
		 * This airing is a premiere airing; typically a movie and not a tv show, but not necessarily
		 */
		PREMIERE,
		
		/**
		 * This airing is a season premiere; typically a tv show
		 */
		SEASON_PREMIERE,
		
		/**
		 * This airing is a series premiere; typically a tv show
		 */
		SERIES_PREMIERE,
		
		/**
		 * And unknown value was received; report the unknown value as a bug ticket for future inclusion
		 */
		UNKNOWN
	}

	/**
	 * Represents the finale status of an airing
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum FinaleStatus {
		/**
		 * The airing is not a finale
		 */
		NONE,
		
		/**
		 * The airing is a season finale; typically a tv show
		 */
		SEASON_FINALE,
		
		/**
		 * The airing is a series finale; typically a tv show
		 */
		SERIES_FINALE,
		
		/**
		 * An unknown value was received; report the unknown value as a bug ticket for future inclusion
		 */
		UNKNOWN
	}

	/**
	 * Represents the content type of the airing
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum ContentType {
		/**
		 * No type info was provided by the upstream; this is valid and acceptable
		 */
		NONE,
		OFF_NETWORK,
		BROADCAST_NETWORK,
		FIRST_RUN_SYNDICATION,
		/**
		 * An unknown value was provided; report the value as a bug ticket for future inclusion
		 */
		UNKNOWN
	}
	
	private String id;
	private boolean subjectToBlackout;
	private boolean educational;
	private boolean joinedInProgress;
	private boolean leftInProgress;
	private String contentSource;
	private int partNum;
	private boolean closedCaptioned;
	private boolean stereo;
	private boolean newAiring;
	private int duration;
	private int totalParts;
	private DolbyStatus dolbyStatus;
	private LiveStatus liveStatus;
	private boolean hdtv;
	private PremiereStatus premiereStatus;
	private FinaleStatus finaleStatus;
	private ContentRating[] tvRatings;
	private ContentType contentType;
	private boolean letterboxed;
	private Date gmtStart;
	private boolean descriptiveVideo;
	private boolean is3d;
	private boolean cableInTheClassroom;
	private boolean enhanced;
	private boolean sap;
	private String sapLanguage;
	private boolean subtitled;
	private String subtitleLanguage;
	private boolean timeApproximate;
	private String broadcastLanguage;
	private Station station;
	private Program program;
	
	/**
	 * Constructor
	 * @param src The upstream raw data source from which the instance will be constructed
	 * @param prog The Program to associate with this Airing
	 * @param station The Station to associate with this Airing
	 * @throws InvalidJsonObjectException Thrown if the src is not in the expected format
	 * @throws IllegalArgumentException Thrown if the Program or Station argument is null or if the Program's id does not match the id of this Airing
	 */
	Airing(JSONObject src, Program prog, Station station) throws InvalidJsonObjectException, IllegalArgumentException {
		if(prog == null)
			throw new IllegalArgumentException("An Airing's Program cannot be null!");
		if(station == null)
			throw new IllegalArgumentException("An Airing's Station cannot be null!");
		program = prog;
		this.station = station;

		try {
			id = src.getString("programID");
			if(!program.getId().equals(id))
				throw new IllegalArgumentException("Received Program does not match id of Airing!");
			duration = Integer.parseInt(src.get("duration").toString());
			gmtStart = Config.get().getDateTimeFormat().parse(src.getString("airDateTime"));
			JSONArray audioOpts = src.optJSONArray("audioProperties");
			dolbyStatus = DolbyStatus.NONE;
			if(audioOpts != null) {
				for(int i = 0; i < audioOpts.length(); ++i) {
					String val = audioOpts.getString(i);
					switch(val) {
						case "cc": closedCaptioned = true; break;
						case "stereo": stereo = true; break;
						case "dvs": descriptiveVideo = true; break;
						case "subtitled": subtitled = true; break;
						case "SAP": sap = true; break;
						default:
							if(val.startsWith("D")) { // This is a Dolby marker
								dolbyStatus = DolbyStatus.fromValue(val);
							} else
								LOG.warn(String.format("Unknown audio property encountered! [%s]", val));
					}
				}
			}
			timeApproximate = src.optBoolean("timeApproximate");
			if(subtitled && src.has("subtitledLanguage"))
				subtitleLanguage = src.getString("subtitledLanguage");
			cableInTheClassroom = src.optBoolean("cableInTheClassroom");
			subjectToBlackout = src.optBoolean("subjectToBlackout");
			educational = src.optBoolean("educational");
			joinedInProgress = src.optBoolean("joinedInProgress");
			leftInProgress = src.optBoolean("leftInProgress");
			contentSource = src.optString("netSyndicationSource");
			JSONObject partInfo = src.optJSONObject("multipart");
			if(partInfo != null) {
				partNum = partInfo.getInt("partNumber");
				totalParts = partInfo.getInt("totalParts");
			} else {
				partNum = 0;
				totalParts = 0;
			}
			newAiring = src.optBoolean("new");
			String live = src.optString("liveTapeDelay", LiveStatus.NONE.toString()).toUpperCase();
			try {
				liveStatus = live.length() == 0 ? LiveStatus.NONE : LiveStatus.valueOf(live);
			} catch(IllegalArgumentException e) {
				LOG.warn(String.format("Unknown LiveStatus encountered! [%s]", live));
				liveStatus = LiveStatus.UNKNOWN;
			}
			JSONArray videoOpts = src.optJSONArray("videoProperties");
			if(videoOpts != null)  {
				for(int i = 0; i < videoOpts.length(); ++i) {
					String val = videoOpts.getString(i);
					switch(val) {
						case "hdtv": hdtv = true; break;
						case "letterbox": letterboxed = true; break;
						case "enhanced": enhanced = true; break;
						case "3d": is3d = true; break;
					}
				}
			}
			String premiereFinale = src.optString("isPremiereOrFinale").toUpperCase().replace(' ', '_');
			if(premiereFinale.length() == 0) {
				premiereStatus = PremiereStatus.NONE;
				finaleStatus = FinaleStatus.NONE;
			} else if(premiereFinale.contains("PREMIERE")) {
				finaleStatus = FinaleStatus.NONE;
				try {
					premiereStatus = PremiereStatus.valueOf(premiereFinale);
				} catch(IllegalArgumentException e) {
					LOG.warn(String.format("Unknown PremiereStatus encountered! [%s]", premiereFinale));
					premiereStatus = PremiereStatus.UNKNOWN;
				}
			} else {
				premiereStatus = PremiereStatus.NONE;
				try {
					finaleStatus = FinaleStatus.valueOf(premiereFinale);
				} catch(IllegalArgumentException e) {
					LOG.warn(String.format("Unknown FinaleStatus encountered! [%s]", premiereFinale));
					finaleStatus = FinaleStatus.UNKNOWN;
				}
			}
			JSONArray ratings = src.optJSONArray("contentRating");
			if(ratings != null) {
				Collection<ContentRating> ratingsColl = new ArrayList<>();
				for(int i = 0; i < ratings.length(); ++i) {
					JSONObject o = ratings.getJSONObject(i);
					ratingsColl.add(new ContentRating(o.getString("body"), o.getString("code")));
				}
				tvRatings = ratingsColl.toArray(new ContentRating[0]);
			} else
				tvRatings = new ContentRating[0];
			JSONObject content = src.optJSONObject("syndication");
			if(content != null) {
				try {
					String type = content.getString("type").toUpperCase().replace(' ', '_');
					contentType = type.length() == 0 ? ContentType.NONE : ContentType.valueOf(type);
				} catch(IllegalArgumentException e) {
					LOG.warn(String.format("Unknwon ContentType encountered! [%s]", content));
					contentType = ContentType.UNKNOWN;
				}				
			} else
				contentType = ContentType.NONE;
			broadcastLanguage = src.optString("programLanguage", null);
		} catch(Throwable t) {
			throw new InvalidJsonObjectException(String.format("Airing[%s]: %s", id, t.getMessage()), t, src.toString(3));
		}
	}

	/**
	 * Get the language this airing broadcasts in.
	 * <p>This field is a hint and not 100% accurate; if null assume English, but not guaranteed.</p>
	 * @return The language for this airing or null if the language is English or unknown
	 */
	public String getBroadcastLanguage() {
		return broadcastLanguage;
	}

	/**
	 * Set this airing's broadcasting language.
	 * @param lang The language this airing is broadcasting in or null if English or unknown
	 */
	public void setBroadcastLanguage(String lang) {
		broadcastLanguage = lang;
	}
	
	/**
	 * @return True if this airing is subject to blackout or false otherwise
	 */
	public boolean isSubjectToBlackout() {
		return subjectToBlackout;
	}

	/**
	 * @return True if this airing is that of an educational or informational nature or false otherwise
	 */
	public boolean isEducational() {
		return educational;
	}

	/**
	 * @return True if this airing is joining a live event in progress or false otherwise
	 */
	public boolean isJoinedInProgress() {
		return joinedInProgress;
	}

	/**
	 * @return True if this airing is scheduled to end while the live event is expected to still be in progress or false otherwise
	 */
	public boolean isLeftInProgress() {
		return leftInProgress;
	}

	/**
	 * @return Returns the source of the content
	 */
	public String getContentSource() {
		return contentSource;
	}

	/**
	 * @return Returns the part number of this airing or 0 if not applicable (i.e. there aren't multiple parts to the Program)
	 */
	public int getPartNum() {
		return partNum;
	}

	/**
	 * @return True if this airing is closed captioned or false otherwise
	 */
	public boolean isClosedCaptioned() {
		return closedCaptioned;
	}

	/**
	 * @return True if this airing is in stereo sound or false otherwise
	 */
	public boolean isStereo() {
		return stereo;
	}

	/**
	 * @return True if this airing is new (as determined by the upstream) or false otherwise
	 */
	public boolean isNewAiring() {
		return newAiring;
	}

	/**
	 * @return Returns the duration of this airing, in seconds
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @return Returns the total number of parts or 0 if not applicable (i.e. there aren't multiple parts to this Program)
	 */
	public int getTotalParts() {
		return totalParts;
	}

	/**
	 * @return Return the Dolby sound status of this airing
	 */
	public DolbyStatus getDolbyStatus() {
		return dolbyStatus;
	}

	/**
	 * @return Returns the live status of this airing
	 */
	public LiveStatus getLiveStatus() {
		return liveStatus;
	}

	/**
	 * @return True if this airing is in HD or false otherwise
	 */
	public boolean isHdtv() {
		return hdtv;
	}

	/**
	 * @return Returns the premiere status of this airing
	 */
	public PremiereStatus getPremiereStatus() {
		return premiereStatus;
	}

	/**
	 * @return Returns the finale status of this airing
	 */
	public FinaleStatus getFinaleStatus() {
		return finaleStatus;
	}

	/**
	 * @return Returns the assigned TV ratings for this airing
	 */
	public ContentRating[] getTvRatings() {
		return tvRatings;
	}

	/**
	 * @return Returns the unique upstream (i.e. Data Direct) id for the Program this airing is attached to
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the content type of this airing
	 */
	public ContentType getContentType() {
		return contentType;
	}

	/**
	 * @return True if this airing is letterboxed or false otherwise
	 */
	public boolean isLetterboxed() {
		return letterboxed;
	}

	/**
	 * @return Returns the scheduled start time of this airing, represented in GMT time zone
	 */
	public Date getGmtStart() {
		return gmtStart;
	}

	/**
	 * @return True if this airing provies descriptive video or false otherwise
	 */
	public boolean isDescriptiveVideo() {
		return descriptiveVideo;
	}

	/**
	 * @return True if this airing is in 3D or false otherwise 
	 */
	public boolean is3d() {
		return is3d;
	}

	/**
	 * @return True if this airing is available via Cable in the Classroom or false otherwise
	 */
	public boolean isCableInTheClassroom() {
		return cableInTheClassroom;
	}

	/**
	 * @return True if this airing is enhanced or false otherwise (NOTE: I really don't know what this means)
	 */
	public boolean isEnhanced() {
		return enhanced;
	}

	/**
	 * @return True if this airing provides SAP or false otherwise
	 */
	public boolean isSap() {
		return sap;
	}

	/**
	 * @return If SAP is available, this returns the alternate language that is available; will return null when data not available
	 */
	public String getSapLanguage() {
		return sapLanguage;
	}

	/**
	 * @return True if this airing is subtitled or false otherwise
	 */
	public boolean isSubtitled() {
		return subtitled;
	}

	/**
	 * @return If subtitled, returns the language of the subtitles; will return null if no data is available
	 */
	public String getSubtitleLanguage() {
		return subtitleLanguage;
	}

	/**
	 * @return True if the duration is approximate or false otherwise
	 */
	public boolean isTimeApproximate() {
		return timeApproximate;
	}

	/**
	 * Modify this Airing's unique id; this is the id of the Program that this Airing represents
	 * @param id The unique id for this Airing
	 * @throws IllegalArgumentException Thrown if id is null or if the id does not match the id of the attached Program object in this Airing
	 */
	public void setId(String id) throws IllegalArgumentException {
		if(id == null || !program.getId().equals(id))
			throw new IllegalArgumentException(String.format("The new id for a Program cannot be null and it must match the id of the attached Program [%s]", program.getId()));
		this.id = id;
	}

	/**
	 * @param subjectToBlackout the subjectToBlackout to set
	 */
	public void setSubjectToBlackout(boolean subjectToBlackout) {
		this.subjectToBlackout = subjectToBlackout;
	}

	/**
	 * @param educational the educational to set
	 */
	public void setEducational(boolean educational) {
		this.educational = educational;
	}

	/**
	 * @param joinedInProgress the joinedInProgress to set
	 */
	public void setJoinedInProgress(boolean joinedInProgress) {
		this.joinedInProgress = joinedInProgress;
	}

	/**
	 * @param leftInProgress the leftInProgress to set
	 */
	public void setLeftInProgress(boolean leftInProgress) {
		this.leftInProgress = leftInProgress;
	}

	/**
	 * @param contentSource the contentSource to set
	 */
	public void setContentSource(String contentSource) {
		this.contentSource = contentSource;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(int partNum) {
		this.partNum = partNum;
	}

	/**
	 * @param closedCaptioned the closedCaptioned to set
	 */
	public void setClosedCaptioned(boolean closedCaptioned) {
		this.closedCaptioned = closedCaptioned;
	}

	/**
	 * @param stereo the stereo to set
	 */
	public void setStereo(boolean stereo) {
		this.stereo = stereo;
	}

	/**
	 * @param newAiring the newAiring to set
	 */
	public void setNewAiring(boolean newAiring) {
		this.newAiring = newAiring;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @param totalParts the totalParts to set
	 */
	public void setTotalParts(int totalParts) {
		this.totalParts = totalParts;
	}

	/**
	 * @param dolbyStatus the dolbyStatus to set
	 */
	public void setDolbyStatus(DolbyStatus dolbyStatus) {
		this.dolbyStatus = dolbyStatus;
	}

	/**
	 * @param liveStatus the liveStatus to set
	 */
	public void setLiveStatus(LiveStatus liveStatus) {
		this.liveStatus = liveStatus;
	}

	/**
	 * @param hdtv the hdtv to set
	 */
	public void setHdtv(boolean hdtv) {
		this.hdtv = hdtv;
	}

	/**
	 * @param premiereStatus the premiereStatus to set
	 */
	public void setPremiereStatus(PremiereStatus premiereStatus) {
		this.premiereStatus = premiereStatus;
	}

	/**
	 * @param finaleStatus the finaleStatus to set
	 */
	public void setFinaleStatus(FinaleStatus finaleStatus) {
		this.finaleStatus = finaleStatus;
	}

	/**
	 * @param tvRatings the tvRatings to set
	 */
	public void setTvRatings(ContentRating[] tvRatings) {
		this.tvRatings = tvRatings;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param letterboxed the letterboxed to set
	 */
	public void setLetterboxed(boolean letterboxed) {
		this.letterboxed = letterboxed;
	}

	/**
	 * @param gmtStart the gmtStart to set
	 */
	public void setGmtStart(Date gmtStart) {
		this.gmtStart = gmtStart;
	}

	/**
	 * @param descriptiveVideo the descriptiveVideo to set
	 */
	public void setDescriptiveVideo(boolean descriptiveVideo) {
		this.descriptiveVideo = descriptiveVideo;
	}

	/**
	 * @param is3d the is3d to set
	 */
	public void setIs3d(boolean is3d) {
		this.is3d = is3d;
	}

	/**
	 * @param cableInTheClassroom the cableInTheClassroom to set
	 */
	public void setCableInTheClassroom(boolean cableInTheClassroom) {
		this.cableInTheClassroom = cableInTheClassroom;
	}

	/**
	 * @param enhanced the enhanced to set
	 */
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}

	/**
	 * @param sap the sap to set
	 */
	public void setSap(boolean sap) {
		this.sap = sap;
	}

	/**
	 * @param sapLanguage the sapLanguage to set
	 */
	public void setSapLanguage(String sapLanguage) {
		this.sapLanguage = sapLanguage;
	}

	/**
	 * @param subtitled the subtitled to set
	 */
	public void setSubtitled(boolean subtitled) {
		this.subtitled = subtitled;
	}

	/**
	 * @param subtitleLanguage the subtitleLanguage to set
	 */
	public void setSubtitleLanguage(String subtitleLanguage) {
		this.subtitleLanguage = subtitleLanguage;
	}

	/**
	 * @param timeApproximate the timeApproximate to set
	 */
	public void setTimeApproximate(boolean timeApproximate) {
		this.timeApproximate = timeApproximate;
	}

	/**
	 * @return the station
	 */
	public Station getStation() {
		return station;
	}

	/**
	 * Change the Station this Airing is associated with (i.e. change the station this airing will broadcast on)
	 * @param station The new Station to associate with this Airing
	 * @throws IllegalArgumentException Thrown if the argument is null
	 */
	public void setStation(Station station) throws IllegalArgumentException {
		if(station == null)
			throw new IllegalArgumentException("An Airing's Station cannot be null!");
		this.station = station;
	}

	/**
	 * @return the program
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * Change the Program this Airing is associated with; this airing's id is updated to that of the Program argument
	 * @param program The new Program to associate with this Airing
	 * @throws IllegalArgumentException Thrown if the Program argument is null
	 */
	public void setProgram(Program program) throws IllegalArgumentException {
		if(program == null)
			throw new IllegalArgumentException("The new Program cannot be null!");
		this.program = program;
		id = program.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((gmtStart == null) ? 0 : gmtStart.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		result = prime * result + ((station == null) ? 0 : station.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Airing)) {
			return false;
		}
		Airing other = (Airing) obj;
		if (gmtStart == null) {
			if (other.gmtStart != null) {
				return false;
			}
		} else if (!gmtStart.equals(other.gmtStart)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (program == null) {
			if (other.program != null) {
				return false;
			}
		} else if (!program.equals(other.program)) {
			return false;
		}
		if (station == null) {
			if (other.station != null) {
				return false;
			}
		} else if (!station.equals(other.station)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 2;
		return "Airing [id="
				+ id
				+ ", subjectToBlackout="
				+ subjectToBlackout
				+ ", educational="
				+ educational
				+ ", joinedInProgress="
				+ joinedInProgress
				+ ", leftInProgress="
				+ leftInProgress
				+ ", contentSource="
				+ contentSource
				+ ", partNum="
				+ partNum
				+ ", closedCaptioned="
				+ closedCaptioned
				+ ", stereo="
				+ stereo
				+ ", newAiring="
				+ newAiring
				+ ", duration="
				+ duration
				+ ", totalParts="
				+ totalParts
				+ ", dolbyStatus="
				+ dolbyStatus
				+ ", liveStatus="
				+ liveStatus
				+ ", hdtv="
				+ hdtv
				+ ", premiereStatus="
				+ premiereStatus
				+ ", finaleStatus="
				+ finaleStatus
				+ ", tvRatings="
				+ (tvRatings != null ? Arrays.asList(tvRatings).subList(0,
						Math.min(tvRatings.length, maxLen)) : null)
				+ ", contentType=" + contentType + ", letterboxed="
				+ letterboxed + ", gmtStart=" + gmtStart
				+ ", descriptiveVideo=" + descriptiveVideo + ", is3d=" + is3d
				+ ", cableInTheClassroom=" + cableInTheClassroom
				+ ", enhanced=" + enhanced + ", sap=" + sap + ", sapLanguage="
				+ sapLanguage + ", subtitled=" + subtitled
				+ ", subtitleLanguage=" + subtitleLanguage
				+ ", timeApproximate=" + timeApproximate
				+ ", broadcastLanguage=" + broadcastLanguage + ", station="
				+ station + ", program=" + program + "]";
	}
}
