package com.InterestRatesAustria.InterestRatesAustria.config;

import com.InterestRatesAustria.InterestRatesAustria.service.LastUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UpdateTrackingInterceptor implements HandlerInterceptor {

    private final LastUpdateService lastUpdateService;

    public UpdateTrackingInterceptor(LastUpdateService lastUpdateService) {
        this.lastUpdateService = lastUpdateService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {

        // Only track successful requests (status 2xx or 3xx)
        if (response.getStatus() < 400) {
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) &&
                !isExcludedPath(uri)) {
                
                lastUpdateService.recordUpdate();
            }
        }
    }
    
    private boolean isExcludedPath(String uri) {
        return uri.startsWith("/api/files") ||
               uri.startsWith("/static") ||
               uri.startsWith("/css") ||
               uri.startsWith("/js") ||
               uri.startsWith("/images") ||
               uri.contains("/error") ||
               uri.contains("/health") ||
               uri.contains("/actuator");
    }
}