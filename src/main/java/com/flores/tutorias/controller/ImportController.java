package com.flores.tutorias.controller;

import com.flores.tutorias.importacion.ImportService;
import com.flores.tutorias.importacion.TipoImport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/importar")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("tipos", TipoImport.values());
        return "admin/importar";
    }

    @PostMapping
    public String importar(@RequestParam TipoImport tipo,
                           @RequestParam("archivo") MultipartFile archivo, Model model) {
        model.addAttribute("resultado", importService.importar(tipo, archivo));
        return "admin/importar :: resultado";
    }
}
