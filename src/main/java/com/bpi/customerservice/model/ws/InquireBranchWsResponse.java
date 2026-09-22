package com.bpi.customerservice.model.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquireBranchWsResponse {

    @JsonProperty("WSIDCTLPOperationResponse")
    private InquireBranchOperationResponse inquireBranchOperationResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquireBranchOperationResponse{
        @JsonProperty("wsidctlp_output")
        private InquireBranchOutput inquireBranchOutput;
    }

    @Data
    @NoArgsConstructor
    public static class InquireBranchOutput{
        @JsonProperty("ws_out_in_account")
        private String accountNumber;

        @JsonProperty("ws_out_in_ctl2")
        private String control2;

        @JsonProperty("ws_out_stlkp_file_stat")
        private String fileStat;

        @JsonProperty("ws_out_acct_ctls")
        private String accountControls;

        @JsonProperty("ws_out_real_branch")
        private String realBranch;

        @JsonProperty("ws_out_branch_name")
        private String branchName;

        @JsonProperty("ws_out_reply_code")
        private String responseCode;

        @JsonProperty("ws_out_reply_desc")
        private String responseDescription;
    }
}