package com.flores.tutorias.repository;

import com.flores.tutorias.model.Tutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TutoriaRepository extends JpaRepository<Tutoria, Long> {

    boolean existsByMateriaId(Long materiaId);

    @Query("select t from Tutoria t join fetch t.materia join fetch t.tutor order by t.fecha, t.horaInicio")
    List<Tutoria> listar();

    // recibe un tutor id, verifica si tiene una tutoria en la misma hora, si hay choque tira true
    // la consulta busca todas las tutorias en la misma fecha y hora del tutor y tira true si hay
    // si es tutoria nueva, idexcluir pasa -1, para que no se compare consigo misma,

    @Query("""
            select count(t) > 0 from Tutoria t
            where t.tutor.id = :tutorId and t.fecha = :fecha
              and t.estado <> com.flores.tutorias.model.EstadoTutoria.CANCELADA
              and t.horaInicio < :fin and t.horaFin > :inicio
              and t.id <> :idExcluir""")
    boolean haySolape(@Param("tutorId") Long tutorId, @Param("fecha") LocalDate fecha,
                      @Param("inicio") LocalTime inicio, @Param("fin") LocalTime fin,
                      @Param("idExcluir") Long idExcluir);
}
