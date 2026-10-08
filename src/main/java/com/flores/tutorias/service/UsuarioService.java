package com.flores.tutorias.service;

import com.flores.tutorias.dto.UsuarioForm;
import com.flores.tutorias.model.Usuario;
import com.flores.tutorias.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public List<Usuario> listar() {
        return repo.findAll(Sort.by("nombre"));
    }

    public UsuarioForm obtenerForm(Long id) {
        Usuario u = repo.findById(id).orElseThrow();
        UsuarioForm f = new UsuarioForm();
        f.setId(u.getId());
        f.setNombre(u.getNombre());
        f.setEmail(u.getEmail());
        f.setRol(u.getRol());
        f.setActivo(u.isActivo());
        return f;
    }

    //ver que la contraseña se ingrese y que no se ingresen correos duplicados
    public void validarNegocio(UsuarioForm f, BindingResult result) {
        boolean esNuevo = f.getId() == null;

        if (esNuevo && (f.getPassword() == null || f.getPassword().isBlank())) {
            result.rejectValue("password", "requerida", "La contraseña es obligatoria");
        }

        if (!result.hasFieldErrors("email")) {
            repo.findByEmail(f.getEmail()).ifPresent(existente -> {
                if (!existente.getId().equals(f.getId())) {
                    result.rejectValue("email", "duplicado", "Ya existe un usuario con ese correo");
                }
            });
        }
    }

    @Transactional
    public void guardar(UsuarioForm f) {
        Usuario u = (f.getId() == null) ? new Usuario() : repo.findById(f.getId()).orElseThrow();
        u.setNombre(f.getNombre());
        u.setEmail(f.getEmail());
        u.setRol(f.getRol());
        u.setActivo(f.isActivo());
        if (f.getPassword() != null && !f.getPassword().isBlank()) {
            u.setPassword(encoder.encode(f.getPassword()));
        }
        repo.save(u);
    }

    // no se elimina la misma cuenta del admin
    public boolean eliminar(Long id, String emailActual) {
        Usuario u = repo.findById(id).orElseThrow();
        if (u.getEmail().equals(emailActual)) {
            return false;
        }
        repo.delete(u);
        return true;
    }
}