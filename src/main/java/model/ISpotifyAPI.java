package model;

import java.util.List;

import model.webservice_data.PlaylistDTO;
import model.webservice_data.RecommendationDTO;
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
}