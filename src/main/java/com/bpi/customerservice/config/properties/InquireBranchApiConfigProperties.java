package com.bpi.customerservice.config.properties;

import com.bpi.framework.web.configproperties.HttpApiConfigProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EqualsAndHashCode(callSuper = false)
@ConfigurationProperties(prefix = "bpi.api.inquire-branch")
public class InquireBranchApiConfigProperties extends HttpApiConfigProperties {

    @NotBlank
    private String basePath;

    private int connectTimeout;

    private int socketTimeout;

}