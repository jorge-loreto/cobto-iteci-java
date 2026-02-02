package com.iteci.cobro.services;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.entities.Student;
import com.iteci.cobro.repository.ListaAsistenciaRepository;
import com.iteci.cobro.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReciboServices {

    @Autowired
    private ListaAsistenciaRepository listaAsistenciaRepository;


    @Autowired
    private StudentRepository studentRepo;

    public AlumnoAsistenciaDTO getLatestAsistencia(Long idAlumno) {
    //Object[] row = listaAsistenciaRepository.findLatestAsistenciaRaw(idAlumno);
        LocalDate today = LocalDate.now();
        List<Object[]> raw = listaAsistenciaRepository.findLatestAsistenciaRaw(idAlumno, today);
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

    
        AlumnoAsistenciaDTO dto = AlumnoAsistenciaDTO.from(row);


        return dto;
    }


    @Transactional
    public int markAsPaid(AlumnoAsistenciaDTO dto) {
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
        int limitYear = 2027;

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        if (currentYear < limitYear) {
            System.out.println("✅ Continue: Year is before 2027");
        } else {
            System.out.println("❌ Deny: Year is 2027 or later");
        }
        return currentYear < limitYear;
    }

    private boolean isFechaPagoExisting(LocalDate fechaPago, int idGroup) {
        List<com.iteci.cobro.dto.ListaAsistencia> existingRecords = listaAsistenciaRepository.existingByFechaPago(fechaPago);
        for (com.iteci.cobro.dto.ListaAsistencia record : existingRecords) {
            //if (record.getId() != null && record.getId().equals(String.valueOf(folio))) {
            if (record.getId() != null && record.getId().getIdGrupoAlumno().intValue() == idGroup) {
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
                    stu.setNombre(changeWordString(stu.getNombre()));
                    stu.setApellidoPaterno(changeWordString(stu.getApellidoPaterno()));
                    stu.setApellidoMaterno(changeWordString(stu.getApellidoMaterno()));
                    return stu;
                })
                .toList();
        return filteredStudents;
    }

    public String changeWordString(String name){
         // 1. Remove accents first
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("\\p{M}", ""); // removes diacritical marks

        String[] parts = name.trim().split("\\s+"); // handles multiple spaces safely
        String resultaString = "";
        for(String names : parts){
            resultaString += names.toUpperCase().charAt(0) 
                + names.substring(1).toLowerCase() 
                + " ";
        }
        resultaString = resultaString.trim();
        return resultaString;
    }

     public int countStudents(){
        return (int) studentRepo.count();
    }

}
