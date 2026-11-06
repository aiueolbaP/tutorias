package com.flores.tutorias.service;

import com.flores.tutorias.dto.TutoriaForm;
import com.flores.tutorias.model.*;
import com.flores.tutorias.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutoriaService {

    //horarios limite 7am - 9pm
    private static final LocalTime APERTURA = LocalTime.of(7, 0);
    private static final LocalTime CIERRE = LocalTime.of(21, 0);

    private final TutoriaRepository tutoriaRepo;
    private final MateriaRepository materiaRepo;
    private final UsuarioRepository usuarioRepo;
    private final CarreraRepository carreraRepo;

    public List<Tutoria> listar() {
        return tutoriaRepo.listar();
    }


    public List<Usuario> tutores() {
        return usuarioRepo.findByRolAndActivoTrueOrderByNombre(Rol.TUTOR);
    }

    public List<Carrera> carreras() {
        return carreraRepo.findAll(Sort.by("nombre"));
    }


    public List<Materia> materiasPorCarrera(Long carreraId) {
        return carreraId == null ? materiaRepo.findAll(Sort.by("nombre")) : materiaRepo.porCarrera(carreraId);
    }

    public TutoriaForm obtenerForm(Long id) {
        Tutoria t = tutoriaRepo.findById(id).orElseThrow();
        TutoriaForm f = new TutoriaForm();
        f.setId(t.getId());
        f.setTitulo(t.getTitulo());
        f.setDescripcion(t.getDescripcion());
        f.setMateriaId(t.getMateria().getId());
        f.setTutorId(t.getTutor().getId());
        f.setFecha(t.getFecha());
        f.setHoraInicio(t.getHoraInicio());
        f.setHoraFin(t.getHoraFin());
        f.setCupo(t.getCupo());
        f.setEnlaceReunion(t.getEnlaceReunion());
        f.setDireccionFisica(t.getDireccionFisica());
        return f;
    }

    //practicamente la mayoria, si no todos los errores q pueden haber
    public void validarNegocio(TutoriaForm f, BindingResult r) {
        //si se envia un id de materia pero no existe (alguien borro justo antes o algo), error
        if (f.getMateriaId() != null && !materiaRepo.existsById(f.getMateriaId())) {
            r.rejectValue("materiaId", "invalida", "La materia seleccionada no existe");
        }

        // el tutor debe existir, estar activo y tener su rol de tutor
        if (f.getTutorId() != null) {
            Usuario tutor = usuarioRepo.findById(f.getTutorId()).orElse(null);
            if (tutor == null || tutor.getRol() != Rol.TUTOR || !tutor.isActivo()) {
                r.rejectValue("tutorId", "invalido", "El tutor no existe o no está activo");
            }
        }

        //Si hay fecha valida pero es anterior a la actual, error
        if (!r.hasFieldErrors("fecha") && f.getFecha().isBefore(LocalDate.now())) {
            r.rejectValue("fecha", "pasada", "La fecha no puede estar en el pasado");
        }


        if (!r.hasFieldErrors("horaInicio") && !r.hasFieldErrors("horaFin")) {
            LocalTime ini = f.getHoraInicio(), fin = f.getHoraFin();
            //hora fin debe ser despues de inicio
            if (!fin.isAfter(ini)) {
                r.rejectValue("horaFin", "rango", "La hora de fin debe ser posterior a la de inicio");
                //debe estar dentro del horario disponible
            } else if (ini.isBefore(APERTURA) || fin.isAfter(CIERRE)) {
                r.rejectValue("horaInicio", "jornada", "Debe estar dentro de la jornada 07:00 - 21:00");

                //mira que no choque con otras tutorias, si es nueva tira -1 para q no se excluya a si misma
            } else if (!r.hasFieldErrors("fecha") && !r.hasFieldErrors("tutorId")
                    && f.getTutorId() != null
                    && tutoriaRepo.haySolape(f.getTutorId(), f.getFecha(), ini, fin,
                    f.getId() == null ? -1L : f.getId())) {
                r.rejectValue("horaInicio", "solape", "El tutor ya tiene una tutoría en ese horario");
            }
        }

        //si el enlace no es enlace, error
        if (!r.hasFieldErrors("enlaceReunion") && !esHttps(f.getEnlaceReunion())) {
            r.rejectValue("enlaceReunion", "enlace", "Debe ser un enlace https:// válido");
        }
    }

    //estructura del enlace, el getscheme ve que sea https y no ftp:// o file://, javascript:, etc
    //host ve que exista el dominio como zoom.us o meet.google.com por ejm
    private boolean esHttps(String enlace) {
        try {
            URI u = URI.create(enlace.trim());
            return "https".equalsIgnoreCase(u.getScheme()) && u.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public void guardar(TutoriaForm f) {
        Tutoria t = (f.getId() == null) ? new Tutoria() : tutoriaRepo.findById(f.getId()).orElseThrow();
        t.setTitulo(f.getTitulo().trim());
        t.setDescripcion(f.getDescripcion());
        t.setMateria(materiaRepo.getReferenceById(f.getMateriaId()));
        t.setTutor(usuarioRepo.getReferenceById(f.getTutorId()));
        t.setFecha(f.getFecha());
        t.setHoraInicio(f.getHoraInicio());
        t.setHoraFin(f.getHoraFin());
        t.setCupo(f.getCupo());
        t.setEnlaceReunion(f.getEnlaceReunion().trim());
        t.setDireccionFisica(f.getDireccionFisica() == null || f.getDireccionFisica().isBlank()
                ? null : f.getDireccionFisica().trim());
        tutoriaRepo.save(t);
    }

    public void eliminar(Long id) {
        tutoriaRepo.deleteById(id);
    }
}
