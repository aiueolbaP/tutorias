package com.flores.tutorias.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class TutoriaForm {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String titulo;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    // Solo filtro del dropdown en cascada (carrera -> materias). No se guarda.
    private Long carreraId;

    @NotNull(message = "Selecciona una materia")
    private Long materiaId;

    @NotNull(message = "Selecciona un tutor")
    private Long tutorId;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    @NotNull(message = "El cupo es obligatorio")
    @Min(value = 1, message = "Mínimo 1")
    @Max(value = 200, message = "Máximo 200")
    private Integer cupo;

    @NotBlank(message = "El enlace de reunión es obligatorio")
    @Size(max = 300, message = "Máximo 300 caracteres")
    private String enlaceReunion;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String direccionFisica; // ej: "Aula 315" (opcional: puede ser solo virtual)
}
