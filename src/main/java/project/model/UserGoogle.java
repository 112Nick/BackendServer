package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class UserGoogle implements User{
    private BigDecimal id;
    private String given_name;
    private String email;
    private String token;

    @JsonCreator
    public UserGoogle(
            @JsonProperty("id") BigDecimal id,
            @JsonProperty("given_name") String given_name,
            @JsonProperty("email") String email,
            @JsonProperty("token") String token
    ) {
        this.id = id;
        this.given_name = given_name;
        this.email = email;
        this.token = token;
    }



    public UserGoogle() {
        this.id = BigDecimal.valueOf(0);
        this.given_name = "created";
        this.email = "created";
        this.token = "created";
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    @Override
    public String getLogin() {
        return given_name;
    }

    @Override
    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getDefault_email() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


