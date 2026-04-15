package com.ben.sdet.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {

    private String name;
    private String email;
    private int age;

}
