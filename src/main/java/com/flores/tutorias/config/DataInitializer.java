package com.flores.tutorias.config;

import com.flores.tutorias.model.Rol;
import com.flores.tutorias.model.Usuario;
import com.flores.tutorias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    //si no hay datos al correr, esto carga para pruebitas
    @Override
    public void run(String... args) {
        crearSiNoExiste("Admin", "admin@tutorias.com", "admin123", Rol.ADMIN);
        crearSiNoExiste("Tutor Demo", "tutor@tutorias.com", "tutor123", Rol.TUTOR);
        crearSiNoExiste("Estudiante Demo", "estudiante@tutorias.com", "estudiante123", Rol.ESTUDIANTE);
    }

    private void crearSiNoExiste(String nombre, String email, String clave, Rol rol) {
        if (!repo.existsByEmail(email)) {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setEmail(email);
            u.setPassword(encoder.encode(clave));
            u.setRol(rol);
            repo.save(u);
        }
    }
}