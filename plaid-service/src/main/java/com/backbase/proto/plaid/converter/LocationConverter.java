package com.backbase.proto.plaid.converter;

import com.backbase.proto.plaid.model.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;


/**
 * This class converts a list to a string and vice versa to be stored in a database.
 */
@Converter
@RequiredArgsConstructor
public class LocationConverter implements AttributeConverter<Location, String> {

    private final ObjectMapper objectMapper;

    /**
     * Takes a list of strings and joins then with a ',' separator so it may be stored as a column in a database.
     *
     * @param location the list to be converted into a string
     * @return joined string containing the contents of the list parsed in
     */
    @Override
    public String convertToDatabaseColumn(Location location) {
        if (location != null) {
            try {
                return objectMapper.writeValueAsString(location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Takes a string separated by ',' and splits it into a list to be stored as attributes.
     *
     * @param json a string to be turned to a list
     * @return the list of strings to be stored as attributes
     */
    @Override
    public Location convertToEntityAttribute(String json) {
        if (!StringUtils.isEmpty(StringUtils.join())) {
            try {
                return objectMapper.readValue(json, Location.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
