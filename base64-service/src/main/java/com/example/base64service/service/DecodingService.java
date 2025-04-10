package com.example.base64service.service;

import com.example.base64service.dto.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for decoding Base64 encoded fields within Code objects.
 */
@Service
public class DecodingService {

    private static final Logger log = LoggerFactory.getLogger(DecodingService.class);
    private static final String BASE64_PREFIX = "base64:";

    /**
     * Decodes Base64 encoded 'name' and 'type' fields in a list of Code objects.
     * Fields prefixed with "base64:" are processed.
     *
     * @param inputCodes The list of Code objects to process.
     * @return A new list containing Code objects with relevant fields decoded.
     * Original objects are returned if no decoding was needed for them.
     * Returns an empty list if the input is null or empty.
     */
    public List<Code> decodeCodeList(List<Code> inputCodes) {
        if (inputCodes == null || inputCodes.isEmpty()) {
            log.debug("Received null or empty list for decoding, returning empty list.");
            return Collections.emptyList();
        }

        log.debug("Processing {} codes for potential Base64 decoding.", inputCodes.size());

        return inputCodes.stream()
                .map(this::decodeSingleCode)
                .collect(Collectors.toList());
    }

    /**
     * Processes a single Code object, decoding name and type if necessary.
     *
     * @param originalCode The original Code object.
     * @return A new Code object with decoded fields, or the original object if no decoding occurred.
     */
    private Code decodeSingleCode(Code originalCode) {
        if (originalCode == null) {
            return null;
        }

        String originalName = originalCode.getName();
        String originalType = originalCode.getType();

        String decodedName = decodeField(originalName, "name", originalCode.getCode());
        String decodedType = decodeField(originalType, "type", originalCode.getCode());

        if (originalName == decodedName && originalType == decodedType) {
            return originalCode;
        }

        log.trace("Decoded fields for code [{}]: name='{}', type='{}'", originalCode.getCode(), decodedName, decodedType);
        return new Code(
                originalCode.getCode(),
                decodedType,
                decodedName,
                originalCode.getCategory()
        );
    }

    /**
     * Decodes a single field value if it starts with the Base64 prefix.
     *
     * @param value     The field value to potentially decode.
     * @param fieldName The name of the field (for logging).
     * @param codeId    The ID of the code (for logging).
     * @return The decoded string, or the original value if not encoded or if decoding fails.
     */
    private String decodeField(String value, String fieldName, String codeId) {
        if (value != null && value.startsWith(BASE64_PREFIX)) {
            String encodedPart = value.substring(BASE64_PREFIX.length());
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(encodedPart);
                return new String(decodedBytes, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to decode Base64 value for field '{}' in code [{}]. Input was: '{}'. Error: {}",
                        fieldName, codeId, value, e.getMessage());
                return value;
            }
        }
        return value;
    }
}