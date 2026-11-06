package com.flores.tutorias.controller;

import com.flores.tutorias.dto.MateriaForm;
import com.flores.tutorias.service.MateriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService materiaService;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias";
    }

    @GetMapping("/lista")
    public String lista(Model model) {
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias :: lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("materiaForm", new MateriaForm());
        cargarCombos(model);
        return "admin/materia-form :: form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("materiaForm", materiaService.obtenerForm(id));
        cargarCombos(model);
        return "admin/materia-form :: form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("materiaForm") MateriaForm form,
                          BindingResult result, Model model) {
        materiaService.validarNegocio(form, result);
        if (result.hasErrors()) {
            cargarCombos(model);
            return "admin/materia-form :: form";
        }
        materiaService.guardar(form);
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias :: lista";
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        String error = materiaService.eliminar(id);
        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("materias", materiaService.listar());
        return "admin/materias :: lista";
    }

    private void cargarCombos(Model model) {
        // FK a carreras para el dropdown
        model.addAttribute("carreras", materiaService.carreras());
        model.addAttribute("tags", materiaService.todosLosTags());
    }
}
