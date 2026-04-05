package com.iteci.cobro.repository;

import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.dto.ListaAsistenciaId;

import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
@Repository
public interface ListaAsistenciaRepository extends JpaRepository<ListaAsistencia, ListaAsistenciaId> {

    @Transactional
    @CacheEvict(value = {"entityCache", "queryCache"}, allEntries = true)
    @Query(value = "SELECT\n" + //
                "    al.idAlumnos,\n" + //
                "    al.nombre,\n" + //
                "    al.apellidoPaterno,\n" + //
                "    al.apellidoMaterno,\n" + //
                "    al.telefono,\n" + //
                "    ga.idGrupo,\n" + //
                "    gru.diaSemana,\n" + //
                "    gru.horaInicio,\n" + //
                "    m.nombre AS modalidad,\n" + //
                "    alm.colegiatura AS monto,\n" + //
                "    COALESCE(MAX(CASE WHEN la.status = 'P' THEN la.numeroSemana ELSE NULL END), 0) + 1 AS numeroSemanaSiguiente,\n" + //
                "    (SELECT COALESCE(MAX(CAST(folio AS UNSIGNED)) + 1, 1) FROM iteci.listaasistencia) AS folio,\n" + //
                "    ga.idGrupoAlumnos,\n" + //
                "    gru.horaFin,\n" + //
                "    SUM(CASE WHEN la.fechaClase < :fechaParametro AND la.folio IS NULL THEN 1 ELSE 0 END) -1 AS totalObservaciones,\n" + //
                "    SUM(CASE WHEN la.fechaClase > :fechaParametro AND la.folio IS NOT NULL THEN 1 ELSE 0 END) AS adelantadas,\n" + //
                "    COALESCE(MAX(CASE WHEN la.fechaClase <= :fechaParametro THEN la.numeroSemana ELSE NULL END), 1)  AS semanaActualPago\n" + //
                "FROM iteci.alumnos AS al\n" + //
                "JOIN iteci.`grupo-alumnos` AS ga ON al.idAlumnos = ga.idAlumno\n" + //
                "JOIN iteci.grupos AS gru ON gru.idGrupo = ga.idGrupo\n" + //
                "JOIN iteci.modalidades AS m ON gru.idModalidad = m.idModalidad\n" + //
                "JOIN iteci.`alumnos-modalidades` AS alm ON alm.idAlumno = al.idAlumnos\n" + //
                "LEFT JOIN iteci.listaasistencia AS la ON la.idGrupoAlumno = ga.idGrupoAlumnos\n" + //
                "WHERE al.idAlumnos = :idAlumno\n" + //
                "GROUP BY\n" + //
                "    ga.idGrupoAlumnos,\n" + //
                "    al.idAlumnos,\n" + //
                "    al.nombre,\n" + //
                "    al.apellidoPaterno,\n" + //
                "    al.apellidoMaterno,\n" + //
                "    al.telefono,\n" + //
                "    ga.idGrupo,\n" + //
                "    gru.diaSemana,\n" + //
                "    gru.horaInicio,\n" + //
                "    gru.horaFin,\n" + //
                "    m.nombre,\n" + //
                "    alm.colegiatura;", 
    nativeQuery = true)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "false"),
    })
    List<Object[]> findLatestAsistenciaRaw(
            @Param("idAlumno") Long idAlumno,
            @Param("fechaParametro") LocalDate fechaParametro
    );


    @Modifying
    @Transactional
    @Query("""
    UPDATE ListaAsistencia l
    SET l.status = :status,
        l.fechaPago = :fechaPago,
        l.monto = :monto,
        l.folio = :folio
    WHERE l.id.idGrupoAlumno = :idGrupoAlumno
    AND l.numeroSemana = :numeroSemana
    """)
    int updatePayment(
            @Param("idGrupoAlumno") Long idGrupoAlumno,
            @Param("numeroSemana") Integer numeroSemana,
            @Param("fechaPago") LocalDate fechaPago,
            @Param("monto") Double monto,
            @Param("status") String status,
            @Param("folio") String folio
    );


    @Query("""
       SELECT COALESCE(MIN(l.numeroSemana), 1)
       FROM ListaAsistencia l
       WHERE l.id.idGrupoAlumno = :idGrupoAlumno
    """)
    Integer getLessNumeroSemana(@Param("idGrupoAlumno") Long idGrupoAlumno);


    @Query("""
        SELECT l
        FROM ListaAsistencia l
        WHERE l.fechaPago = :fechaPago
    """)
    List<ListaAsistencia> existingByFechaPago(LocalDate fechaPago);


    @Query("""
        SELECT l
        FROM ListaAsistencia l
        WHERE l.numeroSemana = :numeroSemana
        AND l.id.idGrupoAlumno = :idGrupoAlumno
    """)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false")
    })
    ListaAsistencia existingByNumSem(
            @Param("numeroSemana") int numeroSemana,
            @Param("idGrupoAlumno") Long idGrupoAlumno
    );


    @Query("""
        SELECT l
        FROM ListaAsistencia l
        WHERE l.id.idGrupoAlumno = :idGrupoAlumno
        ORDER BY l.id.fechaClase DESC
    """)
    List<ListaAsistencia> findByIdGrupoAlumnoOrderByNumeroSemanaDesc(
            @Param("idGrupoAlumno") Long idGrupoAlumno
    );


    @Query("""
        SELECT l
        FROM ListaAsistencia l
        WHERE l.id.idGrupoAlumno = :idGrupoAlumno
        AND l.numeroSemana = :numeroSemana
    """)
    ListaAsistencia findByIdGrupoAlumnoSemana(
            @Param("idGrupoAlumno") Long idGrupoAlumno,
            @Param("numeroSemana") Integer numeroSemana
    );

}

