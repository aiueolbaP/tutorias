package com.flores.tutorias.importacion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


 // Convierte CSV, XLSX o JSON a una lista de filas uniforme, cada fila es un Map
// columna -> texto, con los nombres de columna minuscula.
@Component
public class ArchivoParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, String>> parse(MultipartFile file) throws IOException {
        String nombre = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        if (nombre.endsWith(".csv")) return csv(file);
        if (nombre.endsWith(".xlsx")) return xlsx(file);
        if (nombre.endsWith(".json")) return json(file);
        throw new IllegalArgumentException("Formato no soportado: usa .csv, .xlsx o .json");
    }

    // ---------- CSV (detecta ; o , como separador, típico de Excel en español) ----------
    private List<Map<String, String>> csv(MultipartFile file) throws IOException {
        String texto = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (texto.startsWith("\uFEFF")) texto = texto.substring(1);

        String primera = texto.lines().findFirst().orElse("");
        long puntoComa = primera.chars().filter(c -> c == ';').count();
        long coma = primera.chars().filter(c -> c == ',').count();

        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setDelimiter(puntoComa > coma ? ';' : ',')
                .setHeader().setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true).setTrim(true)
                .build();

        List<Map<String, String>> filas = new ArrayList<>();
        try (CSVParser parser = formato.parse(new StringReader(texto))) {
            for (CSVRecord r : parser) {
                Map<String, String> fila = new LinkedHashMap<>();
                r.toMap().forEach((k, v) -> fila.put(norm(k), v == null ? "" : v.trim()));
                filas.add(fila);
            }
        }
        return filas;
    }

    // ---------- XLSX (primera hoja, primera fila = encabezados) ----------
    private List<Map<String, String>> xlsx(MultipartFile file) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet hoja = wb.getSheetAt(0);
            Row cab = hoja.getRow(hoja.getFirstRowNum());
            if (cab == null) return filas;

            List<String> columnas = new ArrayList<>();
            for (int j = 0; j < cab.getLastCellNum(); j++) columnas.add(norm(texto(cab.getCell(j), fmt)));

            for (int i = hoja.getFirstRowNum() + 1; i <= hoja.getLastRowNum(); i++) {
                Row row = hoja.getRow(i);
                if (row == null) continue;
                Map<String, String> fila = new LinkedHashMap<>();
                boolean vacia = true;
                for (int j = 0; j < columnas.size(); j++) {
                    String v = texto(row.getCell(j), fmt);
                    if (!v.isBlank()) vacia = false;
                    if (!columnas.get(j).isBlank()) fila.put(columnas.get(j), v);
                }
                if (!vacia) filas.add(fila);
            }
        }
        return filas;
    }

    private String texto(Cell c, DataFormatter fmt) {
        if (c == null) return "";
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            LocalDateTime dt = c.getLocalDateTimeCellValue();
            // una celda solo-hora llega con fecha 1899/1900
            return dt.getYear() < 1901
                    ? dt.toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString()
                    : dt.toLocalDate().toString();
        }
        return fmt.formatCellValue(c).trim();
    }

    // ---------- JSON: arreglo de objetos [ {...}, {...} ] ----------
    private List<Map<String, String>> json(MultipartFile file) throws IOException {
        List<Map<String, Object>> datos = mapper.readValue(file.getInputStream(), new TypeReference<>() {});
        List<Map<String, String>> filas = new ArrayList<>();
        for (Map<String, Object> obj : datos) {
            Map<String, String> fila = new LinkedHashMap<>();
            obj.forEach((k, v) -> {
                String valor;
                if (v == null) valor = "";
                else if (v instanceof Collection<?> c) valor = String.join(";", c.stream().map(String::valueOf).toList());
                else valor = String.valueOf(v).trim();
                fila.put(norm(k), valor);
            });
            filas.add(fila);
        }
        return filas;
    }

    private String norm(String s) {
        return s == null ? "" : s.replace("\uFEFF", "").trim().toLowerCase().replace(' ', '_');
    }
}
