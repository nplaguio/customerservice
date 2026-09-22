package com.bpi.customerservice.config;

import com.bpi.customerservice.config.properties.InquireBranchSoapConfigProperties;
import com.bpi.framework.soap.configurer.client.SoapApiConfigurerComponent;
import com.wsrvalni.wsrvalng.WSRVALNGPort;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class SoapEndpointConfiguration {

    private SoapApiConfigurerComponent soapApiComponent;

    private InquireBranchSoapConfigProperties soapConfigProperties;

    public static final String WSRVALNG_PORT_NAME = "wsrvalngPort";

    @Bean(name = WSRVALNG_PORT_NAME)
    public WSRVALNGPort wsrvalngPortProxy() {
        return soapApiComponent.configure(soapConfigProperties, WSRVALNGPort.class);

    }

}