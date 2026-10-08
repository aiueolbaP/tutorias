package com.flores.tutorias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    @GetMapping("/admin/panel")
    public String admin(Model model) {
        model.addAttribute("titulo", "Panel de Administrador");
        return "panel";
    }

    @GetMapping("/tutor/panel")
    public String tutor(Model model) {
        model.addAttribute("titulo", "Panel de Tutor");
        return "panel";
    }

    @GetMapping("/estudiante/panel")
    public String estudiante(Model model) {
        model.addAttribute("titulo", "Panel de Estudiante");
        return "panel";
    }
}