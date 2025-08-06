package com.InterestRatesAustria.InterestRatesAustria.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            // Add common attributes to model
            model.addAttribute("status", statusCode);
            model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            model.addAttribute("timestamp", java.time.LocalDateTime.now());
            
            // Get additional error details
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            
            model.addAttribute("message", errorMessage != null ? errorMessage : "An unexpected error occurred");
            model.addAttribute("path", requestUri != null ? requestUri : "Unknown");

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorTitle", "Page Not Found");
                model.addAttribute("errorDescription", "The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.");
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorTitle", "Access Forbidden");
                model.addAttribute("errorDescription", "You don't have permission to access this resource.");
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorTitle", "Internal Server Error");
                model.addAttribute("errorDescription", "Something went wrong on our end. Please try again later.");
                return "error/500";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                model.addAttribute("errorTitle", "Bad Request");
                model.addAttribute("errorDescription", "The request could not be understood by the server.");
                return "error/400";
            }
        }
        
        // Default error page for other status codes
        model.addAttribute("errorTitle", "An Error Occurred");
        model.addAttribute("errorDescription", "Something went wrong. Please try again later.");
        return "error/default";
    }
}