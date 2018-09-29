package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageCut {
    private int ID;
    private String title;

    public PageCut() {
        this.title = "JustCreated";
    }


    @JsonCreator
    public PageCut(@JsonProperty("ID") int ID,
                   @JsonProperty("title") String title) {
        this.ID = ID;
        this.title = title;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}