package com.backbase.proto.plaid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlaidApplication {

    public static void main(String[] args) {
        new SpringApplication(PlaidApplication.class).run(args);
    }

}
