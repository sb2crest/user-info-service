package com.example.user_info_service.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    @NonNull
    private String firstName;

    private String middleName;

    @NonNull
    private String lastName;

    @NonNull
    private String mobile;

    private String email;
}
