package controller.webservice;

import static model.Constant.MAX_SONGS_TO_ADD_PER_REQUEST;
import static model.Constant.PLAYLIST_NAME;
import static model.Constant.SPOTIFY_TRACK;
import static model.Constant.STEP_SIZE_FOR_RECS;
import static model.Endpoints.buildURIForRecommendations;
import static model.Endpoints.buildURIForShortTermTopTracks;
import static model.Endpoints.buildURIToAddNewSongs;
import static model.Endpoints.buildURIToReplaceOldSongs;
import static model.Endpoints.buildURIToCreatePlaylist;
import static model.Endpoints.buildURIToGetUserPlaylists;
import static model.Endpoints.buildURIToGetUserProfile;
import static model.ErrorMessage.COULD_NOT_GET_RECOMMENDATIONS;
import static model.ErrorMessage.NOT_ENOUGH_DATA_FOR_RECOMMENDATIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Strings;

import jersey.repackaged.com.google.common.collect.Lists;
import model.webservice_data.CreatedPlaylistDTO;
import model.webservice_data.Item;
import model.webservice_data.PlaylistDTO;
import model.webservice_data.PlaylistItem;
import model.webservice_data.RecommendationDTO;
import model.webservice_data.TopShortTermTracksDTO;
import model.webservice_data.Track;
import model.webservice_data.TrackURI;
import model.webservice_data.UserProfileDTO;

/* TODO
 *  - Avoid repeated songs
 *  - Integrate with Hibernate
 *  - Quartz to execute weekly
 *  - Use threads to get recommendations
 *  - Add filters to avoid repeating the header in each request
 */

