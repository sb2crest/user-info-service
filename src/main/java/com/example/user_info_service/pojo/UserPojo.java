package com.example.user_info_service.pojo;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserPojo {
    @NonNull
    private String name;

    @NonNull
    private String mobile;

    private String email;
}
