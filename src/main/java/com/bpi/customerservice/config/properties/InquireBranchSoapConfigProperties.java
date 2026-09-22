package com.bpi.customerservice.config.properties;

import com.bpi.framework.soap.configproperties.SoapApiConfigProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(prefix = "bpi.soap.wsrvalng")
public class InquireBranchSoapConfigProperties extends SoapApiConfigProperties {
}