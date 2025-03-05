package com.carabuena.it342midterm.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showIndex(@AuthenticationPrincipal OAuth2User principal, Model model) {
        String name = (principal != null && principal.getAttributes().containsKey("name"))
                ? principal.getAttributes().get("name").toString()
                : "Unknown User";
        model.addAttribute("fullName", name);
        return "index";
    }
}