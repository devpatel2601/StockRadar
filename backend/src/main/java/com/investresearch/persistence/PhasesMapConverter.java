package com.investresearch.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.investresearch.model.PhaseResult;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class PhasesMapConverter implements AttributeConverter<Map<String, PhaseResult>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final TypeReference<Map<String, PhaseResult>> TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, PhaseResult> phases) {
        if (phases == null) return null;
        try {
            return MAPPER.writeValueAsString(phases);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize phases map to JSON", e);
        }
    }

    @Override
    public Map<String, PhaseResult> convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize phases map from JSON", e);
        }
    }
}
