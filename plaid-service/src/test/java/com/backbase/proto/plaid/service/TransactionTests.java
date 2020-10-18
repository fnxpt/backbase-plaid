package com.backbase.proto.plaid.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class TransactionTests {

    @Test
    public void testDescriptionParser() {

        String regex = ".+?(?=Pasvolgnr:)";
        String text = "ALBERT HEIJN 1338 ALBERT HEIJN 1338 AMSTERDAM NLD Pasvolgnr: 001 15-10-2020 17:59 Transactie: I8I2K6 Term: 8V709L Valutadatum: 16-10-2020";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if(m.find()) {
            System.out.println(m.group());
        }

    }


    @Test
    public void testReferenceParser() {

        String regex = "(?<=Transactie:\\s)(.*?)[?:\\s]";
        String text = "ALBERT HEIJN 1338 ALBERT HEIJN 1338 AMSTERDAM NLD Pasvolgnr: 001 15-10-2020 17:59 Transactie: I8I2K6 Term: 8V709L Valutadatum: 16-10-2020";


        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if(m.find()) {
            System.out.println(m.group());
        }

    }
}
