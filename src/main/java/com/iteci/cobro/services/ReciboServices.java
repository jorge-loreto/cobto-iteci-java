package com.iteci.cobro.services;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.dto.ListaAsistenciaId;
import com.iteci.cobro.entities.Student;
import com.iteci.cobro.repository.ListaAsistenciaRepository;
import com.iteci.cobro.repository.StudentRepository;
import com.iteci.cobro.utils.NumeroALetrasUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ReciboServices {

    
    @Autowired
    private ListaAsistenciaRepository listaAsistenciaRepository;


    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private ListaAsistenciaService listaService;
    @Autowired
    private EntityManager entityManager;


    
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public AlumnoAsistenciaDTO getLatestAsistencia(Long idAlumno) {
// clear Hibernate first-level cache
        entityManager.clear();
      
        LocalDate today = LocalDate.now();
        
        //List<Object[]> raw = listaService.getLatestAsistencia(idAlumno, today);
        List<Object[]> raw = listaService.getLatestAsistencia3(idAlumno, today);

        if (raw.isEmpty()) return null;

        Object[] row = raw.get(0);

        System.out.println("\n=== RAW QUERY RESULT ===");
        System.out.println("ROW = " + java.util.Arrays.toString(row));

        System.out.println("========================\n");
        Long numeroSemana = null;

        if (row[10] != null) {
            try {
                numeroSemana = Long.parseLong(row[10].toString());
            } catch (Exception e) {
                numeroSemana = null;
            }
        }

        if (numeroSemana == null || numeroSemana <= 0) {
            row[10] = listaAsistenciaRepository.getLessNumeroSemana(((Integer)row[12]).longValue());
        }
        if(row[14]==null){
            row[14]=-1;
        }

        //Checking if a remain amount is in previous payments
        ListaAsistencia listaAsistencia = listaService.getAsistenciaBySem(Integer.valueOf(row[10].toString()), Long.valueOf(row[12].toString()));
        
        log.info("previous record info looking for money previuosly {} "+listaAsistencia);
        if(listaAsistencia != null && listaAsistencia.getMonto() != null){
            if(listaAsistencia.getMonto() < (Double)row[9]){
                row[9] = (Double)row[9]+((Double)row[9]-listaAsistencia.getMonto());
            }
        }
        List<ListaAsistencia> listaAsistenciaList = listaAsistenciaRepository.findByIdGrupoAlumnoOrderByNumeroSemanaDesc(Long.valueOf(row[12].toString()));
        log.info("All records for groupAlumno {} "+listaAsistenciaList);
    
        AlumnoAsistenciaDTO dto = AlumnoAsistenciaDTO.from(row);
        
        return dto;
    }


    @Transactional
    public int markAsPaid(AlumnoAsistenciaDTO dto) {
        log.info("Marking as paid to be UPDATED: {}", dto.toStringFull());
        return listaAsistenciaRepository.updatePayment(
                dto.idGrupoAlumno().longValue(),
                dto.numeroSemana() != null ? Integer.valueOf(dto.numeroSemana().intValue()) : Integer.valueOf(0),
                LocalDate.now(),
                dto.monto() != null ? Double.valueOf(dto.monto().doubleValue()) : Double.valueOf(0.0),
                "P",
                dto.folio() != null ? dto.folio().toString() : "0"
        );
    }


    public boolean dateValidator() {
        int limitYear = 2028;

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        if (currentYear < limitYear) {
            System.out.println("✅ Continue: Year is before 2027");
        }else {
            System.out.println("❌ Deny: Year is 2027 or later");
        }
        return currentYear < limitYear;
    }

    private boolean isFechaPagoExisting(LocalDate fechaPago, int idGroup) {
        List<com.iteci.cobro.dto.ListaAsistencia> existingRecords = listaAsistenciaRepository.existingByFechaPago(fechaPago);
        for (com.iteci.cobro.dto.ListaAsistencia record : existingRecords) {
            //if (record.getId() != null && record.getId().equals(String.valueOf(folio))) {
            if (record.getIdGrupoAlumno() != null && record.getIdGrupoAlumno().intValue() == idGroup) {
                return true; // Found a matching record
            }
        }
        return false; // No matching record found
    }

    public boolean folioValidator(int idGroup) {
        LocalDate today = LocalDate.now();
        return isFechaPagoExisting(today, idGroup);
    }

    public List<Student> getEverybody(){

        List<Student> students = studentRepo.findAll();

        List<Student> filteredStudents = students.stream()
                .map(stu ->{
                    stu.setNombre(NumeroALetrasUtil.changeWordString(stu.getNombre()));
                    stu.setApellidoPaterno(NumeroALetrasUtil.changeWordString(stu.getApellidoPaterno()));
                    stu.setApellidoMaterno(NumeroALetrasUtil.changeWordString(stu.getApellidoMaterno()));
                    return stu;
                })
                .toList();
        return filteredStudents;
    }


    public int countStudents(){
        return (int) studentRepo.count();
    }
    @Transactional
    public int markAsPaidModified(long idGrupoAlumno, int numeroSemana, 
        double monto, String folio, String status) {
        return listaAsistenciaRepository.updatePayment(
                idGrupoAlumno,
                numeroSemana,
                LocalDate.now(),
                monto,
                status,
                folio
        );
    }

}
