package com.example.user_info_service.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserPojo {

    @NonNull
    private String name;

    @NonNull
    private String mobile;

    private String email;
}
