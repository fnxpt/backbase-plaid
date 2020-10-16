package com.backbase.proto.plaid.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductTypeUtils {

    public static String mapSubTypeId(String subtype) {
        return "external-" + subtype.replace(" ", "-");
    }

    public static String mapProductType(String type, String s) {
        return s + type;
    }
}
