package com.example.backend.controller;

import com.example.backend.dto.Code;
import com.example.backend.dto.OrderByDirection;
import com.example.backend.dto.OrderByField;
import com.example.backend.service.CodeDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing code-related endpoints.
 * <p>
 * Implements:
 * - GET /public/codes – returns a list of available code identifiers.
 * - GET /public/code/{code} – returns detailed information for a given code.
 * - GET /private/codes – returns a sorted list of codes based on query parameters.
 * - GET /private/decodedcodes – returns the list of decoded codes.
 */
@RestController
public class CodeController {

    private final CodeDataService codeDataService;

    public CodeController(CodeDataService codeDataService) {
        this.codeDataService = codeDataService;
    }

    /**
     * GET /public/codes
     * Returns a list of code identifiers.
     *
     * @return A list of code strings.
     */
    @GetMapping("/public/codes")
    public List<String> getPublicCodes() {
        return codeDataService.getAllCodes().stream()
                .map(Code::getCode)
                .collect(Collectors.toList());
    }

    /**
     * GET /public/code/{code}
     * Returns detailed information about a specific code.
     *
     * @param code The code identifier.
     * @return A ResponseEntity with the code details if found, or HTTP 404 status if not.
     */
    @GetMapping("/public/code/{code}")
    public ResponseEntity<Code> getPublicCode(@PathVariable("code") String code) {
        return codeDataService.getCodeById(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * GET /private/codes
     * Returns a sorted list of codes.
     *
     * @param orderBy          The field to sort by (defaults to "code").
     * @param orderByDirection The direction of the sort (defaults to "asc").
     * @return A list of Code objects sorted according to the parameters.
     */
    @GetMapping("/private/codes")
    public List<Code> getPrivateCodes(
            @RequestParam(name = "orderby", required = false, defaultValue = "code") OrderByField orderBy,
            @RequestParam(name = "orderbydirection", required = false, defaultValue = "asc") OrderByDirection orderByDirection) {
        return codeDataService.getSortedCodes(orderBy, orderByDirection);
    }


    /**
     * GET /private/decodedcodes
     * Retrieves codes, triggers decoding via Base64 service for relevant codes,
     * and returns the potentially modified list.
     * Requires Admin role (enforced by SecurityConfig).
     *
     * @return A list of Code objects, with base64 fields decoded.
     */
    @GetMapping("/private/decodedcodes")
    public List<Code> getDecodedCodes() {
        return codeDataService.getDecodedCodes();
    }
}
