package com.backbase.proto.plaid.converter;

import com.backbase.proto.plaid.model.PaymentMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter
public class PaymentMetaConverter implements AttributeConverter<PaymentMeta, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Takes a list of strings and joins then with a ',' separator so it may be stored as a column in a database.
     *
     * @param paymentMeta the list to be converted into a string
     * @return joined string containing the contents of the list parsed in
     */

    @Override
    public String convertToDatabaseColumn(PaymentMeta paymentMeta) {
        if (paymentMeta != null) {
            try {
                return objectMapper.writeValueAsString(paymentMeta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Takes a string separated by ',' and splits it into a list to be stored as attributes.
     *
     * @param joined a string to be turned to a list
     * @return the list of strings to be stored as attributes
     */

    @Override
    public PaymentMeta convertToEntityAttribute(String joined) {
        if (!StringUtils.isEmpty(StringUtils.join())) {
            try {
                return objectMapper.readValue(joined, PaymentMeta.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new PaymentMeta();
    }
}