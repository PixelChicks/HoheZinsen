package com.InterestRatesAustria.InterestRatesAustria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web controller for serving authentication pages.
 */
@Controller
@RequestMapping("/auth")
public class AuthWebController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/verify-success")
    public String verifySuccessPage() {
        return "auth/verify-success";
    }

    @GetMapping("/verify-error")
    public String verifyErrorPage(@RequestParam(required = false) String message) {
        return "auth/verify-error";
    }
}