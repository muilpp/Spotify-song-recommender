package model.webservice_data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TopShortTermTracksDTO {
    private List<Item> itemsList;
    
    public TopShortTermTracksDTO() {
        itemsList = new ArrayList<>();
    }

    @JsonProperty("items")
    public List<Item> getItemList() {
        return itemsList;
    }
    public void setItemList(List<Item> itemList) {
        this.itemsList = itemList;
    }
}