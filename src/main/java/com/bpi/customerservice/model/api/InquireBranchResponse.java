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
public class InquireBranchResponse {

    @JsonProperty("operationResponse")
    private InquireBranchOperationResponse inquireBranchOperationResponse;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquireBranchOperationResponse {
        @JsonProperty("output")
        private InquireBranchOutput inquireBranchOutput;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquireBranchOutput {

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("control2")
        private String control2;

        @JsonProperty("fileStatus")
        private String fileStat;

        @JsonProperty("accountControls") // flattened
        private String accountControls;

        @JsonProperty("realBranch")
        private String realBranch;

        @JsonProperty("branchName")
        private String branchName;

        @JsonProperty("responseCode")
        private String responseCode;

        @JsonProperty("responseDescription")
        private String responseDescription;

    }

}