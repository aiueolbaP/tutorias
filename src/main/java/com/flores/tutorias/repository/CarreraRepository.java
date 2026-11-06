package com.flores.tutorias.repository;

import com.flores.tutorias.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {
    Optional<Carrera> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

    @Query("select distinct c from Carrera c left join fetch c.tags order by c.nombre")
    List<Carrera> listarConTags();
}
