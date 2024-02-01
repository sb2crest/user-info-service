package com.example.user_info_service.dto;

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
