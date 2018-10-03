package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageCut {
    private String uuid;
    private String title;

    public PageCut() {
        this.title = "JustCreated";
    }


    @JsonCreator
    public PageCut(@JsonProperty("ID") String uuid,
                   @JsonProperty("title") String title) {
        this.uuid = uuid;
        this.title = title;
    }

    public String getID() {
        return uuid;
    }

    public void setID(String ID) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}