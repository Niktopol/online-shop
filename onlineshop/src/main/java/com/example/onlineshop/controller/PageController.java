package com.example.onlineshop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream()
                .noneMatch(auth -> auth.toString().equals("ROLE_ANONYMOUS"))) {
            return "redirect:/";
        }
        return "signin";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
