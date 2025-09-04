package com.InterestRatesAustria.InterestRatesAustria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UpdateTrackingInterceptor updateTrackingInterceptor;

    public WebConfig(UpdateTrackingInterceptor updateTrackingInterceptor) {
        this.updateTrackingInterceptor = updateTrackingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(updateTrackingInterceptor);
    }
}