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

class BranchIntegrationErrorTest extends CustomerserviceApplicationTests {

    @MockitoBean
    private InquireBranchWsService inquireBranchWsService;

    @Test
    void testBranchSoapServiceDown_ThrowsBranchServiceException() throws Exception {
        when(inquireBranchWsService.callBranchService(any())).thenThrow(new RuntimeException("Branch API Timeout"));

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-002")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testBranchInvalidResponseCode_ThrowsInvalidAccountException() throws Exception {
        InquireBranchWsResponse mockBadResponse = new InquireBranchWsResponse();
        InquireBranchWsResponse.InquireBranchOperationResponse opResponse = new InquireBranchWsResponse.InquireBranchOperationResponse();
        InquireBranchWsResponse.InquireBranchOutput output = new InquireBranchWsResponse.InquireBranchOutput();

        output.setResponseCode("1");
        opResponse.setInquireBranchOutput(output);
        mockBadResponse.setInquireBranchOperationResponse(opResponse);

        when(inquireBranchWsService.callBranchService(any())).thenReturn(mockBadResponse);

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-003")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGlobalExceptionHandler_MethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-004")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBranchNullResponse_ThrowsInvalidAccountException() throws Exception {
        // Forces branchResponse == null to evaluate true
        when(inquireBranchWsService.callBranchService(any())).thenReturn(null);

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/" + defaultCustomer.getCustomerNumber() + "/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-005")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

}