package com.bpi.customerservice.flow.branch;

import com.bpi.customerservice.CustomerserviceApplicationTests;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;
import com.bpi.customerservice.service.ws.InquireBranchWsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BranchIntegrationSuccessTest extends CustomerserviceApplicationTests {

    @MockitoBean
    private InquireBranchWsService inquireBranchWsService;

    @Test
    void testBranchInquiry_Success_Returns200OK() throws Exception {
        InquireBranchWsResponse mockResponse = new InquireBranchWsResponse();
        InquireBranchWsResponse.InquireBranchOperationResponse opResponse = new InquireBranchWsResponse.InquireBranchOperationResponse();
        InquireBranchWsResponse.InquireBranchOutput output = new InquireBranchWsResponse.InquireBranchOutput();

        output.setResponseCode("0");
        output.setAccountNumber("0987654321");
        output.setBranchName("MAKATI MAIN");

        opResponse.setInquireBranchOutput(output);
        mockResponse.setInquireBranchOperationResponse(opResponse);

        when(inquireBranchWsService.callBranchService(any())).thenReturn(mockResponse);

        String requestBody = """
                {
                    "accountNumber": "0987654321"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.operationResponse.output.accountNumber").value("0987654321"))
                .andExpect(jsonPath("$.body.operationResponse.output.branchName").value("MAKATI MAIN"))
                .andExpect(jsonPath("$.body.operationResponse.output.responseCode").value("0"));
    }
}