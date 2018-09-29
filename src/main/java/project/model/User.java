package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private  int id;
    private String login;
    private String password;
    private String confirmPassword;


    @JsonCreator
    public User(
            @JsonProperty("login") String login,
            @JsonProperty("password") String password,
            @JsonProperty("confirmPassword") String confirmPassword
    ) {
        this.login = login;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    @JsonCreator
    public User(
            @JsonProperty("login") String login,
            @JsonProperty("password") String password
    ) {
        this.login = login;
        this.password = password;
    }

    public User() {
        this.id = 0;
        this.login = "created";
        this.password = "created";

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}


