package model.webservice_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatedPlaylistDTO {
    private String playlistId;

    @JsonProperty("id")
    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String id) {
        this.playlistId = id;
    }
}