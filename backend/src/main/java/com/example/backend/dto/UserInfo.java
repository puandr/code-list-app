package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing User Information retrieved from security context.
 * Uses Lombok annotations to generate boilerplate code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private String name;
    private List<String> roles;

}