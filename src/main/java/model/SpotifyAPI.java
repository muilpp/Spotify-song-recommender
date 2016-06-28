package model;

import static model.Constant.AUTHORIZATION_CODE;
import static model.Constant.CLIENT_ID;
import static model.Constant.CLIENT_ID_KEY;
import static model.Constant.CLIENT_SECRET;
import static model.Constant.CLIENT_SECRET_KEY;
import static model.Constant.CODE_KEY;
import static model.Constant.GRANT_TYPE_KEY;
import static model.Constant.MAX_SONGS_TO_ADD_PER_REQUEST;
import static model.Constant.PLAYLIST_NAME;
import static model.Constant.REDIRECT_URI;
import static model.Constant.REDIRECT_URI_KEY;
import static model.Constant.REFRESH_TOKEN_KEY;
import static model.Constant.SPOTIFY_TRACK;
import static model.Endpoints.buildURIForShortTermTopTracks;
import static model.Endpoints.buildURIToAddNewSongs;
import static model.Endpoints.buildURIToCreatePlaylist;
import static model.Endpoints.buildURIToGetUserPlaylists;
import static model.Endpoints.buildURIToGetUserProfile;
import static model.Endpoints.buildURIToReplaceOldSongs;
import static model.Endpoints.buildURIToRequestToken;
import static model.Endpoints.buildURIToRequestUserProfileName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import jersey.repackaged.com.google.common.collect.Lists;
import model.webservice_data.CreatedPlaylistDTO;
import model.webservice_data.PlaylistDTO;
import model.webservice_data.PlaylistItem;
import model.webservice_data.RecommendationDTO;
import model.webservice_data.Token;
import model.webservice_data.TopShortTermTracksDTO;
import model.webservice_data.Track;
import model.webservice_data.TrackURI;
import model.webservice_data.UserProfile;
import model.webservice_data.UserProfileDTO;

public class SpotifyAPI implements ISpotifyAPI{
    private final Logger LOGGER = Logger.getLogger(SpotifyAPI.class.getName());

    @Override
    public void getUsersTopTracks() {
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target(buildURIForShortTermTopTracks());

        Response response = webTarget.request().get();
        
        if (response.getStatus() != 200)
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
    }

    @Override
    public TopShortTermTracksDTO getShortTermTopTracks(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget shortTermsTracksTarget = client.target(buildURIForShortTermTopTracks());

        Response response = shortTermsTracksTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));            
        } else
            return response.readEntity(TopShortTermTracksDTO.class);

        return new TopShortTermTracksDTO();
    }

    @Override
    public String getPlaylistId(final String bearer) {
        PlaylistDTO playlistDTO = getUserPlaylists(bearer);

        for (PlaylistItem playlistItem : playlistDTO.getPlaylistItemList()) {
            if (playlistItem.getPlaylistName().equalsIgnoreCase(PLAYLIST_NAME)) {
                return playlistItem.getPlaylistId();
            }
        }
        
        return null;
    }
    
    @Override
    public PlaylistDTO getUserPlaylists(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget userPlaylistsTarget = client.target(buildURIToGetUserPlaylists());

        Response response = userPlaylistsTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return response.readEntity(PlaylistDTO.class);

        return new PlaylistDTO();
    }

    @Override
    public String getUserId(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget userProfileTarget = client.target(buildURIToGetUserProfile());

        Response response = userProfileTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else {
            UserProfileDTO userProfile = response.readEntity(UserProfileDTO.class);
            return userProfile.getUserId();
        }

        return null;
    }

    @Override
    public String createPlaylist(final String bearer, final String userId) {
        Client client = ClientBuilder.newClient();

        WebTarget createPlaylistTarget = client.target(buildURIToCreatePlaylist(userId));

        PlaylistItem playlist = new PlaylistItem(PLAYLIST_NAME, true);

        Response response = createPlaylistTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .post(Entity.entity(playlist, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return response.readEntity(CreatedPlaylistDTO.class).getPlaylistId();

        return null;
    }

    @Override
    public int replaceOldSongsInPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI) {
        Client client = ClientBuilder.newClient();

        WebTarget replacePlaylistTarget = client.target(buildURIToReplaceOldSongs(userId, playlistId));

        Response response = replacePlaylistTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .put(Entity.entity(trackURI, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return trackURI.getURISet().size();

        return 0;
    }

    @Override
    public int addNewSongsToPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI) {
        Client client = ClientBuilder.newClient();

        WebTarget addNewSongsTarget = client.target(buildURIToAddNewSongs(userId, playlistId));

        Response response = addNewSongsTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .post(Entity.entity(trackURI, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return trackURI.getURISet().size();

        return 0;
    }

    @Override
    public List<TrackURI> createUriTrackList(RecommendationDTO recs) {
        if (recs.getTrackList().isEmpty())
            return Collections.emptyList();

        List<TrackURI> trackURIList = new ArrayList<>();
        if (recs.getTrackList().size() > MAX_SONGS_TO_ADD_PER_REQUEST) {
            List<List<Track>> trackSublist = Lists.partition(recs.getTrackList(), MAX_SONGS_TO_ADD_PER_REQUEST);

            for (int i=0; i<trackSublist.size(); i++) {
                TrackURI trackURI = new TrackURI();
                for (Track track : trackSublist.get(i)) {
                    trackURI.getURISet().add(SPOTIFY_TRACK + track.getSongId());
                }
                trackURIList.add(trackURI);
            }
        } else {
            TrackURI trackURI = new TrackURI();
            for (Track track : recs.getTrackList()) {
                trackURI.getURISet().add(SPOTIFY_TRACK + track.getSongId());
            }

            trackURIList.add(trackURI);
        }

        return trackURIList;
    }

    @Override
    public Token requestToken(String authorizationCode) {
            
        Client client = ClientBuilder.newClient();

        WebTarget requestNewTokenTarget = client.target(buildURIToRequestToken());

        MultivaluedMap<String, String> authData = new MultivaluedHashMap<String, String>();
        authData.add(GRANT_TYPE_KEY, AUTHORIZATION_CODE);
        authData.add(CODE_KEY, authorizationCode);
        authData.add(REDIRECT_URI_KEY, REDIRECT_URI);
        authData.add(CLIENT_ID_KEY, CLIENT_ID);
        authData.add(CLIENT_SECRET_KEY, CLIENT_SECRET);

        Response response = requestNewTokenTarget.request().post(Entity.form(authData));

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.info("Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return response.readEntity(Token.class);

        return new Token();
    }

    @Override
    public Token refreshToken(String refreshToken) {
        Client client = ClientBuilder.newClient();

        WebTarget requestNewTokenTarget = client.target(buildURIToRequestToken());

        MultivaluedMap<String, String> authData = new MultivaluedHashMap<String, String>();
        authData.add(GRANT_TYPE_KEY, REFRESH_TOKEN_KEY);
        authData.add(REFRESH_TOKEN_KEY, refreshToken);
        authData.add(CLIENT_ID_KEY, CLIENT_ID);
        authData.add(CLIENT_SECRET_KEY, CLIENT_SECRET);

        Response response = requestNewTokenTarget.request().post(Entity.form(authData));

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return response.readEntity(Token.class);

        return new Token();
    }

    @Override
    public String getSpotifyUserName(final String bearer) {
        Client client = ClientBuilder.newClient();
        WebTarget getProfileUserName = client.target(buildURIToRequestUserProfileName());

        Response response = getProfileUserName.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.info( "Failed : HTTP error code : " + response.getStatus());
            LOGGER.info(response.readEntity(String.class));
        } else
            return response.readEntity(UserProfile.class).getId();
        
        return null;
    }
}