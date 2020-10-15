package com.backbase.proto.plaid;

import com.backbase.stream.mambu.configuration.MambuBootstrapConfiguration;
import com.backbase.stream.mambu.configuration.MambuConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MambuBootstrapConfiguration.class)
public class PlaidApplication {

    public static void main(String[] args) {
        new SpringApplication(PlaidApplication.class).run(args);
    }

}
