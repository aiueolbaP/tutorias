package com.flores.tutorias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tutorias")
@Getter @Setter @NoArgsConstructor
public class Tutoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_materia")
    private Materia materia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tutor")
    private Usuario tutor;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private int cupo;

    @Column(name = "enlace_reunion", nullable = false, length = 300)
    private String enlaceReunion;

    @Column(name = "direccion_fisica", length = 200)
    private String direccionFisica; // "Aula 315"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTutoria estado = EstadoTutoria.PROGRAMADA;
}
