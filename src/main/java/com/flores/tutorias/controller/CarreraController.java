package com.flores.tutorias.controller;

import com.flores.tutorias.dto.CarreraForm;
import com.flores.tutorias.service.CarreraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/carreras")
@RequiredArgsConstructor
public class CarreraController {


    private final CarreraService carreraService;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("carreras", carreraService.listar());
        return "admin/carreras";
    }

    @GetMapping("/lista")
    public String lista(Model model) {
        model.addAttribute("carreras", carreraService.listar());
        return "admin/carreras :: lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("carreraForm", new CarreraForm());
        cargarCombos(model);
        return "admin/carrera-form :: form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("carreraForm", carreraService.obtenerForm(id));
        cargarCombos(model);
        return "admin/carrera-form :: form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("carreraForm") CarreraForm form,
                          BindingResult result, Model model) {
        carreraService.validarNegocio(form, result);
        if (result.hasErrors()) {
            cargarCombos(model);
            return "admin/carrera-form :: form";
        }
        carreraService.guardar(form);
        model.addAttribute("carreras", carreraService.listar());
        return "admin/carreras :: lista";
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        String error = carreraService.eliminar(id);
        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("carreras", carreraService.listar());
        return "admin/carreras :: lista";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("tags", carreraService.todosLosTags());
    }
}
