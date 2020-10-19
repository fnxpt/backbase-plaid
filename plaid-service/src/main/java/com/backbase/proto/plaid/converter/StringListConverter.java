package com.backbase.proto.plaid.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

/**
 * StringListConverter:
 * Converts a list to a string and vis versa to be stored in a database
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    /**
     * Takes a list of strings and joins then with a ',' separator so it may be stored as a column in a database
     *
     * @param list the list to be converted into a string
     * @return joined string containing the contents of the list parsed in
     */
    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if(list == null) {
            return null;
        } else {
            return String.join(",", list);
        }
    }

    /**
     * Takes a string separated by ',' and splits it into a list to be stored as attributes
     *
     * @param joined a string to be turned to a list
     * @return the list of strings to be stored as attributes
     */
    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if(StringUtils.isEmpty(StringUtils.join())) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(joined.split(",")));
        }
    }

}
