package com.flores.tutorias.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio(Authentication auth) {
        String rol = auth.getAuthorities().iterator().next().getAuthority();
        return switch (rol) {
            case "ROLE_ADMIN" -> "redirect:/admin/panel";
            case "ROLE_TUTOR" -> "redirect:/tutor/panel";
            default -> "redirect:/estudiante/panel";
        };
    }
}
