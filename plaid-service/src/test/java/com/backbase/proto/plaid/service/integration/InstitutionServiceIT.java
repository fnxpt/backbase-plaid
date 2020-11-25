package com.backbase.proto.plaid.service.integration;

import com.backbase.proto.plaid.PlaidApplication;
import com.backbase.proto.plaid.model.Institution;
import com.backbase.proto.plaid.repository.InstitutionRepository;
import com.backbase.proto.plaid.service.InstitutionService;
import com.backbase.proto.plaid.service.mockserver.plaid.TestMockServer;
import com.google.gson.Gson;
import liquibase.pro.packaged.I;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = PlaidApplication.class
)
@Slf4j
public class InstitutionServiceIT extends TestMockServer {
    static {
        System.setProperty("SIG_SECRET_KEY", "test!");
    }
    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private InstitutionRepository institutionRepository;

    private Gson gson = new Gson();


    @Test
    public void testGetInstitution(){
        Assert.assertFalse(" institution already present ", institutionRepository.existsByInstitutionId("ins_3"));
        institutionService.getInstitution("ins_3", "lesley.knope");
        Institution expectedInstitution = new Institution();
        expectedInstitution.setInstitutionId("ins_3");
        expectedInstitution.setName("ING - Particulier");
        expectedInstitution.setRoutingNumbers(new ArrayList<>());
        expectedInstitution.setFirstRegisteredAt(LocalDateTime.now());


        Assert.assertTrue("institution created ", institutionRepository.existsByInstitutionId("ins_3"));


    }
}
