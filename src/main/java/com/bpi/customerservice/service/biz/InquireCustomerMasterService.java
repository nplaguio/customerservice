package com.bpi.customerservice.service.biz;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.InquireCustomerBizDto;

public interface InquireCustomerMasterService {

    InquireCustomerResponse executeCustomerInquiry(InquireCustomerBizDto bizDto);

}