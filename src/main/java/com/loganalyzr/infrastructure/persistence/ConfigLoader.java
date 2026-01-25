package com.loganalyzr.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {

    public FilterRulesDTO loadConfig(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), FilterRulesDTO.class);
        } catch (IOException e) {
            System.err.println("Error crítico leyendo rules.json: " + e.getMessage());
            return new FilterRulesDTO();
        }
    }
}