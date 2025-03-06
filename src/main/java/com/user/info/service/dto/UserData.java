package com.user.info.service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {

    private String name;

    @NonNull
    private String email;

    private String message;
}
