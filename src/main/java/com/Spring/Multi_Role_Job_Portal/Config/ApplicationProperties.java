package com.Spring.Multi_Role_Job_Portal.Config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "application")
@Validated
@Getter
@Setter
public class ApplicationProperties {

    @Min(0)
    private int rejectionCooldownDays = 30;
}
