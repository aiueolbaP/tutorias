package com.flores.tutorias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carreras")
@Getter @Setter @NoArgsConstructor
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    // tabla intermedia carrera_tag (base de las recomendaciones del home)
    @ManyToMany
    @JoinTable(name = "carrera_tag",
            joinColumns = @JoinColumn(name = "id_carrera"),
            inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<Tag> tags = new HashSet<>();
}
