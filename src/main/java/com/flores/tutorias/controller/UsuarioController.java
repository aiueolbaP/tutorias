package com.flores.tutorias.controller;

import com.flores.tutorias.dto.UsuarioForm;
import com.flores.tutorias.model.Rol;
import com.flores.tutorias.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios";
    }

    @GetMapping("/lista")
    public String lista(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios :: lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("roles", Rol.values());
        return "admin/usuario-form :: form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuarioForm", usuarioService.obtenerForm(id));
        model.addAttribute("roles", Rol.values());
        return "admin/usuario-form :: form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
                          BindingResult result, Model model) {
        usuarioService.validarNegocio(form, result);
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "admin/usuario-form :: form";
        }
        usuarioService.guardar(form);
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios :: lista";
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, Principal principal, Model model) {
        if (!usuarioService.eliminar(id, principal.getName())) {
            model.addAttribute("error", "No puedes eliminar tu propia cuenta");
        }
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios :: lista";
    }
}