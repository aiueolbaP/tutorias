package com.flores.tutorias.repository;

import com.flores.tutorias.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    Optional<Materia> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCarreraId(Long carreraId);

    @Query("select distinct m from Materia m left join fetch m.carrera left join fetch m.tags order by m.nombre")
    List<Materia> listarConCarreraYTags();

    // materias que pertenecen a la carrera o comparten al menos un tag con la carrera
    @Query("""
            select distinct m from Materia m left join m.tags t
            where m.carrera.id = :carreraId
               or t.id in (select ct.id from Carrera c join c.tags ct where c.id = :carreraId)
            order by m.nombre""")
    List<Materia> porCarrera(@Param("carreraId") Long carreraId);
}
