package com.backbase.proto.plaid.utils;

import com.backbase.proto.plaid.model.Institution;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductTypeUtils {

    public static String mapSubTypeId(Institution institution, String subtype) {
        return institution.getInstitutionId().replace("_", "-") + "-" +  subtype.replace(" ", "-");
    }

    public static String mapProductType(Institution institution, String type) {
        return institution.getInstitutionId().replace("_", "-") + "-" + type.replace(" ", "-");
    }
}
