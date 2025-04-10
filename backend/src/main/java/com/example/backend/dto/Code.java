package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object representing a Code item.
 * Uses Lombok annotations to generate boilerplate code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Code {

    private String code;
    private String type;
    private String name;
    private String category;

}