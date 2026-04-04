package com.iteci.cobro.repository;

import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.dto.ListaAsistenciaId;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
@Repository
public interface ListaAsistenciaRepository extends JpaRepository<ListaAsistencia, ListaAsistenciaId> {

    @Transactional
    @CacheEvict(value = {"entityCache", "queryCache"}, allEntries = true)
    @Query(value = """
    SELECT
        a.idAlumnos,
        a.nombre,
        a.apellidoPaterno,
        a.apellidoMaterno,
        a.telefono,
        ga.idGrupo,
        gru.diaSemana,
        gru.horaInicio,
        m.nombre AS modalidad,
        alm.colegiatura AS monto,

        COALESCE(
            (SELECT MIN(la.numeroSemana + 1)
             FROM iteci.listaasistencia la
             WHERE la.idGrupoAlumno = ga.idGrupoAlumnos
             AND la.status='P'), 1
        ) AS numeroSemanaSiguiente,

        COALESCE(
            (SELECT MAX(CAST(folio AS UNSIGNED)) + 1
             FROM iteci.listaasistencia),
            1
        ) AS folio,

        ga.idGrupoAlumnos,
        gru.horaFin,

        COALESCE(
            (SELECT COUNT(*)
             FROM iteci.listaasistencia la
             WHERE la.idGrupoAlumno = ga.idGrupoAlumnos
             AND la.fechaClase <= :fechaParametro
             AND la.folio IS NULL), 0
        ) AS totalObservaciones,

        COALESCE(
            (SELECT COUNT(*)
             FROM iteci.listaasistencia la
             WHERE la.idGrupoAlumno = ga.idGrupoAlumnos
             AND la.fechaClase > :fechaParametro
             AND la.folio IS NOT NULL), 0
        ) AS adelantadas,

        COALESCE(
            (SELECT MAX(la.numeroSemana)
             FROM iteci.listaasistencia la
             WHERE la.idGrupoAlumno = ga.idGrupoAlumnos
             AND la.fechaClase <= :fechaParametro), 0
        ) AS semanaActualPago

    FROM iteci.alumnos a
    JOIN iteci.`grupo-alumnos` ga ON a.idAlumnos = ga.idAlumno
    JOIN iteci.grupos gru ON gru.idGrupo = ga.idGrupo
    JOIN iteci.modalidades m ON gru.idModalidad = m.idModalidad
    JOIN iteci.`alumnos-modalidades` alm ON alm.idAlumno = a.idAlumnos

    WHERE a.idAlumnos = :idAlumno
    LIMIT 1
    """, nativeQuery = true)

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

