package model;

public interface Constant {
    public final static String CLIENT_ID = "";
    public final static String CLIENT_SECRET = "";
    public final static String TIME_RANGE = "time_range";
    public final static String SEED_TRACKS = "seed_tracks";
    public final static String MARKET = "market=US";
    public final static String LIMIT = "limit";
    public final static int MAX_SONGS_TO_ADD_PER_REQUEST = 100;
    public final static String DEFAULT_TOP_TRACKS_LIMIT = "10";
    public final static String DEFAULT_RECS_LIMIT = "50";
    public final static int STEP_SIZE_FOR_RECS = 2;
    public final static String PLAYLIST_NAME = "Weekly Suggestions";
    public final static String SPOTIFY_TRACK = "spotify:track:";
    public final static String AUTHORIZATION_CODE = "authorization_code";
    public final static String REDIRECT_URI = "http://www.spotifyrecommender.xyz";

//    Webservice params keys
    public final static String REFRESH_TOKEN_KEY = "refresh_token";
    public final static String GRANT_TYPE_KEY = "grant_type";
    public final static String CODE_KEY = "code";
    public final static String REDIRECT_URI_KEY = "redirect_uri";
    public final static String CLIENT_ID_KEY = "client_id";
    public final static String CLIENT_SECRET_KEY = "client_secret";
    
//    Quartz
    public final static String EXECUTE_USER_JOB = "executeUserJob";
    public final static String JOB_MANAGER_GROUP = "jobManagerGroup";
    public final static String EXECUTE_USER_TRIGGER = "executeUserTrigger";
    public final static String USER_SCHEDULE = "userSchedule";
}