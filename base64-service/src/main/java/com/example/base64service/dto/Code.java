package com.example.base64service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object representing a Code item for the Base64 Service.
 * Reuses the structure from Phase 1.
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