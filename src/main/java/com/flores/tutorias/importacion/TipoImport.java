package com.flores.tutorias.importacion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// El orden importa: cada tipo depende de los anteriores (se referencian por nombre/correo)
@Getter
@RequiredArgsConstructor
public enum TipoImport {
    TAGS("1. Tags", "nombre"),
    CARRERAS("2. Carreras", "nombre, tags (separados por ;)"),
    MATERIAS("3. Materias", "nombre, descripcion, tags (separados por ;)"),
    USUARIOS("4. Usuarios", "nombre, email, password, rol, carrera, activo"),
    TUTORIAS("5. Tutorías", "titulo, descripcion, materia, tutor_email, fecha (yyyy-MM-dd), "
            + "hora_inicio (HH:mm), hora_fin (HH:mm), cupo, enlace_reunion, direccion_fisica");

    private final String etiqueta;
    private final String columnas;
}
