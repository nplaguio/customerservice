package com.bpi.customerservice.flow.inquirecustomer;

import com.bpi.customerservice.CustomerserviceApplicationTests;
import com.wsrvalni.wsrvalng.WSRVALNGPort;
import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InquireCustomerSoapErrorTest extends CustomerserviceApplicationTests {

    @MockitoBean
    private WSRVALNGPort wsrvalngPort; // Mock the underlying SOAP port, not the service

    @Test
    void testInquireCustomer_ServiceException() throws Exception {

        when(wsrvalngPort.wsrvalngOperation(any())).thenThrow(new WebServiceException("SOAP Server Down"));

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "test-request-uid")
                        .header("resourceOwnerID", "test-resource-owner-id")
                        .content(requestBody))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CUSSE999"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testInquireCustomer_NullResponse() throws Exception {

        when(wsrvalngPort.wsrvalngOperation(any())).thenReturn(null);

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "test-request-uid")
                        .header("resourceOwnerID", "test-resource-owner-id")
                        .content(requestBody))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").exists());
    }
}