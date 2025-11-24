package com.iteci.cobro.dto;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Embeddable
public class ListaAsistenciaId implements Serializable {

    @Column(name = "idGrupoAlumno")
    private Long idGrupoAlumno;

    @Column(name = "numeroSemana")
    private Integer numeroSemana;

    public ListaAsistenciaId() {}

    public ListaAsistenciaId(Long idGrupoAlumno, Integer numeroSemana) {
        this.idGrupoAlumno = idGrupoAlumno;
        this.numeroSemana = numeroSemana;
    }

    // getters, setters, equals(), hashCode()
}
