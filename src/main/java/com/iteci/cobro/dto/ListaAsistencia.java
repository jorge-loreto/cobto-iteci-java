package com.iteci.cobro.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Data
@Entity
@Table(name = "listaasistencia")
public class ListaAsistencia {

    @EmbeddedId
    private ListaAsistenciaId id;

    @Column(name = "fechaClase")
    private LocalDate fechaClase;

    @Column(name = "fechaPago")
    private LocalDate fechaPago;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "status")
    private String status;

    @Column(name = "folio")
    private String folio;
}
