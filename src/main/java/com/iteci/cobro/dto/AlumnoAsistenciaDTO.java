package com.iteci.cobro.dto;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;


@Slf4j
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
        String horaFinal,
        Integer totalObservaciones,
        Integer semanasAdelantadas,
        String observaciones,
        Integer semanaActual,
        BigDecimal montoModificado,
        Boolean montoEditado
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
                toFecha(),           // fechaPago
                convertHora(row[13]),           // horaFinal
                toInt(row[14]),            // totalObservaciones
                toInt(row[15]),  //semanas adelantadas
                toObserv(toInt(row[14]), toInt(row[15])),         // observaciones
                toInt(row[16]),  // semanaActual
                toBigDecimal(row[9]),  // montoModificado
                toBoolean(false)  // montoEditado

        );
    }

    private static Boolean toBoolean(boolean b) {
        return Boolean.valueOf(b);
    }

    private static String toObserv(int totalSemanasPendientes, int numeroSemanaAdelantadas) {
        log.info("Calculating observaciones with totalSemanasPendientes {} and numeroSemanaAdelantadas {}", totalSemanasPendientes, numeroSemanaAdelantadas);
        if (totalSemanasPendientes < 0 && numeroSemanaAdelantadas==0) {
            log.info("1 semana adelantada de pago");
            return "Muy bien vas al corriente con tus pagos :)";
            
        } else if (totalSemanasPendientes < 0 && numeroSemanaAdelantadas>0) {
            log.info("{} semanas adelantadas de pago", numeroSemanaAdelantadas+1);
            return numeroSemanaAdelantadas+1+" semanas adelantadas de pago";
            
        } else {
                log.info("{} semanas pendientes de pago", totalSemanasPendientes);
                return totalSemanasPendientes + " semanas pendientes de pago.";
        }
    }

    private static String toFecha() {
        LocalDate today = LocalDate.now();
        return today.toString();
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
    public String toStringFull() {
        return "AlumnoAsistenciaDTO{" +
                "idAlumno=" + idAlumno +
                ", nombre='" + nombre + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", telefono='" + telefono + '\'' +
                ", idGrupo=" + idGrupo +
                ", diaSemana='" + diaSemana + '\'' +
                ", horaInicio='" + horaInicio + '\'' +
                ", modalidad='" + modalidad + '\'' +
                ", monto=" + monto +
                ", numeroSemana=" + numeroSemana +
                ", folio=" + folio +
                ", idGrupoAlumno=" + idGrupoAlumno +
                ", fechaPago='" + fechaPago + '\'' +
                ", horaFinal='" + horaFinal + '\'' +
                ", totalObservaciones=" + totalObservaciones +
                ", semanasAdelantadas=" + semanasAdelantadas +
                ", observaciones='" + observaciones + '\'' +
                ", semanaActual=" + semanaActual +
                ", montoModificado=" + montoModificado +
                ", montoEditado=" + montoEditado +
                '}';
    }
}
