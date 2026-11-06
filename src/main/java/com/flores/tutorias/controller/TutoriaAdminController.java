package com.flores.tutorias.controller;

import com.flores.tutorias.dto.TutoriaForm;
import com.flores.tutorias.service.TutoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tutorias")
@RequiredArgsConstructor
public class TutoriaAdminController {

    private final TutoriaService service;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("tutorias", service.listar());
        return "admin/tutorias";
    }

    @GetMapping("/lista")
    public String lista(Model model) {
        model.addAttribute("tutorias", service.listar());
        return "admin/tutorias :: lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        TutoriaForm form = new TutoriaForm();
        model.addAttribute("tutoriaForm", form);
        cargarCombos(model, form);
        return "admin/tutoria-form :: form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        TutoriaForm form = service.obtenerForm(id);
        model.addAttribute("tutoriaForm", form);
        cargarCombos(model, form);
        return "admin/tutoria-form :: form";
    }

    // Devuelve las materias, si se le pasa una carrera, son de esa carrera, sino ordenadas por nombre
    //se ejecuta dentro del mismo formulario de tutorias, al dar click en materia
    @GetMapping("/materias")
    public String materias(@RequestParam(required = false) Long carreraId, Model model) {
        model.addAttribute("materias", service.materiasPorCarrera(carreraId));
        return "admin/tutoria-form :: opcionesMaterias";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("tutoriaForm") TutoriaForm form,
                          BindingResult result, Model model) {
        service.validarNegocio(form, result);
        //si el result tiene errores de campo, vuelve a cargar con su mensaje de error en el campo
        if (result.hasErrors()) {
            cargarCombos(model, form);
            return "admin/tutoria-form :: form";
        }
        service.guardar(form);
        model.addAttribute("tutorias", service.listar());
        return "admin/tutorias :: lista";
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        service.eliminar(id);
        model.addAttribute("tutorias", service.listar());
        return "admin/tutorias :: lista";
    }

    //los dropdowns de cada cosa
    private void cargarCombos(Model model, TutoriaForm form) {
        model.addAttribute("carreras", service.carreras());
        model.addAttribute("tutores", service.tutores());
        model.addAttribute("materias", service.materiasPorCarrera(form.getCarreraId()));
    }
}
