package com.flores.tutorias.dto;

import com.flores.tutorias.model.Rol;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioForm {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String email;

    //puede no tener o tener al menos 6 carac
    @Pattern(regexp = "^$|.{6,}", message = "Mínimo 6 caracteres")
    private String password;

    @NotNull(message = "Selecciona un rol")
    private Rol rol;

    private boolean activo = true;
}
