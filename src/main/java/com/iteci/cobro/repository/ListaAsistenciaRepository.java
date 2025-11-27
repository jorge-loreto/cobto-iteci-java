package com.iteci.cobro.repository;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.dto.ListaAsistenciaId;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface ListaAsistenciaRepository extends JpaRepository<ListaAsistencia, ListaAsistenciaId> {

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
    m.nombre,
    alm.colegiatura as monto,
    ll_last.numeroSemana + 1 AS numeroSemana,
    ll_folio.maxFolio+1 AS folio,
    ga.idGrupoAlumnos,
    gru.horaFin
    FROM iteci.alumnos a
    JOIN iteci.`grupo-alumnos` ga
        ON a.idAlumnos = ga.idAlumno
    JOIN iteci.grupos gru
        ON gru.idGrupo = ga.idGrupo
    JOIN iteci.modalidades m
        ON gru.idModalidad = m.idModalidad
    inner join iteci.`alumnos-modalidades` alm 
        on alm.idAlumno = a.idAlumnos
    /* 🔥 FIX: get last semana + monto FOR THIS idGrupoAlumno ONLY */
    LEFT JOIN (
    
            SELECT idGrupoAlumno, MAX(numeroSemana) AS numeroSemana
            FROM iteci.listaasistencia
            WHERE status LIKE 'P%' 
            GROUP BY idGrupoAlumno
    
    ) ll_last
    ON ll_last.idGrupoAlumno = ga.idGrupoAlumnos

    LEFT JOIN (
        SELECT MAX(CAST(folio AS UNSIGNED)) AS maxFolio
        FROM iteci.listaasistencia
    ) ll_folio ON 1=1

    WHERE a.idAlumnos = ?
    LIMIT 1;



    """, nativeQuery = true)
    List<Object[]> findLatestAsistenciaRaw(@Param("idAlumno") Long idAlumno);








   
    @Modifying
    @Transactional
    @Query("UPDATE ListaAsistencia l SET l.status = :status, l.fechaPago = :fechaPago, l.monto = :monto, l.folio = :folio " +
           "WHERE l.id.idGrupoAlumno = :idGrupoAlumno AND l.id.numeroSemana = :numeroSemana")
    int updatePayment(@Param("idGrupoAlumno") Long idGrupoAlumno,
                      @Param("numeroSemana") Integer numeroSemana,
                      @Param("fechaPago") java.time.LocalDate fechaPago,
                      @Param("monto") Double monto,
                      @Param("status") String status,
                      @Param("folio") String folio);


    

                      @Query("""
       SELECT COALESCE(MIN(l.id.numeroSemana), 1)
       FROM ListaAsistencia l
       WHERE l.id.idGrupoAlumno = :idGrupoAlumno
       """)
    Integer getLessNumeroSemana(@Param("idGrupoAlumno") Long idGrupoAlumno);


    
}

