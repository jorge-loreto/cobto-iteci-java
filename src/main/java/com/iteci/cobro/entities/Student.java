package com.iteci.cobro.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="alumnos")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idAlumnos")
    private Long idAlumnos;
    @Column(name="nombre")
    private String nombre;
    @Column(name="apellidoPaterno")
    private String apellidoPaterno;
    @Column(name="apellidoMaterno")
    private String apellidoMaterno;
    @Column(name="telefono")
    private String telefono;

    // getters/setters
}
