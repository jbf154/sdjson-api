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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;

/**
 * A Program represents the details of events and shows that are broadcast
 * 
 * <p>Only EpgClient can instantiate objects of this class</p>
 * 
 * <p>When there's a discrepancy between the descriptions of data found in this source code and
 * that found <a href="https://github.com/rkulagowski/tv_grab_na_sd/wiki">here</a> then the 
 * latter web source will always take precedence!</p>
 *  
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public class Program {
	static private final Log LOG = LogFactory.getLog(Program.class);
	
	/**
	 * An empty Program object as a JSON string
	 */
	static public final String EMPTY_PROGRAM = "{\"md5\":\"\"}";
		
	/**
	 * The expected format of the original airing field
	 */
	static public final SimpleDateFormat ORIG_FMT = new SimpleDateFormat("yyyy-MM-dd");
	static { ORIG_FMT.setTimeZone(TimeZone.getDefault()); }
		
	/**
	 * The color code of a program
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum ColorCode {
		/**
		 * No data provided; valid and acceptable
		 */
		NONE,
		/**
		 * The program is in color
		 */
		COLOR,
		/**
		 * The program is in Black & White
		 */
		BW,
		/**
		 * The program is in both color and Black & White
		 */
		COLOR_AND_BW,
		/**
		 * The program has been colorized
		 */
		COLORIZED,
		/**
		 * An unknown value was provided; provide the unknown value in a bug ticket for future inclusion
		 */
		UNKNOWN
	}

	/**
	 * Represents the source of a program
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum SourceType {
		/**
		 * No data was provided; valid and acceptable
		 */
		NONE,
		LOCAL,
		SYNDICATED,
		NETWORK,
		BLOCK,
		/**
		 * An unknown value was provided; provide the value in a bug ticket for future inclusion
		 */
		UNKNOWN
	}
	
	/**
	 * The type of show this program is
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum ShowType {
		/**
		 * No data was provided; value and acceptable
		 */
		NONE,
		SERIES,
		PAID_PROGRAMMING,
		SPECIAL,
		MINISERIES,
		/**
		 * An unknown value was provided; provide the value in a bug ticket for future inclusion
		 */
		UNKNOWN
	}
	
	/**
	 * The MPAA rating of this program; typically only provided for movies and will always be set to NONE for non-movies
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum MPAARating {
		NONE,
		AO,
		G,
		NC17,
		NR,
		PG13,
		PG,
		R,
		/**
		 * An unknown value was provided; provide the value in a bug ticket for future inclusion
		 */
		UNKNOWN
	}

	/**
	 * Represents the role of cast &amp; crew members of a program
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public enum Role {
		/**
		 * An unknown value was provided; provide the value in a bug ticket for future inclusion
		 */
		UNKNOWN,
		ACTOR,
		ANCHOR,
		CONTESTANT,
		CORRESPONDENT,
		DIRECTOR,
		EXECUTIVE_PRODUCER,
		GUEST_STAR,
		GUEST,
		HOST,
		JUDGE,
		MUSICAL_GUEST,
		NARRATOR,
		PRODUCER,
		WRITER
	}
	
	/**
	 * Represents a credit in a program
	 * 
	 * <p>Only Program can instantiate objects of this class</p>
	 * 
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public class Credit {

		private Role role;
		private String name;
	
		private Credit(JSONObject src) throws JSONException {
			Role r = null;
			String roleStr = src.getString("role");
			String name = src.getString("name");
			try {
				r = Role.valueOf(roleStr.toUpperCase().replace(' ', '_'));
			} catch(IllegalArgumentException e) {
				LOG.warn(String.format("Unknown Role encountered! [%s]", roleStr));
				r = Role.UNKNOWN;
			}
			role = r;
			this.name = name;
		}

		/**
		 * @return the role
		 */
		public Role getRole() {
			return role;
		}
		/**
		 * @return The full name of the person being credited
		 */
		public String getName() {
			return name;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Credit [role=");
			builder.append(role);
			builder.append(", name=");
			builder.append(name);
			builder.append("]");
			return builder.toString();
		}
	}

	static private final Set<String> WARNED_SHOW_TYPES = new HashSet<>();
	static private final Set<String> WARNED_SRC_TYPES = new HashSet<>();
	static private final Set<String> WARNED_COLOR_CODES = new HashSet<>();
	
	private String id;
	private boolean madeForTv;
	private String description;
	private String episodeTitle;
	private String title;
	private Date originalAirDate;
	private String descriptionLanguage;
	private SourceType sourceType;
	private ShowType showType;
	private String syndicatedEpisodeNumber;
	private ColorCode colorCode;
	private String[] advisories;
	private String alternateEpisodeNumber;
	private String alternateTitle;
	private Credit[] credits;
	private String alternateDescription;
	private String alternateDescriptionShort;
	private String[] shortDescriptions;
	private Date gameStart;
	private String holiday;
	private String md5;
	private MPAARating mpaaRating;
	private String countryOfOrigin;
	private String studio;
	private int runTime;
	private String starRating;
	private float starRatingValue;
	private String episodeNumber;
	private String[] shortTitles;
	private int year;
	private String[] genres;
	private List<Map<String, Object>> metadata;
	private String seriesDescription;
	
	/**
	 * Consutrctor
	 * @param src The JSON object from which this instance is being constructed; cannot be null
	 * @throws InvalidJsonObjectException Thrown if the given src is not in the expected format
	 * @throws IllegalArgumentException Thrown if src is null
	 */
	Program(JSONObject src) throws InvalidJsonObjectException {
		if(src == null)
			throw new IllegalArgumentException("src cannot be null!");
		try {
			id = src.getString("programID");
			seriesDescription = src.optString("seriesDescription", null);
			metadata = new ArrayList<Map<String, Object>>();
			if(src.has("metadata")) {
				JSONArray metaArr = src.getJSONArray("metadata");
				for(int i = 0; i < metaArr.length(); ++i) {
					JSONObject o = metaArr.getJSONObject(i);
					Map<String, Object> map = new HashMap<String, Object>();
					for(String k : JSONObject.getNames(o))
						map.put(k, !o.isNull(k) ? o.get(k) : null);
					metadata.add(map);
				}
			}
			if(src.has("genres")) {
				List<String> vals = new ArrayList<String>();
				JSONArray arr = src.getJSONArray("genres");
				for(int i = 0; i < arr.length(); ++i)
					vals.add(arr.getString(i));
				genres = vals.toArray(new String[0]);
			} else
				genres = new String[0];
			JSONObject movieInfo = src.optJSONObject("movie");
			if(movieInfo != null && movieInfo.has("year"))
				year = Integer.parseInt(movieInfo.get("year").toString());
			else
				year = 0;
			JSONObject titles = src.getJSONObject("titles");
			//TMSBUG: title120 should not be optional!
			title = titles.optString("title120", "[No Title]");
			shortTitles = new String[4];
			shortTitles[0] = titles.optString("title70");
			shortTitles[1] = titles.optString("title40");
			shortTitles[2] = titles.optString("title20");
			shortTitles[3] = titles.optString("title10");
			episodeNumber = src.optString("syndicatedEpisodeNumber");
			starRating = movieInfo != null && movieInfo.has("starRating") ? movieInfo.getString("starRating") : null;
			if(starRating == null || starRating.length() == 0)
				starRatingValue = 0.0F;
			else
				starRatingValue = calcStarValue();
			runTime = movieInfo != null && movieInfo.has("runTime") ? movieInfo.getInt("runTime") : 0;
			studio = movieInfo != null && movieInfo.has("origStudio") ? movieInfo.getString("origStudio") : null;
			countryOfOrigin = movieInfo != null && movieInfo.has("origCountry") ? movieInfo.getString("origCountry") : null;
			try {
				mpaaRating = movieInfo != null && movieInfo.has("mpaaRating") ? MPAARating.valueOf(movieInfo.getString("mpaaRating").replaceAll("-", "")) : MPAARating.NONE;
			} catch(IllegalArgumentException e) {
				LOG.warn(String.format("Unknown MPAARating encountered! [%s]", src.getString("mpaa_rating")));
				mpaaRating = MPAARating.UNKNOWN;
			}
			md5 = src.getString("md5");
			holiday = src.has("holiday") ? src.getString("holiday") : null;
			if(src.has("gameDatetime"))
				gameStart = Config.get().getDateTimeFormat().parse(src.getString("gameDatetime"));
			else
				gameStart = null;
			JSONObject descs = src.optJSONObject("descriptions");
			shortDescriptions = new String[3];
			if(descs != null) {
				description = descs.optString("description255");
				shortDescriptions[0] = descs.optString("description100");
				shortDescriptions[1] = descs.optString("description60");
				shortDescriptions[2] = descs.optString("description40");				
				alternateDescription = descs.optString("alternateDescription255");
				alternateDescriptionShort = descs.optString("alternateDescription100");
			} else {
				final String EMPTY = "";
				description = EMPTY;
				Arrays.fill(shortDescriptions, EMPTY);
				alternateDescription = EMPTY;
				alternateDescriptionShort = EMPTY;
			}
			if(src.has("castAndCrew")) {
				List<Credit> vals = new ArrayList<Credit>();
				JSONArray arr = src.getJSONArray("castAndCrew");
				for(int i = 0; i < arr.length(); ++i)
					vals.add(new Credit(arr.getJSONObject(i)));
				credits = vals.toArray(new Credit[0]);
			} else
				credits = new Credit[0];
			alternateTitle = src.optString("alternateTitle");
			alternateEpisodeNumber = src.has("alternateSyndicatedEpisodeNumber") ? src.getString("alternateSyndicatedEpisodeNumber") : null;
			if(src.has("advisories")) {
				JSONArray arr = src.getJSONArray("advisories");
				List<String> vals = new ArrayList<String>();
				for(int i = 0; i < arr.length(); ++i)
					vals.add(arr.getString(i));
				advisories = vals.toArray(new String[vals.size()]);
			} else
				advisories = new String[0];
			madeForTv = src.optBoolean("madeForTv");
			episodeTitle = src.optString("episodeTitle150");
			//TMSBUG: Is OAD actually optional or is this a bug in upstream data?
			String orig = src.optString("originalAirDate", "");
			originalAirDate = orig.length() > 0 && !orig.startsWith("0") ? ORIG_FMT.parse(src.getString("originalAirDate")) : null;
			descriptionLanguage = descs != null ? src.optString("descriptionLanguage", null) : null;
			String srcType = src.optString("sourceType").toUpperCase();
			try {
				sourceType = srcType.length() == 0 ? SourceType.NONE : SourceType.valueOf(srcType);
			} catch(IllegalArgumentException e) {
				if(WARNED_SRC_TYPES.add(srcType))
					LOG.warn(String.format("Unknown SourceType encountered! [%s]", srcType));
				sourceType = SourceType.UNKNOWN;
			}
			String showVal = src.optString("showType", ShowType.NONE.toString()).toUpperCase().replace(' ', '_');
			try {
				showType = showVal.length() == 0 ? ShowType.NONE : ShowType.valueOf(showVal);
			} catch(IllegalArgumentException e) {
				if(WARNED_SHOW_TYPES.add(showVal))
					LOG.warn(String.format("Unknown ShowType encountered! [%s]", showVal));
				showType = ShowType.UNKNOWN;
			}
			syndicatedEpisodeNumber = src.optString("syndicatedEpisodeNumber");
			String colorVal = src.optString("colorCode", ColorCode.NONE.toString()).toUpperCase().replaceAll(" & ", "").replaceAll(" +", "_");
			try {
				colorCode = colorVal.length() == 0 ? ColorCode.NONE : ColorCode.valueOf(colorVal);
			} catch(IllegalArgumentException e) {
				if(WARNED_COLOR_CODES.add(colorVal))
					LOG.warn(String.format("Unknown ColorCode encountered! [%s]", colorVal));
				colorCode = ColorCode.UNKNOWN;
			}
		} catch (JSONException | ParseException e) {
			throw new InvalidJsonObjectException(String.format("Program[%s]: %s", id, e.getMessage()), e, src.toString(3));
		}
	}

	/**
	 * @return A list of maps where each map in the list contains metadata from a given external data source (such as thetvdb.com); never null, possibly empty if no metadata is available for this program; keys of map depend on datasource value, see wiki for details of what's available for each type of metadata map
	 */
	public List<Map<String, Object>> getMetadata() {
		return metadata;
	}
	
	/**
	 * @return The list of genres for this program; never null but may be empty if genre data isn't available for this program
	 */
	public String[] getGenres() {
		return genres;
	}
	
	/**
	 * @return Returns the unique upstream id (i.e. Data Direct) for this program
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return True if this program was made for tv or false otherwise
	 */
	public boolean isMadeForTv() {
		return madeForTv;
	}

	/**
	 * @return Returns the full description of this program; may be empty, never null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the episode title for this program; may be empty, never null
	 */
	public String getEpisodeTitle() {
		return episodeTitle;
	}

	/**
	 * @return Returns the full title of this program
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the original airing date of this program or null if the data is not available
	 */
	public Date getOriginalAirDate() {
		return originalAirDate;
	}

	/**
	 * @return Returns the language that all the description fields are in or null if not provided by the source (assume English when null)
	 */
	public String getDescriptionLanguage() {
		return descriptionLanguage;
	}

	/**
	 * @return Returns the source type of this program
	 */
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @return Returns the show type of this program
	 */
	public ShowType getShowType() {
		return showType;
	}

	/**
	 * @return Returns the syndicated episode number or null if not available; this value is usually not too useful as there is no standard format
	 */
	public String getSyndicatedEpisodeNumber() {
		return syndicatedEpisodeNumber;
	}

	/**
	 * @return Returns the color code for this program
	 */
	public ColorCode getColorCode() {
		return colorCode;
	}

	/**
	 * @return Returns an array of additional advisories for this program; never null but may be empty
	 */
	public String[] getAdvisories() {
		return advisories;
	}

	/**
	 * @return Returns an alternate episode number, if available, or null; if a value is returned it's probably not going to be too useful (no standard format)
	 */
	public String getAlternateEpisodeNumber() {
		return alternateEpisodeNumber;
	}

	/**
	 * @return An alternate title or null
	 */
	public String getAlternateTitle() {
		return alternateTitle;
	}

	/**
	 * @return An array of credits for this program; never null but may be empty if no data is available
	 */
	public Credit[] getCredits() {
		return credits;
	}

	/**
	 * @return An alternate description or null
	 */
	public String getAlternateDescription() {
		return alternateDescription;
	}

	/**
	 * @return A shorter alternate description or null
	 */
	public String getAlternateDescriptionShort() {
		return alternateDescriptionShort;
	}

	/**
	 * @return An array of shorter descriptions for this program; sorted by longest to shortest; never null, never empty, but may just repeat the original description
	 */
	public String[] getShortDescriptions() {
		return shortDescriptions;
	}

	/**
	 * @return If this program refers to a sporting event, this will be the scheduled start time of the event itself; NOTE: currently this appears to represent the start time based on the event's local time zone, but that time zone isn't available so calculations are next to impossible
	 */
	public Date getGameStart() {
		return gameStart;
	}

	/**
	 * @return If this program is in reference to a holiday, this value specifies which holiday; null otherwise
	 */
	public String getHoliday() {
		return holiday;
	}

	/**
	 * @return The MD5 of this object, as computed by the UPSTREAM data source; currently no know way to validate this value
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * @return Returns the MPAA rating of this program
	 */
	public MPAARating getMpaaRating() {
		return mpaaRating;
	}

	/**
	 * @return Returns the program's country of origin or null
	 */
	public String getCountryOfOrigin() {
		return countryOfOrigin;
	}

	/**
	 * @return Returns the program's originating studio or null if not available
	 */
	public String getStudio() {
		return studio;
	}

	/**
	 * @return The running time of this program in seconds or 0 if not known
	 */
	public int getRunTime() {
		return runTime;
	}

	/**
	 * @return The star rating of this program or null if not available; only available for Programs with MV* ids
	 */
	public String getStarRating() {
		return starRating;
	}
	
	/**
	 * @return The star rating of this program, as a float; 0.0 if not available OR if the rating was indeed zero; when zero, check getStarRating() to see if it's null or not, null means there was no rating provided, empty string means zero rating (for movies)
	 */
	public float getStarRatingValue() {
		return starRatingValue;
	}
	
	/**
	 * Given a star rating from upstream, convert it to a number
	 * @return The value of the star rating received
	 * @throws ParseException If an invalid character is encountered in the received string
	 */
	protected float calcStarValue() throws ParseException {
		if(starRating == null || starRating.length() == 0) return 0.0F;
		float val = 0.0F;
		for(int i = 0; i < starRating.length(); ++i) {
			char c = starRating.charAt(i);
			switch(c) {
				case '*': val += 1.0F; break;
				case '+':
					val += 0.5F;
					if(i != starRating.length() - 1)
						throw new ParseException(String.format("Invalid format: can only be one '+' character and it must be last! [%s]", starRating), i);
					break;
				default: throw new ParseException(String.format("Invalid character in star rating! [%s]", Character.toString(c)), i);
			}
		}
		if(val > 4.0F)
			throw new ParseException(String.format("Star rating too high: %f [%s]", val, starRating), 0);
		return val;
	}

	/**
	 * @return Returns the episode number of this program or null if unknown; values returned are next to useless as there is no standard format for this value
	 */
	public String getEpisodeNumber() {
		return episodeNumber;
	}

	/**
	 * @return An array of alternate, shorter titles sorted from longest to shortest; never empty, never null, but may just repeat the original title
	 */
	public String[] getShortTitles() {
		return shortTitles;
	}

	/**
	 * @return Returns the year this program was made or zero if unknown; typically only provided for movies
	 */
	public int getYear() {
		return year;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 2;
		StringBuilder builder = new StringBuilder();
		builder.append("Program [id=");
		builder.append(id);
		builder.append(", madeForTv=");
		builder.append(madeForTv);
		builder.append(", description=");
		builder.append(description);
		builder.append(", episodeTitle=");
		builder.append(episodeTitle);
		builder.append(", title=");
		builder.append(title);
		builder.append(", originalAirDate=");
		builder.append(originalAirDate);
		builder.append(", descriptionLanguage=");
		builder.append(descriptionLanguage);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append(", showType=");
		builder.append(showType);
		builder.append(", syndicatedEpisodeNumber=");
		builder.append(syndicatedEpisodeNumber);
		builder.append(", colorCode=");
		builder.append(colorCode);
		builder.append(", advisories=");
		builder.append(advisories != null ? Arrays.asList(advisories).subList(
				0, Math.min(advisories.length, maxLen)) : null);
		builder.append(", alternateEpisodeNumber=");
		builder.append(alternateEpisodeNumber);
		builder.append(", alternateTitle=");
		builder.append(alternateTitle);
		builder.append(", credits=");
		builder.append(credits != null ? Arrays.asList(credits).subList(0,
				Math.min(credits.length, maxLen)) : null);
		builder.append(", alternateDescription=");
		builder.append(alternateDescription);
		builder.append(", alternateDescriptionShort=");
		builder.append(alternateDescriptionShort);
		builder.append(", shortDescriptions=");
		builder.append(shortDescriptions != null ? Arrays.asList(
				shortDescriptions).subList(0,
				Math.min(shortDescriptions.length, maxLen)) : null);
		builder.append(", gameStart=");
		builder.append(gameStart);
		builder.append(", holiday=");
		builder.append(holiday);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", mpaaRating=");
		builder.append(mpaaRating);
		builder.append(", countryOfOrigin=");
		builder.append(countryOfOrigin);
		builder.append(", studio=");
		builder.append(studio);
		builder.append(", runTime=");
		builder.append(runTime);
		builder.append(", starRating=");
		builder.append(starRating);
		builder.append(", starRatingValue=");
		builder.append(starRatingValue);
		builder.append(", episodeNumber=");
		builder.append(episodeNumber);
		builder.append(", shortTitles=");
		builder.append(shortTitles != null ? Arrays.asList(shortTitles)
				.subList(0, Math.min(shortTitles.length, maxLen)) : null);
		builder.append(", year=");
		builder.append(year);
		builder.append(", genres=");
		builder.append(genres != null ? Arrays.asList(genres).subList(0,
				Math.min(genres.length, maxLen)) : null);
		builder.append(", metadata=");
		builder.append(metadata != null ? metadata.subList(0,
				Math.min(metadata.size(), maxLen)) : null);
		builder.append(", seriesDescription=");
		builder.append(seriesDescription);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param madeForTv the madeForTv to set
	 */
	public void setMadeForTv(boolean madeForTv) {
		this.madeForTv = madeForTv;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param episodeTitle the episodeTitle to set
	 */
	public void setEpisodeTitle(String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param originalAirDate the originalAirDate to set
	 */
	public void setOriginalAirDate(Date originalAirDate) {
		this.originalAirDate = originalAirDate;
	}

	/**
	 * @param descriptionLanguage the descriptionLanguage to set
	 */
	public void setDescriptionLanguage(String descriptionLanguage) {
		this.descriptionLanguage = descriptionLanguage;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * @param showType the showType to set
	 */
	public void setShowType(ShowType showType) {
		this.showType = showType;
	}

	/**
	 * @param syndicatedEpisodeNumber the syndicatedEpisodeNumber to set
	 */
	public void setSyndicatedEpisodeNumber(String syndicatedEpisodeNumber) {
		this.syndicatedEpisodeNumber = syndicatedEpisodeNumber;
	}

	/**
	 * @param colorCode the colorCode to set
	 */
	public void setColorCode(ColorCode colorCode) {
		this.colorCode = colorCode;
	}

	/**
	 * @param advisories the advisories to set
	 */
	public void setAdvisories(String[] advisories) {
		this.advisories = advisories;
	}

	/**
	 * @param alternateEpisodeNumber the alternateEpisodeNumber to set
	 */
	public void setAlternateEpisodeNumber(String alternateEpisodeNumber) {
		this.alternateEpisodeNumber = alternateEpisodeNumber;
	}

	/**
	 * @param alternateTitle the alternateTitle to set
	 */
	public void setAlternateTitle(String alternateTitle) {
		this.alternateTitle = alternateTitle;
	}

	/**
	 * @param credits the credits to set
	 */
	public void setCredits(Credit[] credits) {
		this.credits = credits;
	}

	/**
	 * @param alternateDescription the alternateDescription to set
	 */
	public void setAlternateDescription(String alternateDescription) {
		this.alternateDescription = alternateDescription;
	}

	/**
	 * @param alternateDescriptionShort the alternateDescriptionShort to set
	 */
	public void setAlternateDescriptionShort(String alternateDescriptionShort) {
		this.alternateDescriptionShort = alternateDescriptionShort;
	}

	/**
	 * @param shortDescriptions the shortDescriptions to set
	 */
	public void setShortDescriptions(String[] shortDescriptions) {
		this.shortDescriptions = shortDescriptions;
	}

	/**
	 * @param gameStart the gameStart to set
	 */
	public void setGameStart(Date gameStart) {
		this.gameStart = gameStart;
	}

	/**
	 * @param holiday the holiday to set
	 */
	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}

	/**
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}

	/**
	 * @param mpaaRating the mpaaRating to set
	 */
	public void setMpaaRating(MPAARating mpaaRating) {
		this.mpaaRating = mpaaRating;
	}

	/**
	 * @param countryOfOrigin the countryOfOrigin to set
	 */
	public void setCountryOfOrigin(String countryOfOrigin) {
		this.countryOfOrigin = countryOfOrigin;
	}

	/**
	 * @param studio the studio to set
	 */
	public void setStudio(String studio) {
		this.studio = studio;
	}

	/**
	 * @param runTime the runTime to set
	 */
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	/**
	 * @param starRating the starRating to set
	 * @throws ParseException Thrown if the arugment is not a valid star rating string
	 */
	public void setStarRating(String starRating) throws ParseException {
		this.starRating = starRating;
		starRatingValue = calcStarValue();
	}

	/**
	 * @param episodeNumber the episodeNumber to set
	 */
	public void setEpisodeNumber(String episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	/**
	 * @param shortTitles the shortTitles to set
	 */
	public void setShortTitles(String[] shortTitles) {
		this.shortTitles = shortTitles;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @param genres the genres to set
	 */
	public void setGenres(String[] genres) {
		this.genres = genres;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(List<Map<String, Object>> metadata) {
		this.metadata = metadata;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Program)) {
			return false;
		}
		Program other = (Program) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the seriesDescription
	 */
	public String getSeriesDescription() {
		return seriesDescription;
	}

	/**
	 * @param seriesDescription the seriesDescription to set
	 */
	public void setSeriesDescription(String seriesDescription) {
		this.seriesDescription = seriesDescription;
	}
}
