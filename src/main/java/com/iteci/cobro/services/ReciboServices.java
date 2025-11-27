package com.iteci.cobro.services;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.repository.ListaAsistenciaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReciboServices {

    @Autowired
    private ListaAsistenciaRepository listaAsistenciaRepository;

    public AlumnoAsistenciaDTO getLatestAsistencia(Long idAlumno) {
    //Object[] row = listaAsistenciaRepository.findLatestAsistenciaRaw(idAlumno);

        List<Object[]> raw = listaAsistenciaRepository.findLatestAsistenciaRaw(idAlumno);
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

}
