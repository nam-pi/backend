package eu.nampi.backend.domain;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBodyCreateUser {

    @NotNull
    private String userName;

    @NotNull
    private String email;

}