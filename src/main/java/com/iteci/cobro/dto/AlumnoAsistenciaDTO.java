package com.iteci.cobro.dto;

import java.math.BigDecimal;
import java.sql.Time;

public record AlumnoAsistenciaDTO(
        Long idAlumno,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        Integer idGrupo,
        String diaSemana,
        String horaInicio,
        String modalidad,
        BigDecimal monto,
        Integer numeroSemana,
        Integer folio,
        Integer idGrupoAlumno,
        String fechaPago,
        String horaFinal
) {

    // Factory method from raw Object[]
    public static AlumnoAsistenciaDTO from(Object[] row) {
        if (row == null || row.length < 13) {
            throw new IllegalArgumentException("Row is null or does not have enough columns");
        }

        return new AlumnoAsistenciaDTO(
                toLong(row[0]),           // idAlumno
                toString(row[1]),         // nombre
                toString(row[2]),         // apellidoPaterno
                toString(row[3]),         // apellidoMaterno
                toString(row[4]),         // telefono
                toInt(row[5]),            // idGrupo
                toString(row[6]),         // diaSemana
                convertHora(row[7]),      // horaInicio
                toString(row[8]),         // modalidad
                toBigDecimal(row[9]),     // monto
                toInt(row[10]),           // numeroSemana
                toInt(row[11]),           // folio
                toInt(row[12]),            // idGrupoAlumno
                toString(row[13]),           // fechaPago
                convertHora(row[14])           // horaFinal
        );
    }

    private static String convertHora(Object value) {
        if (value == null) return null;

        if (value instanceof Time t) {
            return t.toLocalTime().toString(); // HH:mm:ss
        }

        return value.toString();
    }

    private static String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof BigDecimal bd) return bd.longValue();
        if (value instanceof Double d) return d.longValue();
        return Long.valueOf(value.toString());
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Long l) return l.intValue();
        if (o instanceof BigDecimal bd) return bd.intValue();
        if (o instanceof Double d) return d.intValue();
        if (o instanceof Float f) return f.intValue();
        // fallback: parse as double first
        try {
            return (int) Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert to int: " + o, e);
        }
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Double d) return BigDecimal.valueOf(d);
        if (o instanceof Float f) return BigDecimal.valueOf(f);
        if (o instanceof Integer i) return BigDecimal.valueOf(i);
        if (o instanceof Long l) return BigDecimal.valueOf(l);
        try {
            return new BigDecimal(o.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert to BigDecimal: " + o, e);
        }
    }
}
