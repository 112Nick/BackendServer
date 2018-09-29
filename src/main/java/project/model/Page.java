package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Page {
    private int ID;
    private int ownerID;
    private String title;
    private boolean isPublic;
    private String[] fieldsNames;
    private String[] fieldsValues;

    public Page() {
        this.title = "JustCreated";
    }


    @JsonCreator
    public Page( @JsonProperty("ownerID") int ownerID,
                 @JsonProperty("title") String title,
                 @JsonProperty("isPublic") boolean isPublic,
                 @JsonProperty("fieldsNames") String[] fieldsNames,
                 @JsonProperty("fieldsValues") String[] fieldsValues) {
        this.ownerID = ownerID;
        this.title = title;
        this.isPublic = isPublic;
        this.fieldsNames = fieldsNames;
        this.fieldsValues = fieldsValues;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String[] getFieldsNames() {
        return fieldsNames;
    }

    public void setFieldsNames(String[] fieldsNames) {
        this.fieldsNames = fieldsNames;
    }

    public String[] getFieldsValues() {
        return fieldsValues;
    }

    public void setFieldsValues(String[] fieldsValues) {
        this.fieldsValues = fieldsValues;
    }
}
