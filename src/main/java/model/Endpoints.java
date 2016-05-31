package model;

import static model.Constant.DEFAULT_RECS_LIMIT;
import static model.Constant.DEFAULT_TOP_TRACKS_LIMIT;
import static model.Constant.LIMIT;
import static model.Constant.MARKET;

import model.enumeration.TimeRange;

public final class Endpoints {
    public final static String BASE_URL = "https://api.spotify.com/v1";
    public final static String TOP_TRACKS_EP = "/me/top/tracks";
    public final static String RECOMMENDATIONS_EP = "/recommendations";
    public final static String PLAYLISTS_EP = "/me/playlists";
    public final static String USER_PROFILE_EP = "/me";
    public final static String CREATE_PLAYLIST_EP = "/users/{userId}/playlists";
    public final static String REPLACE_SONGS_IN_PLAYLIST_EP = "/users/{userId}/playlists/{playlistId}/tracks";
    public final static String ADD_SONGS_IN_PLAYLIST_EP = "/users/{userId}/playlists/{playlistId}/tracks";

    private Endpoints() {}

    public static String buildURIForShortTermTopTracks() {
        return BASE_URL + TOP_TRACKS_EP + "?" + Constant.TIME_RANGE + "=" + TimeRange.short_term + "&" + LIMIT + "=" + DEFAULT_TOP_TRACKS_LIMIT;
    }

    public static String buildURIForRecommendations(String songIds) {
        return BASE_URL + RECOMMENDATIONS_EP + "?" + Constant.SEED_TRACKS + "=" + songIds + "&" + MARKET + "&" + LIMIT + "=" + DEFAULT_RECS_LIMIT;
    }

    public static String buildURIToGetUserPlaylists() {
        return BASE_URL + PLAYLISTS_EP;
    }

    public static String buildURIToGetUserProfile() {
        return BASE_URL + USER_PROFILE_EP;
    }

    public static String buildURIToCreatePlaylist(String userId) {
        return BASE_URL + CREATE_PLAYLIST_EP.replace("{userId}", userId);
    }

    public static String buildURIToReplaceOldSongs(String userId, String playlistId) {
        return BASE_URL + REPLACE_SONGS_IN_PLAYLIST_EP.replace("{userId}", userId).replace("{playlistId}", playlistId);
    }
    
    public static String buildURIToAddNewSongs(String userId, String playlistId) {
        return BASE_URL + ADD_SONGS_IN_PLAYLIST_EP.replace("{userId}", userId).replace("{playlistId}", playlistId);
    }
}