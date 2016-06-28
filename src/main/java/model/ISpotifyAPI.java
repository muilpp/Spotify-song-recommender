package model;

import java.util.List;

import model.webservice_data.PlaylistDTO;
import model.webservice_data.RecommendationDTO;
import model.webservice_data.Token;
import model.webservice_data.TopShortTermTracksDTO;
import model.webservice_data.TrackURI;

public interface ISpotifyAPI {
    public void getUsersTopTracks();
    public TopShortTermTracksDTO getShortTermTopTracks(final String bearer);
    public String getPlaylistId(final String bearer);
    public PlaylistDTO getUserPlaylists(final String bearer);
    public String getUserId(final String bearer);
    public String createPlaylist(final String bearer, final String userId);
    public int replaceOldSongsInPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI);
    public int addNewSongsToPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI);
    public List<TrackURI> createUriTrackList(RecommendationDTO recs);
    /**
     * Requests an access token and a refresh token for the user
     * @param authorizationCode
     * @return a Token with the access, refresh and expiration time if authCode works, empty Token otherwise
     */
    public Token requestToken(String authorizationCode);
    
    /**
     * Request a new access token when the current one is expired
     * @param refreshToken
     * @return a new Token with the access if refreshToken works, empty Token otherwise
     */
    public Token refreshToken(String refreshToken);
    
    /**
     * Request the profile user name
     * @return the user name if found
     */
    public String getSpotifyUserName(String bearer);
}