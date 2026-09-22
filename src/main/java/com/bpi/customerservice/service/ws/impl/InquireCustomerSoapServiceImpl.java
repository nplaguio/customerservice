package com.bpi.customerservice.service.ws.impl;

import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.BranchServiceException;
import com.bpi.customerservice.service.ws.InquireCustomerSoapService;
import com.request.wsrvalni.wsrvalng.ProgramInterface.WsrvalngInput;
import com.response.wsrvalni.wsrvalng.ProgramInterface.WsrvalngOutput;
import com.wsrvalni.wsrvalng.WSRVALNGPort;
import jakarta.xml.ws.WebServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquireCustomerSoapServiceImpl implements InquireCustomerSoapService {

    private final WSRVALNGPort port;

    @Override
    public WsrvalngOutput callSoapService (WsrvalngInput requestInput) {
        try {
            //starts exactly here as requested
            log.info("Calling WSRVALNG SOAP service for account number: {}", requestInput.getWsiRmacctnoFromCustNbr());
            WsrvalngOutput response = port.wsrvalngOperation(requestInput);

            if (response == null) {
                log.warn("WSRVALNG SOAP service returned a null response");
                return null;
            }

            return response;

        } catch (WebServiceException e) {
            log.error("SOAP communication error occurred while calling WSRVALNG service", e);
            throw new BranchServiceException(CustomerServiceErrorCode.SERVICE_ERROR);
        }
    }
}