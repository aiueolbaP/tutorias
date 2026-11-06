package com.flores.tutorias.importacion;

import com.flores.tutorias.dto.TutoriaForm;
import com.flores.tutorias.dto.UsuarioForm;
import com.flores.tutorias.model.*;
import com.flores.tutorias.repository.*;
import com.flores.tutorias.service.TutoriaService;
import com.flores.tutorias.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Importación masiva. Política: TODO O NADA. Si una sola fila tiene error, se
 * revierte todo y se devuelve la lista de errores con su número de fila.
 *
 * Las referencias entre tablas se resuelven por clave natural (nombre de la
 * carrera, correo del tutor...), nunca por id. Y cada fila pasa por las MISMAS
 * validaciones de negocio que los formularios (UsuarioService / TutoriaService).
 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private static final int MAX_FILAS = 5000;

    private final ArchivoParser parser;
    private final TagRepository tagRepo;
    private final CarreraRepository carreraRepo;
    private final MateriaRepository materiaRepo;
    private final UsuarioRepository usuarioRepo;
    private final UsuarioService usuarioService;
    private final TutoriaService tutoriaService;
    private final LocalValidatorFactoryBean validator;

    @Transactional
    public ResultadoImport importar(TipoImport tipo, MultipartFile archivo) {
        List<Map<String, String>> filas;
        try {
            filas = parser.parse(archivo);
        } catch (Exception e) {
            return ResultadoImport.error("No se pudo leer el archivo: " + e.getMessage());
        }
        if (filas.isEmpty()) return ResultadoImport.error("El archivo no contiene filas de datos");
        if (filas.size() > MAX_FILAS) return ResultadoImport.error("Máximo " + MAX_FILAS + " filas por archivo");

        List<String> errores = new ArrayList<>();
        int creados = 0, actualizados = 0;

        for (int i = 0; i < filas.size(); i++) {
            int linea = i + 2; // fila 1 para encabezados
            Map<String, String> f = filas.get(i);
            try {
                boolean nuevo = switch (tipo) {
                    case TAGS -> importarTag(f);
                    case CARRERAS -> importarCarrera(f);
                    case MATERIAS -> importarMateria(f);
                    case USUARIOS -> importarUsuario(f);
                    case TUTORIAS -> importarTutoria(f);
                };
                if (nuevo) creados++; else actualizados++;
            } catch (RuntimeException e) {
                errores.add("Fila " + linea + ": " + e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ResultadoImport(0, 0, errores);
        }
        return new ResultadoImport(creados, actualizados, List.of());
    }

    // ---------------- por tipo ----------------

    private boolean importarTag(Map<String, String> f) {
        String nombre = norm(req(f, "nombre"));
        if (tagRepo.existsByNombreIgnoreCase(nombre)) return false;
        crearTag(nombre);
        return true;
    }

    private boolean importarCarrera(Map<String, String> f) {
        String nombre = req(f, "nombre");
        Carrera c = carreraRepo.findByNombreIgnoreCase(nombre).orElse(null);
        boolean nueva = c == null;
        if (nueva) {
            c = new Carrera();
            c.setNombre(nombre);
        }
        if (f.containsKey("tags")) c.setTags(resolverTags(f.get("tags")));
        carreraRepo.save(c);
        return nueva;
    }

    private boolean importarMateria(Map<String, String> f) {
        String nombre = req(f, "nombre");
        Materia m = materiaRepo.findByNombreIgnoreCase(nombre).orElse(null);
        boolean nueva = m == null;
        if (nueva) {
            m = new Materia();
            m.setNombre(nombre);
        }
        if (f.containsKey("descripcion")) m.setDescripcion(f.get("descripcion"));
        if (f.containsKey("tags")) m.setTags(resolverTags(f.get("tags")));
        if (f.containsKey("carrera") && !f.get("carrera").isBlank()) {
            carreraRepo.findByNombreIgnoreCase(f.get("carrera").trim()).ifPresent(m::setCarrera);
        }
        materiaRepo.save(m);
        return nueva;
    }

    private boolean importarUsuario(Map<String, String> f) {
        UsuarioForm form = new UsuarioForm();
        form.setNombre(req(f, "nombre"));
        form.setEmail(req(f, "email").toLowerCase());
        form.setPassword(f.getOrDefault("password", ""));
        form.setActivo(parseBool(f.getOrDefault("activo", "true")));

        try {
            form.setRol(Rol.valueOf(req(f, "rol").toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("rol inválido (usa ESTUDIANTE, TUTOR o ADMIN)");
        }

        String carrera = f.get("carrera");
        if (carrera != null && !carrera.isBlank()) {
            form.setCarreraId(carreraRepo.findByNombreIgnoreCase(carrera.trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "la carrera '" + carrera + "' no existe (impórtala primero)"))
                    .getId());
        }

        Optional<Usuario> existente = usuarioRepo.findByEmail(form.getEmail());
        existente.ifPresent(u -> form.setId(u.getId())); // mismo correo = actualiza

        BindingResult br = new BeanPropertyBindingResult(form, "usuarioForm");
        validator.validate(form, br);
        usuarioService.validarNegocio(form, br);
        lanzarSiHayErrores(br);

        usuarioService.guardar(form);
        return existente.isEmpty();
    }

    private boolean importarTutoria(Map<String, String> f) {
        TutoriaForm form = new TutoriaForm();
        form.setTitulo(req(f, "titulo"));
        form.setDescripcion(f.get("descripcion"));

        String materia = req(f, "materia");
        form.setMateriaId(materiaRepo.findByNombreIgnoreCase(materia)
                .orElseThrow(() -> new IllegalArgumentException("la materia '" + materia + "' no existe"))
                .getId());

        String correo = req(f, "tutor_email").toLowerCase();
        form.setTutorId(usuarioRepo.findByEmail(correo)
                .orElseThrow(() -> new IllegalArgumentException("no existe el tutor '" + correo + "'"))
                .getId());

        form.setFecha(parsear(req(f, "fecha"), LocalDate::parse, "yyyy-MM-dd"));
        form.setHoraInicio(parsear(req(f, "hora_inicio"), LocalTime::parse, "HH:mm"));
        form.setHoraFin(parsear(req(f, "hora_fin"), LocalTime::parse, "HH:mm"));
        form.setCupo(parsear(req(f, "cupo"), Integer::valueOf, "un número entero"));
        form.setEnlaceReunion(req(f, "enlace_reunion"));
        form.setDireccionFisica(f.get("direccion_fisica"));

        BindingResult br = new BeanPropertyBindingResult(form, "tutoriaForm");
        validator.validate(form, br);
        tutoriaService.validarNegocio(form, br);
        lanzarSiHayErrores(br);

        tutoriaService.guardar(form);
        return true; // si se reimporta el mismo archivo, el solape de horario lo rechaza
    }

    // ---------------- utilidades ----------------

    private Set<Tag> resolverTags(String crudo) {
        Set<Tag> tags = new HashSet<>();
        for (String t : crudo.split("[;,|]")) {
            String nombre = norm(t);
            if (nombre.isBlank()) continue;
            tags.add(tagRepo.findByNombreIgnoreCase(nombre).orElseGet(() -> crearTag(nombre)));
        }
        return tags;
    }

    private Tag crearTag(String nombre) {
        Tag t = new Tag();
        t.setNombre(nombre);
        return tagRepo.save(t);
    }

    private String norm(String s) {
        return s.trim().toLowerCase();
    }

    private String req(Map<String, String> f, String col) {
        String v = f.get(col);
        if (v == null || v.isBlank()) throw new IllegalArgumentException("la columna '" + col + "' es obligatoria");
        return v.trim();
    }

    private <T> T parsear(String valor, Function<String, T> fn, String esperado) {
        try {
            return fn.apply(valor);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("valor inválido '" + valor + "', se esperaba " + esperado);
        }
    }

    private boolean parseBool(String s) {
        return Set.of("true", "1", "si", "sí", "yes").contains(s.trim().toLowerCase());
    }

    private void lanzarSiHayErrores(BindingResult br) {
        if (br.hasErrors()) {
            throw new IllegalArgumentException(br.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .distinct().collect(Collectors.joining("; ")));
        }
    }
}
