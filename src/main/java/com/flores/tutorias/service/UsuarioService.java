package com.flores.tutorias.service;

import com.flores.tutorias.dto.UsuarioForm;
import com.flores.tutorias.model.Carrera;
import com.flores.tutorias.model.Rol;
import com.flores.tutorias.model.Usuario;
import com.flores.tutorias.repository.CarreraRepository;
import com.flores.tutorias.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final CarreraRepository carreraRepo;
    private final PasswordEncoder encoder;

    @Value("${app.dominio-institucional}") //para el correo institucional, está en el application.properties
    private String dominio;

    public List<Usuario> listar() {
        return repo.findAll(Sort.by("nombre"));
    }

    // opciones del dropdown de carreras
    public List<Carrera> carreras() {
        return carreraRepo.findAll(Sort.by("nombre"));
    }

    public UsuarioForm obtenerForm(Long id) {
        Usuario u = repo.findById(id).orElseThrow();
        UsuarioForm f = new UsuarioForm();
        f.setId(u.getId());
        f.setNombre(u.getNombre());
        f.setEmail(u.getEmail());
        f.setRol(u.getRol());
        f.setCarreraId(u.getCarrera() == null ? null : u.getCarrera().getId());
        f.setActivo(u.isActivo());
        return f;
    }

    public void validarNegocio(UsuarioForm f, BindingResult result) {
        boolean esNuevo = f.getId() == null;

        if (esNuevo && (f.getPassword() == null || f.getPassword().isBlank())) {
            result.rejectValue("password", "requerida", "La contraseña es obligatoria");
        } //contraseña obligatoria

        if (!result.hasFieldErrors("email")) {
            String email = f.getEmail().trim().toLowerCase();
            repo.findByEmail(email).ifPresent(existente -> {
                if (!existente.getId().equals(f.getId())) {
                    result.rejectValue("email", "duplicado", "Ya existe un usuario con ese correo");
                }
            });

            if (f.getRol() != null && f.getRol() != Rol.ADMIN && !email.endsWith("@" + dominio)) {
                result.rejectValue("email", "dominio",
                        "Estudiantes y tutores deben usar su correo institucional (@" + dominio + ")");
            }
        }

        if (f.getRol() != null && f.getRol() != Rol.ADMIN) {
            if (f.getCarreraId() == null) {
                result.rejectValue("carreraId", "requerida", "Selecciona la carrera");
            } else if (!carreraRepo.existsById(f.getCarreraId())) {
                result.rejectValue("carreraId", "invalida", "La carrera seleccionada no existe");
            }
        }
    }

    @Transactional
    public void guardar(UsuarioForm f) {
        Usuario u = (f.getId() == null) ? new Usuario() : repo.findById(f.getId()).orElseThrow();
        u.setNombre(f.getNombre().trim());
        u.setEmail(f.getEmail().trim().toLowerCase());
        u.setRol(f.getRol());
        u.setCarrera(f.getRol() == Rol.ADMIN ? null : carreraRepo.getReferenceById(f.getCarreraId()));
        u.setActivo(f.isActivo());
        if (f.getPassword() != null && !f.getPassword().isBlank()) {
            u.setPassword(encoder.encode(f.getPassword()));
        }
        repo.save(u);
    }

    // no se elimina la propia cuenta
    public boolean eliminar(Long id, String emailActual) {
        Usuario u = repo.findById(id).orElseThrow();
        if (u.getEmail().equals(emailActual)) {
            return false;
        }
        repo.delete(u);
        return true;
    }
}
