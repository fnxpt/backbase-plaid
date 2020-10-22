package com.backbase.proto.plaid.converter;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {


    @Override
    public String convertToDatabaseColumn(String[] strings) {
        if(strings == null) {
            return null;
        } else {
            return String.join(",", strings);
        }
    }

    @Override
    public String[] convertToEntityAttribute(String s) {
        if(StringUtils.isEmpty(StringUtils.join())) {
            return null;
        } else {
            return (String[]) new ArrayList(Arrays.asList((s.split(",")))).toArray();
        }    }
}
