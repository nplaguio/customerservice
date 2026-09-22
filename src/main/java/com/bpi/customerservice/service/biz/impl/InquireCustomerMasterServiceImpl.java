package com.bpi.customerservice.service.biz.impl;

import com.bpi.customerservice.converter.InquireCustomerSoapResponseConverter;
import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.InquireCustomerBizDto;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.BranchServiceException;
import com.bpi.customerservice.exception.InvalidAccountException;
import com.bpi.customerservice.service.biz.InquireCustomerMasterService;
import com.bpi.customerservice.service.ws.InquireCustomerSoapService;
import com.request.wsrvalni.wsrvalng.ProgramInterface.WsrvalngInput;
import com.response.wsrvalni.wsrvalng.ProgramInterface.WsrvalngOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquireCustomerMasterServiceImpl implements InquireCustomerMasterService {

    private final InquireCustomerSoapService inquireCustomerSoapApi;
    private final ConversionService conversionService;

    @Override
    public InquireCustomerResponse executeCustomerInquiry(InquireCustomerBizDto bizDto) {

        String accountNumber = bizDto.getAccountNumber();

        //soap request mapped here in biz layer
        WsrvalngInput requestInput = new WsrvalngInput();
        requestInput.setWsiRmacctnoFromCustNbr(accountNumber);

        WsrvalngOutput soapResponse;

        try {

            soapResponse = inquireCustomerSoapApi.callSoapService(requestInput);
        } catch (Exception e) {
            throw new BranchServiceException(CustomerServiceErrorCode.SERVICE_ERROR);
        }

        if (soapResponse == null) {
            throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
        }

        var context = new InquireCustomerSoapResponseConverter.InquiryContext(soapResponse, accountNumber);

        return conversionService.convert(context, InquireCustomerResponse.class);
    }
}