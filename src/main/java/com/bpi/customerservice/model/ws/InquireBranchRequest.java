package com.bpi.customerservice.model.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InquireBranchRequest {

    @JsonProperty("WSIDCTLPOperation")
    public InquireBranchOperationRequest inquireBranchOperationRequest;

    @Data
    public static class InquireBranchOperationRequest {
        @JsonProperty("wsidctlp_input")
        private InquireBranchInput inquireBranchInput;
    }

    @Data
    public static class InquireBranchInput {
        @JsonProperty("ws_in_account")
        private String accountNumber;

        @JsonProperty("ws_in_ctl2")
        private String wsInCtl2;
    }
}