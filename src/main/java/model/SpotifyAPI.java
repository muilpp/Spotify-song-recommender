package model;

import static model.Endpoints.buildURIForShortTermTopTracks;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class SpotifyAPI implements ISpotifyAPI{
    private final Logger LOGGER = Logger.getLogger(SpotifyAPI.class.getName());

    @Override
    public void getUsersTopTracks() {
        Client client = ClientBuilder.newClient();

        WebTarget webTarget = client.target(buildURIForShortTermTopTracks());

        Response response = webTarget.request().get();
        
        if (response.getStatus() != 200)
            LOGGER.log(Level.INFO,  "Failed : HTTP error code : " + response.getStatus());
    }
}