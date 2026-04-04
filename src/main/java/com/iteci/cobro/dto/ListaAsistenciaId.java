package com.iteci.cobro.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;



@Embeddable
public class ListaAsistenciaId implements Serializable {

    @Column(name = "idGrupoAlumno")
    private Long idGrupoAlumno;

    @Column(name = "fechaClase")
    private LocalDate fechaClase;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListaAsistenciaId)) return false;
        ListaAsistenciaId that = (ListaAsistenciaId) o;
        return Objects.equals(idGrupoAlumno, that.idGrupoAlumno) &&
               Objects.equals(fechaClase, that.fechaClase);
        
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGrupoAlumno, fechaClase);
    }

    public Long getIdGrupoAlumno() {
        return idGrupoAlumno;
    }

    public LocalDate getFechaClase() {
        return fechaClase;
    }

    public void setIdGrupoAlumno(long l) {
        this.idGrupoAlumno = l;
    }

    public void setFechaClase(LocalDate of) {
        this.fechaClase = of;
    }
}