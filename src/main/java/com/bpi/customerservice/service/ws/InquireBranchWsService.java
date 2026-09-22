package com.bpi.customerservice.service.ws;

import com.bpi.customerservice.model.ws.InquireBranchRequest;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;

public interface InquireBranchWsService {

    InquireBranchWsResponse callBranchService (InquireBranchRequest request);

}