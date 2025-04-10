package com.example.base64service.controller;

import com.example.base64service.dto.Code;
import com.example.base64service.service.DecodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for handling Base64 decoding requests.
 */
@RestController
public class DecodeController {

    private static final Logger log = LoggerFactory.getLogger(DecodeController.class);

    private final DecodingService decodingService;

    /**
     * Constructor for injecting the DecodingService.
     * @param decodingService The service responsible for decoding logic.
     */
    public DecodeController(DecodingService decodingService) {
        this.decodingService = decodingService;
    }

    /**
     * POST /decode
     * Accepts a list of Code objects and returns a new list with Base64 encoded
     * name and type fields decoded.
     * Requires authentication and admin role (enforced via SecurityConfig).
     *
     * @param codesToDecode A list of Code objects from the request body.
     * @return A list of Code objects with relevant fields decoded.
     */
    @PostMapping("/decode")
    public List<Code> decodeCodes(@RequestBody List<Code> codesToDecode) {
        log.info("Received request to decode {} codes.", (codesToDecode != null ? codesToDecode.size() : 0));

        List<Code> decodedCodes = decodingService.decodeCodeList(codesToDecode);
        log.info("Returning {} processed codes.", decodedCodes.size());
        return decodedCodes;
    }
}