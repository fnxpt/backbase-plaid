package com.backbase.proto.plaid.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if(list == null) {
            return null;
        } else {
            return String.join(",", list);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if(StringUtils.isEmpty(StringUtils.join())) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(joined.split(",")));
        }
    }

}
