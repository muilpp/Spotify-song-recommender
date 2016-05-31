package model.webservice_data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Track {
    private String songId;
    private String songName;
    private List<Artist> artistList;

    @JsonProperty("id")
    public String getSongId() {
        return songId;
    }
    public void setSongId(String songId) {
        this.songId = songId;
    }

    @JsonProperty("name")
    public String getSongName() {
        return songName;
    }
    public void setSongName(String songName) {
        this.songName = songName;
    }
    
    @JsonProperty("artists")
    public List<Artist> artistList() {
        return artistList;
    }
    public void setArtistList(List<Artist> artistList) {
        this.artistList = artistList;
    }
}