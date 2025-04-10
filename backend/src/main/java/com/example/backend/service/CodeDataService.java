package com.example.backend.service;

import com.example.backend.dto.Code;
import com.example.backend.dto.OrderByDirection;
import com.example.backend.dto.OrderByField;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for loading and managing the code list data from data.json.
 * Provides methods for accessing and sorting the codes.
 */
@Service
public class CodeDataService {

    private static final Logger log = LoggerFactory.getLogger(CodeDataService.class);

    private static final String BASE64_PREFIX = "base64:";

    @Value("${app.data.file.path:/data/data.json}")
    private String dataFilePath;

    @Value("${app.base64-service.url}")
    private String base64ServiceUrl;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;


    private List<Code> codeList = new CopyOnWriteArrayList<>();


    public CodeDataService(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Loads the code data from the JSON file specified by 'dataFilePath'
     * after the service bean has been initialized. [cite: 13]
     */
    @PostConstruct
    private void loadData() {
        log.info("Attempting to load code data from: {}", dataFilePath);
        List<Code> tempCodeList = new ArrayList<>();
        try (InputStream inputStream = Files.newInputStream(Paths.get(dataFilePath))) {
            List<List<String>> rawData = objectMapper.readValue(inputStream, new TypeReference<List<List<String>>>() {});

            if (rawData != null && rawData.size() > 1) {
                for (int i = 1; i < rawData.size(); i++) {
                    List<String> row = rawData.get(i);
                    if (row != null && row.size() >= 3) {
                        Code code = new Code();
                        code.setCode(row.get(0));
                        code.setType(row.get(1));
                        code.setName(row.get(2));
                        code.setCategory(null);
                        tempCodeList.add(code);
                    } else {
                        log.warn("Skipping invalid row at index {}: {}", i, row);
                    }
                }
                log.info("Successfully parsed {} code entries from {}", tempCodeList.size(), dataFilePath);
            } else {
                log.warn("Data file at {} is empty or only contains headers.", dataFilePath);
            }

        } catch (NoSuchFileException | FileNotFoundException e) {
            log.error("Data file not found at path: {}. Service will operate with an empty code list.", dataFilePath);
        } catch (IOException e) {
            log.error("Failed to read or parse data file at path: {}. Service will operate with an empty code list.", dataFilePath, e);
        } catch (Exception e) {
            log.error("Unexpected error processing data file at path: {}. Service will operate with an empty code list.", dataFilePath, e);
        }

        this.codeList = List.copyOf(tempCodeList);

        log.info("CodeDataService initialized with {} codes.", this.codeList.size());
    }




    /**
     * Returns the full, potentially unsorted, list of codes. [cite: 18]
     * @return An unmodifiable view or a copy of the code list.
     */
    public List<Code> getAllCodes() {
        return Collections.unmodifiableList(codeList);
    }

    /**
     * Finds a specific code by its ID. [cite: 18]
     * @param codeId The ID of the code to find.
     * @return An Optional containing the Code if found, otherwise an empty Optional.
     */
    public Optional<Code> getCodeById(String codeId) {
        if (codeId == null || codeId.trim().isEmpty()) {
            return Optional.empty();
        }
        return codeList.stream()
                .filter(code -> codeId.equals(code.getCode()))
                .findFirst();
    }

    /**
     * Returns the list of codes sorted according to the specified field and direction. [cite: 38]
     * Handles default sorting if parameters are invalid or null. [cite: 39]
     *
     * @param orderBy The field to sort by (nullable, defaults to CODE).
     * @param orderDirection The direction to sort (nullable, defaults to ASC).
     * @return A new list containing the sorted codes.
     */
    public List<Code> getSortedCodes(OrderByField orderBy, OrderByDirection orderDirection) {
        OrderByField effectiveOrderBy = (orderBy == null) ? OrderByField.code : orderBy;
        OrderByDirection effectiveOrderDirection = (orderDirection == null) ? OrderByDirection.asc : orderDirection;

        Comparator<Code> comparator = switch (effectiveOrderBy) {
            case name -> Comparator.comparing(Code::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case category -> Comparator.comparing(Code::getCategory, Comparator.nullsLast(String::compareToIgnoreCase));
            case code -> Comparator.comparing(Code::getCode, Comparator.nullsLast(String::compareToIgnoreCase));
        };

        if (effectiveOrderDirection == OrderByDirection.desc) {
            comparator = comparator.reversed();
        }

        return codeList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all codes, identifies those needing Base64 decoding (in name or type),
     * calls the external Base64 decoding service for them, and merges the results.
     *
     * @return A list of codes, with relevant fields decoded if applicable.
     */
    public List<Code> getDecodedCodes() {
        List<Code> originalCodes = getAllCodes();
        if (originalCodes.isEmpty()) {
            return originalCodes;
        }

        List<Code> codesToDecode = originalCodes.stream()
                .filter(code -> (code.getName() != null && code.getName().startsWith(BASE64_PREFIX)) ||
                        (code.getType() != null && code.getType().startsWith(BASE64_PREFIX)))
                .collect(Collectors.toList());

        if (codesToDecode.isEmpty()) {
            log.debug("No codes found requiring Base64 decoding.");
            return originalCodes;
        }

        log.debug("Found {} codes requiring Base64 decoding. Calling service at {}", codesToDecode.size(), base64ServiceUrl);

        List<Code> decodedCodesFromService = callBase64Service(codesToDecode);

        if (decodedCodesFromService.isEmpty() && !codesToDecode.isEmpty()) {
            log.warn("Base64 decoding service call seemed unsuccessful (returned empty list). Returning original codes.");
            return originalCodes;
        }

        Map<String, Code> decodedMap = decodedCodesFromService.stream()
                .collect(Collectors.toMap(Code::getCode, Function.identity()));

        List<Code> finalMergedList = originalCodes.stream()
                .map(originalCode -> decodedMap.getOrDefault(originalCode.getCode(), originalCode))
                .collect(Collectors.toList());

        return finalMergedList;
    }

    /**
     * Calls the external Base64 decoding service.
     *
     * @param codesToDecode List of codes to send for decoding.
     * @return List of decoded codes received from the service, or empty list on error.
     */
    private List<Code> callBase64Service(List<Code> codesToDecode) {
        String decodeUrl = base64ServiceUrl + "/decode";

        try {
            ResponseEntity<List<Code>> response = restTemplate.exchange(
                    decodeUrl,
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(codesToDecode),
                    new ParameterizedTypeReference<List<Code>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully received {} decoded codes from Base64 service", response.getBody().size());
                return response.getBody();
            } else {
                log.error("Base64 service call failed with status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            log.error("Error calling Base64 service at {}: {}", decodeUrl, e.getMessage());
            return Collections.emptyList();
        }
    }

}