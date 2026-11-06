package com.flores.tutorias.repository;

import com.flores.tutorias.model.Rol;
import com.flores.tutorias.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByRolAndActivoTrueOrderByNombre(Rol rol);
    boolean existsByCarreraId(Long carreraId);
}
