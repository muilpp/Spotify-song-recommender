package controller.webservice;

import static model.Constant.STEP_SIZE_FOR_RECS;
import static model.Endpoints.buildURIForRecommendations;
import static model.ErrorMessage.COULD_NOT_GET_RECOMMENDATIONS;
import static model.ErrorMessage.NOT_ENOUGH_DATA_FOR_RECOMMENDATIONS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Strings;

import model.ISpotifyAPI;
import model.SpotifyAPI;
import model.database.IUserDAO;
import model.database.UserDAO;
import model.webservice_data.Item;
import model.webservice_data.RecommendationDTO;
import model.webservice_data.Token;
import model.webservice_data.TopShortTermTracksDTO;
import model.webservice_data.TrackURI;

/* TODO
 *  - Avoid repeated songs
 *  - Use threads to get recommendations
 *  - Use Lists.partition instead of the module at getListByStepSize
 *  - Add filters to avoid repeating the header in each request
 */

@WebService
@Path("/")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class RecommendAPI {
    private final static Logger LOGGER = Logger.getLogger(RecommendAPI.class.getName());

    @GET
    @Path("recommendations/{authorization_code}")
    public RecommendationDTO getRecommendations(@PathParam("authorization_code") final String authorizationCode) {
        final ISpotifyAPI spotifyAPI = new SpotifyAPI();
        final IUserDAO userDAO = new UserDAO();

        Token authToken = spotifyAPI.requestToken(authorizationCode);

        String userName = spotifyAPI.getUserId(authToken.getAccessToken());
        userDAO.addUser(userName, authToken.getAccessToken(), authToken.getRefreshToken());

        return getRecommendations(authToken);
    }

    public RecommendationDTO getRecommendations(Token authToken) {
        final ISpotifyAPI spotifyAPI = new SpotifyAPI();

        TopShortTermTracksDTO shortTermTracks = spotifyAPI.getShortTermTopTracks(authToken.getAccessToken());
        List<Item> itemList = shortTermTracks.getItemList();

        if (itemList == null || itemList.isEmpty())
            return new RecommendationDTO(false, NOT_ENOUGH_DATA_FOR_RECOMMENDATIONS);

        List<String> songIdList = getListByStepSize(itemList);
        RecommendationDTO recs = new RecommendationDTO();

        for (String songId : songIdList) {
            Client client = ClientBuilder.newClient();
            WebTarget recommendationsTarget = client
                    .target(buildURIForRecommendations(songId.replace("[", "").replace("]", "")));
            Response response = recommendationsTarget.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + authToken.getAccessToken()).get();
            if (response.getStatus() != Status.OK.getStatusCode()) {
                LOGGER.info("Failed : HTTP error code : " + response.getStatus());
                LOGGER.info(response.readEntity(String.class));
            } else {
                RecommendationDTO recDTO = response.readEntity(RecommendationDTO.class);
                recs.getTrackList().addAll(recDTO.getTrackList());
                LOGGER.info("List size -> " + recs.getTrackList().size());
            }
        }
        String playlistId = spotifyAPI.getPlaylistId(authToken.getAccessToken());
        String userId = spotifyAPI.getUserId(authToken.getAccessToken());

        if (Strings.isNullOrEmpty(playlistId)) {
            // Create playlist for the first time
            playlistId = spotifyAPI.createPlaylist(authToken.getAccessToken(), userId);

            if (Strings.isNullOrEmpty(playlistId))
                return new RecommendationDTO(false, COULD_NOT_GET_RECOMMENDATIONS);
        }

        if (recs.getTrackList().isEmpty())
            return new RecommendationDTO(false, COULD_NOT_GET_RECOMMENDATIONS);
        else {
            List<TrackURI> trackURIList = spotifyAPI.createUriTrackList(recs);

            int addedSongsCount = 0;

            // First chunk replaces the old songs
            if (trackURIList.size() > 0)
                addedSongsCount += spotifyAPI.replaceOldSongsInPlaylist(authToken.getAccessToken(), userId, playlistId,
                        trackURIList.get(0));

            // If there're still songs to be added, we add them normally
            if (trackURIList.size() > 1) {
                for (int i = 1; i < trackURIList.size(); i++) {
                    addedSongsCount += spotifyAPI.addNewSongsToPlaylist(authToken.getAccessToken(), userId, playlistId,
                            trackURIList.get(i));
                }
            }

            return new RecommendationDTO(true, "" + addedSongsCount);
        }
    }

    private List<String> getListByStepSize(List<Item> list) {
        List<String> result = new ArrayList<>();

        int size = list.size() / STEP_SIZE_FOR_RECS;

        for (int i = 0; i < size; i++) {
            List<Item> itemSubList = list.subList(STEP_SIZE_FOR_RECS * i, (i + 1) * STEP_SIZE_FOR_RECS);

            Set<String> songSet = new HashSet<>();
            for (Item item : itemSubList) {
                songSet.add(item.getSongId());
            }

            String songName = songSet.toString().replace("[", "").replace("]", "").replaceAll(" ", "");

            result.add(songName);
        }

        int res = list.size() % STEP_SIZE_FOR_RECS;
        if (res != 0) {
            List<Item> itemSubList = list.subList(list.size() - res, list.size());

            Set<String> songSet = new HashSet<>();
            for (Item item : itemSubList) {
                songSet.add(item.getSongId());
            }
            result.add(songSet.toString().replace("[", "").replace("]", "").replaceAll(" ", ""));
        }

        return result;
    }
}