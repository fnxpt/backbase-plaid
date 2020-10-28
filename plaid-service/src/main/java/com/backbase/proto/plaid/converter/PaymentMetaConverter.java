package com.backbase.proto.plaid.converter;

import com.backbase.proto.plaid.model.PaymentMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Slf4j
@Converter
public class PaymentMetaConverter implements AttributeConverter<PaymentMeta, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Takes a list of strings and joins then with a ',' separator so it may be stored as a column in a database.
     *
     * @param paymentMeta the list to be converted into a string
     * @return json string containing the contents of the list parsed in
     */

    @Override
    public String convertToDatabaseColumn(PaymentMeta paymentMeta) {
        if (paymentMeta != null) {
            try {
                return objectMapper.writeValueAsString(paymentMeta);
            } catch (IOException e) {
                log.error("Failed to write paymentMeta: {} to database format", paymentMeta, e);
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
    public PaymentMeta convertToEntityAttribute(String json) {
        if (!StringUtils.isEmpty(StringUtils.join())) {
            try {
                return objectMapper.readValue(json, PaymentMeta.class);
            } catch (IOException e) {
                log.error("Failed to read Payment meta: {} to database format", json, e);
            }

        }
        return new PaymentMeta();
    }
}