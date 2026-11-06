package com.flores.tutorias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CarreraForm {

    private Long id;

    @NotBlank(message = "El nombre de la carrera es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    // IDs de tags seleccionados desde la lista de tags
    private Set<Long> tagIds = new HashSet<>();

    // nuevos tags opcionales
    private String nuevosTags;
}
