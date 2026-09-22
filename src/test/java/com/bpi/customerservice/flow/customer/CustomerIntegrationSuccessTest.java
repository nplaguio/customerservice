package com.bpi.customerservice.flow.customer;

import com.bpi.customerservice.CustomerserviceApplicationTests;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;
import com.bpi.customerservice.service.ws.impl.InquireBranchWsServiceImpl;
import com.bpi.framework.security.policies.AccessPoliciesAspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.CoreMatchers.anything;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerIntegrationSuccessTest extends CustomerserviceApplicationTests {

    @Autowired
    private InquireBranchWsServiceImpl inquireBranchWsServiceImpl;

    private MockRestServiceServer mockServer;

    @MockitoBean
    private AccessPoliciesAspect accessPoliciesAspect;

    @BeforeEach
    void setUpMockServer() {
        mockServer = MockRestServiceServer.createServer(inquireBranchWsServiceImpl);
    }

    @AfterEach
    void tearDownMockServer() {
        mockServer.reset();
    }

    @Test
    void testInquireBranch_Success() throws Exception {
        InquireBranchWsResponse.InquireBranchOutput output = new InquireBranchWsResponse.InquireBranchOutput();
        output.setAccountNumber("1234567890");
        output.setControl2("00");
        output.setFileStat("A");
        output.setAccountControls("0100260100000001234567890");
        output.setRealBranch("0601");
        output.setBranchName("CUBAO-P. TUAZON");
        output.setResponseCode("0");
        output.setResponseDescription("VALID ACCT");

        InquireBranchWsResponse.InquireBranchOperationResponse opResponse =
                new InquireBranchWsResponse.InquireBranchOperationResponse(output);

        InquireBranchWsResponse mockResponse = new InquireBranchWsResponse();
        mockResponse.setInquireBranchOperationResponse(opResponse);

        String jsonResponse = objectMapper.writeValueAsString(mockResponse);

        mockServer.expect(requestTo(anything()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/091230/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-000")
                        .header("resourceOwnerID", "user-000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.operationResponse.output.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.body.operationResponse.output.realBranch").value("0601"))
                .andExpect(jsonPath("$.body.operationResponse.output.branchName").value("CUBAO-P. TUAZON"))
                .andExpect(jsonPath("$.body.operationResponse.output.responseCode").value("0"))
                .andExpect(jsonPath("$.body.operationResponse.output.responseDescription").value("VALID ACCT"));

        mockServer.verify();
    }

    @Test
    void testCreateCustomer_Success() throws Exception {
        String requestBody = """
                {
                    "customerNumber": "123",
                    "accountNumber": "1234567890",
                    "firstName": "John",
                    "lastName": "Doe",
                    "address": "Manila",
                    "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("123"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.address").value("Manila"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));

        assertEquals(2, customerRepository.count());
        assertEquals(1, auditLogRepository.count());
    }

    @Test
    void testGetCustomer_Success() throws Exception {
        mockMvc.perform(get("/api/customers/" + defaultCustomer.getCustomerNumber())
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-002")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("091230"))
                .andExpect(jsonPath("$.firstName").value("Default"))
                .andExpect(jsonPath("$.lastName").value("Customer"))
                .andExpect(jsonPath("$.address").value("Makati"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-01"));
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        String requestBody = """
                {
                    "customerNumber": "091230",
                    "accountNumber": "1234567890",
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "address": "Cebu",
                    "birthDate": "1995-05-05"
                }
                """;

        mockMvc.perform(put("/api/customers/" + defaultCustomer.getCustomerNumber())
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-004")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerNumber").value("091230"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.address").value("Cebu"))
                .andExpect(jsonPath("$.birthDate").value("1995-05-05"));
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/api/customers/" + defaultCustomer.getCustomerNumber())
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-005")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isOk());

        assertEquals(0, customerRepository.count());
    }


}