package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private  int id;
    private String login;
    private String default_email;
    private String token;


    @JsonCreator
    public User(
            @JsonProperty("id") Integer id,
            @JsonProperty("login") String login,
            @JsonProperty("default_email") String default_email,
            @JsonProperty("token") String token
    ) {
        this.id = id;
        this.login = login;
        this.default_email = default_email;
        this.token = token;
    }



    public User() {
        this.id = 0;
        this.login = "created";
        this.default_email = "created";
        this.token = "created";
    }


    public Integer getId() {
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

    public String getDefault_email() {
        return default_email;
    }

    public void setDefault_email(String default_email) {
        this.default_email = default_email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


