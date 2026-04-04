package com.iteci.cobro.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;
@Data
@Entity
@Cacheable(false)
@Table(name = "listaasistencia")
public class ListaAsistencia {

    @Column(name = "fechaPago")
    private LocalDate fechaPago;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "status")
    private String status;

    @EmbeddedId
    private ListaAsistenciaId id;

    @Column(name = "folio")
    private String folio;

    @Column(name = "numeroSemana")
    private Integer numeroSemana;

    public Long getIdGrupoAlumno() {
        return id != null ? id.getIdGrupoAlumno() : null;
    }
    public LocalDate getFechaClase() {
        return id != null ? id.getFechaClase() : null;
    }

    @Override
    public String toString() {
        return "ListaAsistencia{" +
                "idGrupoAlumno=" + getIdGrupoAlumno() +
                ", fechaClase=" + getFechaClase() +
                ", fechaPago=" + fechaPago +
                ", monto=" + monto +
                ", status='" + status + '\'' +
                ", folio='" + folio + '\'' +
                ", numeroSemana=" + numeroSemana +
                '}';
    }
}
