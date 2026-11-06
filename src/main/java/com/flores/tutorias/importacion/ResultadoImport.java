package com.flores.tutorias.importacion;

import java.util.List;

public record ResultadoImport(int creados, int actualizados, List<String> errores) {

    public boolean ok() {
        return errores.isEmpty();
    }

    public static ResultadoImport error(String mensaje) {
        return new ResultadoImport(0, 0, List.of(mensaje));
    }
}
