package com.bpi.customerservice.flow.inquirecustomer;

import com.bpi.customerservice.CustomerserviceApplicationTests;
import com.bpi.customerservice.service.ws.InquireCustomerSoapService;
import com.response.wsrvalni.wsrvalng.ProgramInterface.WsrvalngOutput;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InquireCustomerSoapSuccessTest extends CustomerserviceApplicationTests {

    private static final String INQUIRE_CUSTOMER_URL = "/api/customers/";

    @MockitoBean
    private InquireCustomerSoapService inquireCustomerSoapApi;

    @Test
    void testInquireCustomer_Success() throws Exception {
        var mockSoapOutput = new WsrvalngOutput();

        mockSoapOutput.setWsoRmacctnoFromCustNbr("1234567890");

        mockSoapOutput.setWsoFiller1("SUCCESS_FILLER_DATA");

        when(inquireCustomerSoapApi.callSoapService(any())).thenReturn(mockSoapOutput);

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post(INQUIRE_CUSTOMER_URL + defaultCustomer.getCustomerNumber() + "/inquire-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-001")
                        .content(requestBody))
                .andExpect(status().isOk())
                // Asserting that your Controller successfully maps the mainframe data to clean JSON
                .andExpect(jsonPath("$.body.operationResponse.output.responseCode").value("0"))
                .andExpect(jsonPath("$.body.operationResponse.output.responseDescription").value("SUCCESS"));
    }

    @Test
    void testInquireCustomer_Success_InactiveAccount() throws Exception {
        var mockSoapOutput = new WsrvalngOutput();

        //simulate different account state from the mainframe
        mockSoapOutput.setWsoRmacctnoFromCustNbr("9876543210");

        mockSoapOutput.setWsoFiller1("INACTIVE_FILLER_DATA");

        when(inquireCustomerSoapApi.callSoapService(any())).thenReturn(mockSoapOutput);

        String requestBody = """
                {
                    "accountNumber": "9876543210"
                }
                """;

        mockMvc.perform(post(INQUIRE_CUSTOMER_URL + defaultCustomer.getCustomerNumber() + "/inquire-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-002")
                        .header("resourceOwnerID", "user-001")
                        .content(requestBody))
                .andExpect(status().isOk());

    }

}