package com.iteci.cobro.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="perfil")
public class Perfil {
    @Id
    @Column(name="idPerfil")
    private Long idPerfil;
    @Column(name="nombre")
    private String nombrePerfil;
    @Column(name="clave")
    private String clave;
    @Column(name="entidad")
    private String entidad;
    @Column(name="municipio")
    private String municipio;
    @Column(name="localidad")
    private String localidad;
    @Column(name="domicilio")
    private String direccion;
    @Column(name="telefono")
    private String telefono;
    @Column(name="colonia")
    private String colonia;

}
