package com.bpi.customerservice.converter;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.response.wsrvalni.wsrvalng.ProgramInterface.WsrvalngOutput;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class InquireCustomerSoapResponseConverter implements Converter<InquireCustomerSoapResponseConverter.InquiryContext, InquireCustomerResponse> {

    public record InquiryContext(WsrvalngOutput soapOutput, String accountNumber) {}

    @Override
    public InquireCustomerResponse convert(@NonNull InquiryContext source) {

        String respCode = source.soapOutput() != null ? "0" : "1";

        InquireCustomerResponse.InquireCustomerOutput output = InquireCustomerResponse.InquireCustomerOutput.builder()
                .accountNumber(source.accountNumber())
                .accountType("SA")
                .isValid("0".equals(respCode) ? "Y" : "N")
                .responseCode(respCode)
                .responseDescription("0".equals(respCode) ? "SUCCESS" : "ERROR")
                .build();

        return InquireCustomerResponse.builder()
                .inquireCustomerOperationResponse(
                        InquireCustomerResponse.InquireCustomerOperationResponse.builder()
                                .inquireCustomerOutput(output)
                                .build()
                )
                .build();

    }

}