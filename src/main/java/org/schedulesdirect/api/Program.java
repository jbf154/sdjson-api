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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.schedulesdirect.api.exception.InvalidJsonObjectException;
import org.schedulesdirect.api.exception.SilentInvalidJsonObjectException;

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
	static private final Set<String> WARNED_ROLES = new HashSet<>();
	
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
		ASSISTANT_DIRECTOR,
		EXECUTIVE_PRODUCER,
		GUEST_STAR,
		GUEST,
		HOST,
		JUDGE,
		MUSICAL_GUEST,
		NARRATOR,
		PRODUCER,
		WRITER,
		COSTUME_DESIGNER,
		SET_DECORATION,
		ART_DIRECTION,
		PRODUCTION_DESIGNER,
		CASTING,
		FILM_EDITOR,
		CINEMATOGRAPHER,
		ORIGINAL_MUSIC,
		ASSOCIATE_PRODUCER,
		CASTING_DIRECTOR,
		COMPOSER,
		VOICE,
		PRODUCTION_MANAGER,
		DIRECTORY_OF_PHOTOGRAPHY,
		VISUAL_EFFECTS
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
		private int billingOrder;
		private String personId;
		private String nameId;
		private String characterName;
	
		private Credit(JSONObject src) throws JSONException {
			Role r = null;
			String roleStr = src.getString("role");
			if(roleStr.startsWith("Writer") || roleStr.contains("Screenwriter"))
				roleStr = "Writer";
			else if(roleStr.contains("Assistant Director"))
				roleStr = "Assistant Director";
			else if(roleStr.contains("Producer"))
				roleStr = "Producer";
			else if(roleStr.contains("Art Director"))
				roleStr = "Art Direction";
			else if(roleStr.contains("Production Design"))
				roleStr = "Production Designer";
			else if(roleStr.contains("Visual Effects"))
				roleStr = "Visual Effects";
			String name = src.getString("name");
			try {
				r = Role.valueOf(roleStr.toUpperCase().replace(' ', '_'));
			} catch(IllegalArgumentException e) {
				if(WARNED_ROLES.add(roleStr))
					LOG.warn(String.format("Unknown Role encountered! [%s]", roleStr));
				r = Role.UNKNOWN;
			}
			role = r;
			this.name = name;
			billingOrder = Integer.parseInt(src.optString("billingOrder", "0"));
			personId = src.optString("personId", null);
			nameId = src.optString("nameId", null);
			characterName = src.optString("characterName", null);
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
			return "Credit [personId=" + personId + ", nameId=" + nameId
					+ ", billingOrder=" + billingOrder + ", name=" + name
					+ ", role=" + role + ", characterName=" + characterName
					+ "]";
		}

		/**
		 * @return the billingOrder
		 */
		public int getBillingOrder() {
			return billingOrder;
		}

		/**
		 * @return the personId
		 */
		public String getPersonId() {
			return personId;
		}

		/**
		 * @return the nameId
		 */
		public String getNameId() {
			return nameId;
		}

		/**
		 * @return the characterName
		 */
		public String getCharacterName() {
			return characterName;
		}
	}
	
	static public class Team {

		private String name;
		private boolean isHome;

		/**
		 * @param name
		 * @param isHome
		 */
		protected Team(String name, boolean isHome) {
			this.name = name;
			this.isHome = isHome;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the isHome
		 */
		public boolean isHome() {
			return isHome;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Team [name=" + name + ", isHome=" + isHome + "]";
		}
	}
	
	/**
	 * Represents a movie quality rating (i.e. star rating, "3 out of 4 stars")
	 * @author Derek Battams &lt;derek@battams.ca&gt;
	 *
	 */
	static public abstract class QualityRating {
		private String source;
		private String units;
		
		private QualityRating(String source, String units) {
			this.source = source;
			this.units = units;
		}

		/**
		 * @return the ratings body (i.e. critic, TMS, etc.); null if not known
		 */
		public String getSource() {
			return source;
		}
		
		/**
		 * @return the unit of measure this rating is in (i.e. "stars", etc.)
		 */
		public String getUnits() {
			return units;
		}

		abstract public Object getRating();
		abstract public Object getMinRating();
		abstract public Object getMaxRating();
		abstract public Object getIncrement();
	}
	
	static public class StringQualityRating extends QualityRating {

		private String rating;
		private String minRating;
		private String maxRating;
		private String increment;
		
		protected StringQualityRating(JSONObject src, String units) throws InvalidJsonObjectException {
			super(src.optString("ratingsBody", null), units);
			try {
				rating = src.getString("rating");
				minRating = src.optString("minRating", null);
				maxRating = src.optString("maxRating", null);
				increment = src.optString("increment", null);
			} catch(JSONException e) {
				throw new SilentInvalidJsonObjectException(e);
			}
		}

		/**
		 * @return the rating
		 */
		public String getRating() {
			return rating;
		}

		/**
		 * @return the minRating
		 */
		public String getMinRating() {
			return minRating;
		}

		/**
		 * @return the maxRating
		 */
		public String getMaxRating() {
			return maxRating;
		}

		/**
		 * @return the increment
		 */
		public String getIncrement() {
			return increment;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s", rating, getUnits());
		}		
	}
	
	static public class FloatQualityRating extends QualityRating {
		
		private float rating;
		private float minRating;
		private float maxRating;
		private float increment;
		
		protected FloatQualityRating(JSONObject src, String units) {
			super(src.optString("ratingsBody", null), units);
			rating = Float.parseFloat(src.getString("rating"));
			minRating = Float.parseFloat(src.optString("minRating", Float.toString(Float.MIN_VALUE)));
			maxRating = Float.parseFloat(src.optString("maxRating", Float.toString(Float.MIN_VALUE)));
			increment = Float.parseFloat(src.optString("increment", Float.toString(Float.MIN_VALUE)));
		}

		@Override
		public Float getRating() {
			return rating;
		}

		@Override
		public Float getMinRating() {
			return minRating;
		}

		@Override
		public Float getMaxRating() {
			return maxRating;
		}

		@Override
		public Float getIncrement() {
			return increment;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(String.format("%.1f", rating));
			if(maxRating > Float.MIN_VALUE)
				sb.append(String.format("/%.1f", maxRating));
			sb.append(String.format(" %s", getUnits()));
			return sb.toString();
		}
	}

	static private String parseDesc(JSONArray src) {
		return src != null ? src.getJSONObject(findEnDescIndex(src)).getString("description") : "";
	}
	
	static private int findEnDescIndex(JSONArray src) {
		int retVal = 0;
		if(src.length() > 1) {
			for(int i = 0; i < src.length(); ++i) {
				JSONObject o = src.getJSONObject(i);
				if(o.getString("descriptionLanguage").equals("en")) {
					retVal = i;
					break;
				}
			}
		}
		return retVal;
	}
	
	static public String convertToSeriesId(String id) {
		if(id == null || !id.startsWith("EP"))
			throw new IllegalArgumentException("Invalid id!");
		return "SH" + id.substring(2, id.length() - 4) + "0000";
	}
	
	static public final Pattern MOVIE_REGEX = Pattern.compile("Feature Film|.*Movie");
	
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
	private ContentRating[] ratings;
	private String countryOfOrigin;
	private String studio;
	private int runTime;
	private QualityRating[] qualityRatings;
	private String episodeNumber;
	private String[] shortTitles;
	private int year;
	private String[] genres;
	private List<Map<String, Object>> metadata;
	private String seriesDescription;
	private String venue;
	private Team[] teams;
	private URL[] images;
	private Program seriesInfo;
	
	/**
	 * Consutrctor
	 * @param src The JSON object from which this instance is being constructed; cannot be null
	 * @param epgClnt The EpgClient instance to associate with this Program instance
	 * @throws InvalidJsonObjectException Thrown if the given src is not in the expected format
	 * @throws IllegalArgumentException Thrown if src is null
	 */
	Program(JSONObject src, EpgClient clnt) throws InvalidJsonObjectException {
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
			JSONObject movieInfo = src.optJSONObject("movie");
			if(movieInfo != null && movieInfo.has("year"))
				year = Integer.parseInt(movieInfo.get("year").toString());
			else
				year = 0;
			JSONObject titles = src.getJSONObject("titles");
			title = titles.getString("title120");
			shortTitles = new String[4];
			shortTitles[0] = titles.optString("title70");
			shortTitles[1] = titles.optString("title40");
			shortTitles[2] = titles.optString("title20");
			shortTitles[3] = titles.optString("title10");
			episodeNumber = src.optString("syndicatedEpisodeNumber");
			Collection<QualityRating> ratingObjs = new ArrayList<>();
			if(movieInfo != null && movieInfo.has("qualityRating")) {
				JSONArray ratings = movieInfo.getJSONArray("qualityRating");
				for(int i = 0; i < ratings.length(); ++i) {
					JSONObject r = ratings.getJSONObject(i);
					if("TMS".equals(r.optString("ratingsBody")))
						ratingObjs.add(new FloatQualityRating(r, "stars"));
					else
						ratingObjs.add(new StringQualityRating(r, "rating"));
				}
			}
			qualityRatings = ratingObjs.toArray(new QualityRating[0]);
			runTime = movieInfo != null && movieInfo.has("runTime") ? movieInfo.getInt("runTime") : 0;
			studio = movieInfo != null && movieInfo.has("origStudio") ? movieInfo.getString("origStudio") : null;
			countryOfOrigin = movieInfo != null && movieInfo.has("origCountry") ? movieInfo.getString("origCountry") : null;
			JSONArray ratings = src.optJSONArray("contentRating");
			if(ratings != null) {
				Collection<ContentRating> coll = new ArrayList<ContentRating>();
				for(int i = 0; i < ratings.length(); ++i) {
					JSONObject o = ratings.getJSONObject(i);
					coll.add(new ContentRating(o.getString("body"), o.getString("code")));
				}
				this.ratings = coll.toArray(new ContentRating[0]);
			} else
				this.ratings = new ContentRating[0];
			md5 = src.getString("md5");
			holiday = src.has("holiday") ? src.getString("holiday") : null;
			if(src.has("gameDatetime"))
				gameStart = Config.get().getDateTimeFormat().parse(src.getString("gameDatetime"));
			else
				gameStart = null;
			JSONObject descs = src.optJSONObject("descriptions");
			shortDescriptions = new String[4];
			if(descs != null) {
				description = parseDesc(descs.optJSONArray("description1000"));
				shortDescriptions[0] = parseDesc(descs.optJSONArray("description255"));
				shortDescriptions[1] = parseDesc(descs.optJSONArray("description100"));
				shortDescriptions[2] = parseDesc(descs.optJSONArray("description60"));
				shortDescriptions[3] = parseDesc(descs.optJSONArray("description40"));
				Arrays.sort(shortDescriptions, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						if(o1 == null && o2 != null)
							return 1;
						else if(o1 != null && o2 == null)
							return -1;
						else if(o1 == null && o2 == null)
							return 0;
						else
							return o2.length() - o1.length();
					}
					
				});
				if(description.length() == 0 && shortDescriptions[0].length() > 0)
					description = shortDescriptions[0];
				alternateDescription = parseDesc(descs.optJSONArray("alternateDescription255"));
				alternateDescriptionShort = parseDesc(descs.optJSONArray("alternateDescription100"));
			} else {
				final String EMPTY = "";
				description = EMPTY;
				Arrays.fill(shortDescriptions, EMPTY);
				alternateDescription = EMPTY;
				alternateDescriptionShort = EMPTY;
			}
			Collection<Credit> castAndCrew = new ArrayList<>();
			if(src.has("cast")) {
				JSONArray cast = src.getJSONArray("cast");
				for(int i = 0; i < cast.length(); ++i)
					castAndCrew.add(new Credit(cast.getJSONObject(i)));
			}
			if(src.has("crew")) {
				JSONArray crew = src.getJSONArray("crew");
				for(int i = 0; i < crew.length(); ++i)
					castAndCrew.add(new Credit(crew.getJSONObject(i)));				
			}
			credits = castAndCrew.toArray(new Credit[0]);
			
			alternateTitle = src.optString("alternateTitle");
			alternateEpisodeNumber = src.has("alternateSyndicatedEpisodeNumber") ? src.getString("alternateSyndicatedEpisodeNumber") : null;
			if(src.has("contentAdvisory")) {
				JSONArray arr = src.getJSONArray("contentAdvisory");
				List<String> vals = new ArrayList<String>();
				for(int i = 0; i < arr.length(); ++i)
					vals.add(arr.getString(i));
				advisories = vals.toArray(new String[vals.size()]);
			} else
				advisories = new String[0];
			madeForTv = src.optBoolean("madeForTv");
			episodeTitle = src.optString("episodeTitle150");
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
			Set<String> genreVals = new LinkedHashSet<>();
			String showTypeVal = src.optString("showType");
			if(showTypeVal != null && !"Series".equals(showTypeVal)) {
				if(MOVIE_REGEX.matcher(showTypeVal).matches())
					showTypeVal = "Movie";
				genreVals.add(showTypeVal);
			}
			if(src.has("genres")) {
				JSONArray arr = src.getJSONArray("genres");
				for(int i = 0; i < arr.length(); ++i)
					genreVals.add(arr.getString(i));
			}
			genres = genreVals.toArray(new String[0]);
			syndicatedEpisodeNumber = src.optString("syndicatedEpisodeNumber");
			String colorVal = src.optString("colorCode", ColorCode.NONE.toString()).toUpperCase().replaceAll(" & ", "").replaceAll(" +", "_");
			try {
				colorCode = colorVal.length() == 0 ? ColorCode.NONE : ColorCode.valueOf(colorVal);
			} catch(IllegalArgumentException e) {
				if(WARNED_COLOR_CODES.add(colorVal))
					LOG.warn(String.format("Unknown ColorCode encountered! [%s]", colorVal));
				colorCode = ColorCode.UNKNOWN;
			}
			JSONObject event = src.optJSONObject("eventDetails");
			if(event != null) {
				List<Team> list = new ArrayList<>();
				JSONArray teams = event.optJSONArray("teams");
				if(teams != null)
					for(int i = 0; i < teams.length(); ++i) {
						JSONObject team = teams.getJSONObject(i);
						list.add(new Team(team.getString("name"), team.optBoolean("isHome")));
					}
				this.teams = list.toArray(new Team[0]);
				venue = event.optString("venue", null);
			} else {
				teams = new Team[0];
				venue = null;
			}
			JSONArray images = src.optJSONArray("images");
			if(images != null) {
				Collection<URL> urls = new ArrayList<>();
				for(int i = 0; i < images.length(); ++i) {
					JSONObject o = images.getJSONObject(i);
					String url = o.getString("uri");
					if(!url.matches("^https?:\\/\\/.*"))
						urls.add(new URL(String.format("%s/%s/%s", clnt.getBaseUrl(), EpgClient.API_VERSION, url)));
					else
						urls.add(new URL(url));
				}
				this.images = urls.toArray(new URL[0]);
			}
			seriesInfo = id.startsWith("EP") ? clnt.fetchProgram(convertToSeriesId(id)) : null;
		} catch (Throwable t) {
			throw new InvalidJsonObjectException(String.format("Program[%s]: %s", id, t.getMessage()), t, src.toString(3));
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
	 * @return Returns the ratings of this program
	 */
	public ContentRating[] getRatings() {
		return ratings;
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
		return "Program [id="
				+ id
				+ ", madeForTv="
				+ madeForTv
				+ ", description="
				+ description
				+ ", episodeTitle="
				+ episodeTitle
				+ ", title="
				+ title
				+ ", originalAirDate="
				+ originalAirDate
				+ ", descriptionLanguage="
				+ descriptionLanguage
				+ ", sourceType="
				+ sourceType
				+ ", syndicatedEpisodeNumber="
				+ syndicatedEpisodeNumber
				+ ", colorCode="
				+ colorCode
				+ ", advisories="
				+ (advisories != null ? Arrays.asList(advisories).subList(0,
						Math.min(advisories.length, maxLen)) : null)
				+ ", alternateEpisodeNumber="
				+ alternateEpisodeNumber
				+ ", alternateTitle="
				+ alternateTitle
				+ ", credits="
				+ (credits != null ? Arrays.asList(credits).subList(0,
						Math.min(credits.length, maxLen)) : null)
				+ ", alternateDescription="
				+ alternateDescription
				+ ", alternateDescriptionShort="
				+ alternateDescriptionShort
				+ ", shortDescriptions="
				+ (shortDescriptions != null ? Arrays.asList(shortDescriptions)
						.subList(0, Math.min(shortDescriptions.length, maxLen))
						: null)
				+ ", gameStart="
				+ gameStart
				+ ", holiday="
				+ holiday
				+ ", md5="
				+ md5
				+ ", ratings="
				+ (ratings != null ? Arrays.asList(ratings).subList(0,
						Math.min(ratings.length, maxLen)) : null)
				+ ", countryOfOrigin="
				+ countryOfOrigin
				+ ", studio="
				+ studio
				+ ", runTime="
				+ runTime
				+ ", qualityRatings="
				+ (qualityRatings != null ? Arrays.asList(qualityRatings)
						.subList(0, Math.min(qualityRatings.length, maxLen))
						: null)
				+ ", episodeNumber="
				+ episodeNumber
				+ ", shortTitles="
				+ (shortTitles != null ? Arrays.asList(shortTitles).subList(0,
						Math.min(shortTitles.length, maxLen)) : null)
				+ ", year="
				+ year
				+ ", genres="
				+ (genres != null ? Arrays.asList(genres).subList(0,
						Math.min(genres.length, maxLen)) : null)
				+ ", metadata="
				+ (metadata != null ? metadata.subList(0,
						Math.min(metadata.size(), maxLen)) : null)
				+ ", seriesDescription="
				+ seriesDescription
				+ ", venue="
				+ venue
				+ ", teams="
				+ (teams != null ? Arrays.asList(teams).subList(0,
						Math.min(teams.length, maxLen)) : null)
				+ ", images="
				+ (images != null ? Arrays.asList(images).subList(0,
						Math.min(images.length, maxLen)) : null)
				+ ", seriesInfo=" + seriesInfo + "]";
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
	public void setRatings(ContentRating[] ratings) {
		this.ratings = ratings;
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

	/**
	 * @return the venue; typically only provided for (some) sports event programs or null otherwise
	 */
	public String getVenue() {
		return venue;
	}

	/**
	 * @param venue the venue to set
	 */
	public void setVenue(String venue) {
		this.venue = venue;
	}

	/**
	 * @return the teams; identifies teams competing in a sports event program; will be an empty array when not available/not applicable
	 */
	public Team[] getTeams() {
		return teams;
	}

	/**
	 * @param teams the teams to set
	 */
	public void setTeams(Team[] teams) {
		this.teams = teams;
	}

	/**
	 * <p>
	 *   The URLs returned by this method must be inspected by the caller and acted upon differently
	 *   based on whether or not the URL starts with <code>EpgClient.getBaseUrl()</code>.
	 *   If the URL does start with the value of <code>getBaseUrl()</code> then in order to access
	 *   the image, you <b>must</b> route the request thru an instance of <code>NetworkEpgClient</code>
	 *   in order to ensure the request is properly authenticated.  URLs that don't point to the JSON
	 *   service do not need to be authenticated and can just be accessed directly with your favourite
	 *   URL reader (including <code>URL.openStream()</code>).
	 * </p>
	 * @return URLs representing the various fanart images available for this program
	 */
	public URL[] getImages() {
		return images;
	}

	/**
	 * @param images the images to set
	 */
	public void setImages(URL[] images) {
		this.images = images;
	}

	/**
	 * @return the qualityRatings
	 */
	public QualityRating[] getQualityRatings() {
		return qualityRatings;
	}

	/**
	 * @param qualityRatings the qualityRatings to set
	 */
	public void setQualityRatings(QualityRating[] qualityRatings) {
		this.qualityRatings = qualityRatings;
	}

	/**
	 * @return the seriesInfo
	 */
	public Program getSeriesInfo() {
		return seriesInfo;
	}

	/**
	 * @param seriesInfo the seriesInfo to set
	 */
	public void setSeriesInfo(Program seriesInfo) {
		this.seriesInfo = seriesInfo;
	}
}
