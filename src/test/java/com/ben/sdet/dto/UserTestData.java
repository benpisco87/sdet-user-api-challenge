package com.ben.sdet.dto;

import com.ben.sdet.dto.user.CreateUserRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTestData {

    private String label;
    private CreateUserRequest user;


}
