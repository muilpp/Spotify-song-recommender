package model.webservice_data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationDTO {
    private List<Track> trackList;
    private boolean success;
    private String message;

    public RecommendationDTO() {
        trackList = new ArrayList<>();
    }

    public RecommendationDTO(boolean succes, String message) {
        this.setSuccess(succes);
        this.message = message;
    }

    @JsonProperty("tracks")
    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }   
}