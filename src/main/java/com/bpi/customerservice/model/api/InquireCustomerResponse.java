package com.bpi.customerservice.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquireCustomerResponse {

    @JsonProperty("operationResponse")
    private InquireCustomerOperationResponse inquireCustomerOperationResponse;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquireCustomerOperationResponse {
        @JsonProperty("output")
        private InquireCustomerOutput inquireCustomerOutput;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquireCustomerOutput {

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("accountType")
        private String accountType;

        @JsonProperty("isValid")
        private String isValid;

        @JsonProperty("responseCode")
        private String responseCode;

        @JsonProperty("responseDescription")
        private String responseDescription;
    }
}