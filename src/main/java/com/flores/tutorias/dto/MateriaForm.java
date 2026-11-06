package com.flores.tutorias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class MateriaForm {

    private Long id;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    // FK a carrera: seleccionada estrictamente vía dropdown, nunca input simple de id
    @NotNull(message = "Selecciona una carrera")
    private Long carreraId;

    // IDs de tags seleccionados desde la lista de tags
    private Set<Long> tagIds = new HashSet<>();

    // Opcional: tags adicionales separados por coma o punto y coma
    private String nuevosTags;
}
