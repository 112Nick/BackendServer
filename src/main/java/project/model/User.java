package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private  int id;
    private String login;
    private String defaultEmail;
    private String token;


    @JsonCreator
    public User(
            @JsonProperty("login") String login,
            @JsonProperty("default_email") String defaultEmail,
            @JsonProperty("token") String token
    ) {
        this.login = login;
        this.defaultEmail = defaultEmail;
        this.token = token;
    }



    public User() {
        this.id = 0;
        this.login = "created";
        this.defaultEmail = "created";
        this.token = "created";


    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDefaultEmail() {
        return defaultEmail;
    }

    public void setDefaultEmail(String defaultEmail) {
        this.defaultEmail = defaultEmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


