package eu.nampi.backend.model;

import javax.validation.constraints.NotNull;

public class RequestBodyCreateUser {

    @NotNull
    private String userName;

    @NotNull
    private String email;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}