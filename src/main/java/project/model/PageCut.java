package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageCut {
    private String uuid;
    private String title;
    private String date;

    public PageCut() {
        this.title = "JustCreated";
    }


    @JsonCreator
    public PageCut(@JsonProperty("ID") String uuid,
                   @JsonProperty("title") String title,
                   @JsonProperty("date") String date) {
        this.uuid = uuid;
        this.title = title;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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