package com.loganalyzr.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzr.infrastructure.config.dto.ActionsConfigDTO;

import java.io.File;
import java.io.IOException;

/**
 * Carga el archivo actions.json y lo deserializa en ActionsConfigDTO.
 *
 * Si el archivo no existe o falla la lectura, retorna una config vacía
 * (sin reglas) para no interrumpir el arranque del agente.
 */
public class ActionConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public ActionsConfigDTO load(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("[ActionConfigLoader] Archivo no encontrado: " + filePath
                        + ". El DecisionEngine arrancará sin reglas.");
                return new ActionsConfigDTO();
            }
            return mapper.readValue(file, ActionsConfigDTO.class);
        } catch (IOException e) {
            System.err.println("[ActionConfigLoader] Error leyendo " + filePath + ": " + e.getMessage());
            return new ActionsConfigDTO();
        }
    }
}
