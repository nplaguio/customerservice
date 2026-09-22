package com.bpi.customerservice.service.ws.impl;

import com.bpi.customerservice.config.properties.InquireBranchApiConfigProperties;
import com.bpi.customerservice.model.ws.InquireBranchRequest;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.service.ws.InquireBranchWsService;
import com.bpi.framework.web.component.client.AbstractErrorAwareRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

@Service
public class InquireBranchWsServiceImpl extends AbstractErrorAwareRestTemplate implements InquireBranchWsService {

    private InquireBranchApiConfigProperties configProperties;

    private static final DefaultError DEFAULT_ERROR = new DefaultError(
            CustomerServiceErrorCode.SERVICE_ERROR,
            CustomerServiceErrorCode.SERVICE_ERROR
    );

    @Autowired
    public void setConfigProperties (InquireBranchApiConfigProperties configProperties) {

        this.configProperties = configProperties;

    }

    public InquireBranchWsServiceImpl() {
        super(DEFAULT_ERROR);
    }

    @Override
    public InquireBranchWsResponse callBranchService(InquireBranchRequest request) {
        HttpEntity<InquireBranchRequest> httpEntity = new HttpEntity<>(request);
        return post(configProperties.getBasePath(), httpEntity, InquireBranchWsResponse.class).getBody();
    }
}