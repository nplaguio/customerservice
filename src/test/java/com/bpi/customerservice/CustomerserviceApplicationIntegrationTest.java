package com.bpi.customerservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerserviceApplicationIntegrationTest extends CustomerserviceApplicationTests {

    @Test
    void testMainExecution() {

        CustomerserviceApplication.main(new String[]{"--server.port=0"});

        assertNotNull(customerRepository);
        
    }
}