@WebService
@Path("/")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class RecommendAPI {
    private final static Logger LOGGER = Logger.getLogger(RecommendAPI.class.getName());
//    private final String BEARER = "BQB5fHBzjeMlsPORlHDr1GyIC3uQ8_VG-k6mSKk91_TdcPm99403AlrzCPT_hMPN7Zo8Hvvz384gSO8mmNLYZbCUGWuYGvJHVliJ0a6Va2Fz-gWg5_JFQE1WcwjtV011HJpAR17hhoi7dsbspSl5VxzgO4b_yTi5lDr6uYmDt-FI9LXQsDy_Bz8TRk0qV0jxIgO1Vm7cn15YLEKMFlOD_TsnlBf3Ty-9Mk3SIoNrhw";

    @GET
    @Path("recommendations/{token}")
    public RecommendationDTO getRecommendations(@PathParam("token") final String token) {
        TopShortTermTracksDTO shortTermTracks = getShortTermTopTracks(token);
        List<Item> itemList = shortTermTracks.getItemList();

        if (itemList == null || itemList.isEmpty())
            return new RecommendationDTO(false, NOT_ENOUGH_DATA_FOR_RECOMMENDATIONS);

        List<String> songIdList = getListByStepSize(itemList);

        RecommendationDTO recs = new RecommendationDTO();
        for (String songId : songIdList) {
            String uri = buildURIForRecommendations(songId.replace("[", "").replace("]", ""));

            Client client = ClientBuilder.newClient();
            WebTarget recommendationsTarget = client.target(uri);

            Response response = recommendationsTarget.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() != Status.OK.getStatusCode()) {
                LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
                LOGGER.log(Level.INFO, response.readEntity(String.class));
            } else {
                RecommendationDTO recDTO = response.readEntity(RecommendationDTO.class);
                LOGGER.log(Level.INFO, "Mida tracks noves -> " + recDTO.getTrackList().size());
                recs.getTrackList().addAll(recDTO.getTrackList());
                LOGGER.log(Level.INFO, "Mida llista -> " + recs.getTrackList().size());
            }
        }

        String playlistId = getPlaylistId(token);
        String userId = getUserId(token);

        if (Strings.isNullOrEmpty(playlistId)) {
            //Create playlist for the first time
            playlistId = createPlaylist(token, userId);

            if (Strings.isNullOrEmpty(playlistId))
                return new RecommendationDTO(false, COULD_NOT_GET_RECOMMENDATIONS);
        }

        if (recs.getTrackList().isEmpty())
            return new RecommendationDTO(false, COULD_NOT_GET_RECOMMENDATIONS);
        else {
            List<TrackURI> trackURIList = createUriTrackList(recs);

            int addedSongsCount = 0;

            //First chunk replaces the old songs
            if (trackURIList.size() > 0)
                addedSongsCount += replaceOldSongsInPlaylist(token, userId, playlistId, trackURIList.get(0));

            //If there're still songs to be added, we add them normally
            if (trackURIList.size() > 1) {
                for (int i = 1; i<trackURIList.size(); i++) {
                    addedSongsCount += addNewSongsToPlaylist(token, userId, playlistId, trackURIList.get(i));
                }
            }

            return new RecommendationDTO(true, "" + addedSongsCount);
        }
    }

    private TopShortTermTracksDTO getShortTermTopTracks(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget shortTermsTracksTarget = client.target(buildURIForShortTermTopTracks());

        LOGGER.log(Level.INFO, "URI -> " + buildURIForShortTermTopTracks());

        Response response = shortTermsTracksTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));            
        } else
            return response.readEntity(TopShortTermTracksDTO.class);

        return new TopShortTermTracksDTO();
    }

    private String getPlaylistId(final String bearer) {
        PlaylistDTO playlistDTO = getUserPlaylists(bearer);

        for (PlaylistItem playlistItem : playlistDTO.getPlaylistItemList()) {
            if (playlistItem.getPlaylistName().equalsIgnoreCase(PLAYLIST_NAME)) {
                return playlistItem.getPlaylistId();
            }
        }
        
        return null;
    }
    
    private PlaylistDTO getUserPlaylists(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget userPlaylistsTarget = client.target(buildURIToGetUserPlaylists());

        LOGGER.log(Level.INFO, "URI -> " + buildURIToGetUserPlaylists());

        Response response = userPlaylistsTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));
        } else
            return response.readEntity(PlaylistDTO.class);

        return new PlaylistDTO();
    }

    private String getUserId(final String bearer) {
        Client client = ClientBuilder.newClient();

        WebTarget userProfileTarget = client.target(buildURIToGetUserProfile());

        LOGGER.log(Level.INFO, "URI -> " + buildURIToGetUserProfile());

        Response response = userProfileTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .get();

        if (response.getStatus() != Status.OK.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));
        } else {
            UserProfileDTO userProfile = response.readEntity(UserProfileDTO.class);
            return userProfile.getUserId();
        }

        return null;
    }

    private String createPlaylist(final String bearer, final String userId) {
        Client client = ClientBuilder.newClient();

        WebTarget createPlaylistTarget = client.target(buildURIToCreatePlaylist(userId));

        LOGGER.log(Level.INFO, "URI -> " + buildURIToCreatePlaylist(userId));

        PlaylistItem playlist = new PlaylistItem(PLAYLIST_NAME, true);

        Response response = createPlaylistTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .post(Entity.entity(playlist, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));
        } else
            return response.readEntity(CreatedPlaylistDTO.class).getPlaylistId();

        return null;
    }

    private int replaceOldSongsInPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI) {
        Client client = ClientBuilder.newClient();

        WebTarget replacePlaylistTarget = client.target(buildURIToReplaceOldSongs(userId, playlistId));

        LOGGER.log(Level.INFO, "URI -> " + buildURIToReplaceOldSongs(userId, playlistId));

        Response response = replacePlaylistTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .put(Entity.entity(trackURI, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));
        } else
            return trackURI.getURISet().size();

        return 0;
    }

    private int addNewSongsToPlaylist(final String bearer, final String userId, final String playlistId, final TrackURI trackURI) {
        Client client = ClientBuilder.newClient();

        WebTarget addNewSongsTarget = client.target(buildURIToAddNewSongs(userId, playlistId));

        LOGGER.log(Level.INFO, "URI -> " + buildURIToAddNewSongs(userId, playlistId));

        Response response = addNewSongsTarget.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearer)
                .post(Entity.entity(trackURI, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
            LOGGER.log(Level.INFO, response.readEntity(String.class));
        } else
            return trackURI.getURISet().size();

        return 0;
    }

    private List<TrackURI> createUriTrackList(RecommendationDTO recs) {
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

    private List<String> getListByStepSize(List<Item> list) {
        List<String> result = new ArrayList<>();

        int size = list.size() / STEP_SIZE_FOR_RECS;
        for (int i=0; i<size; i++) {
            List<Item> itemSubList = list.subList(5*i, (i+1)*5);

            Set<String> songSet = new HashSet<>();
            for (Item item : itemSubList) {
                songSet.add(item.getSongId());
            }
            result.add(songSet.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
        }

        int res = list.size() % STEP_SIZE_FOR_RECS;
        if (res!= 0) {
            List<Item> itemSubList = list.subList(list.size()-res, list.size());

            Set<String> songSet = new HashSet<>();
            for (Item item : itemSubList) {
                songSet.add(item.getSongId());
            }
            result.add(songSet.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
        }

        return result;
    }
}