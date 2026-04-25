package com.investresearch.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.investresearch.model.InvestorProfile;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class InvestorProfileConverter implements AttributeConverter<InvestorProfile, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(InvestorProfile profile) {
        if (profile == null) return null;
        try {
            return MAPPER.writeValueAsString(profile);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize InvestorProfile to JSON", e);
        }
    }

    @Override
    public InvestorProfile convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return MAPPER.readValue(json, InvestorProfile.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize InvestorProfile from JSON", e);
        }
    }
}
