package com.iteci.cobro.services;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.entities.Student;
import com.iteci.cobro.repository.ListaAsistenciaRepository;
import com.iteci.cobro.repository.StudentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ListaAsistenciaService {

    @Autowired
    private ListaAsistenciaRepository listaAsistenciaRepository;

    @Autowired
    private EntityManager entityManager;


    public ListaAsistencia getLastAsistencia(Long groupId, Integer numSemana) {

       Optional<ListaAsistencia> asistencia = Optional.of(listaAsistenciaRepository.findByIdGrupoAlumnoSemana(groupId, numSemana));
       if (asistencia.isPresent()) {
           return asistencia.get();
       }
       return null;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public ListaAsistencia getAsistenciaBySem(int numSemana, Long idGrupoAlumno) {
        if (numSemana>1) {
            numSemana--;    
        }
        
        log.info("Lookign for semana {} and gourpId {}", numSemana, idGrupoAlumno);
        ListaAsistencia listaAsistencia = listaAsistenciaRepository.existingByNumSem(
                numSemana,
                idGrupoAlumno
        );
        return listaAsistencia;
    }

    public List<Object[]>  getLatestAsistencia3(Long idAlumno, LocalDate fechaParametro) {
        List<Object[]> results = listaAsistenciaRepository.findLatestAsistenciaRaw(idAlumno, fechaParametro);
       
        return results;
    }
}