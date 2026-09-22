package com.bpi.customerservice.service.ws;

import com.request.wsrvalni.wsrvalng.ProgramInterface.WsrvalngInput;
import com.response.wsrvalni.wsrvalng.ProgramInterface.WsrvalngOutput;

public interface InquireCustomerSoapService {

    WsrvalngOutput callSoapService(WsrvalngInput requestInput);

}