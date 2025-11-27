package com.iteci.cobro.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iteci.cobro.dto.AlumnoAsistenciaDTO;
import com.iteci.cobro.dto.StudentDTO;
import com.iteci.cobro.entities.Student;
import com.iteci.cobro.repository.StudentRepository;
import com.iteci.cobro.services.PdfService;
import com.iteci.cobro.services.ReciboServices;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/students")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000") // allow React dev server
public class StudentController {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private ReciboServices reciboServices;

    @Autowired
    private PdfService pdfService;


    @GetMapping("/test")
    public String testQuery() {
        long count = studentRepo.count();
        return "Students in database: " + count;
    }

    @GetMapping("/telefono")
    public Student getByEmail(@RequestParam String email) {
        return studentRepo.findByEmail(email);
    }

    @GetMapping("/nombre")
    public ResponseEntity<List<StudentDTO>> getStudents(@RequestParam String nombre) {
        log.info("nombre {} ",nombre);

        try{
            List<Student> students = studentRepo.findAll();
            log.info("--------------------- ALL STUDENTS ---------------------");
            students.forEach(s -> log.info("student {} ",s));

            List<StudentDTO> dtos = students.stream()
                                    .map(StudentDTO::from)
                                    .filter(dto -> dto.fullName().toLowerCase().contains(nombre.toLowerCase()))
                                    .toList();
            // 3️⃣ Return ResponseEntity with status 200 OK
            log.info("---------------------FILTERED STUDENTS {}---------------------", nombre);
            dtos.forEach(s -> log.info("student {} ",s));
            if (dtos.isEmpty()) {
                //return ResponseEntity.noContent().build(); // 204 No Content
                return ResponseEntity.ok(dtos);
            }
            if(reciboServices.dateValidator()==false){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
            }
            return ResponseEntity.ok(dtos);
        }catch(Exception e){
            e.printStackTrace(); // optional: replace with logger
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Collections.emptyList()); // return empty list on error
        }
    }

    @GetMapping("/recibo")
    public ResponseEntity<List<AlumnoAsistenciaDTO>> getRecibo(@RequestParam Long id) {
        log.info("ID ALUMNO {} ", id);

        try {
            AlumnoAsistenciaDTO asistenciaOpt = reciboServices.getLatestAsistencia(id);

                log.info("--------------------- RECIBO ASISTENCIA ---------------------");
                log.info("Asistencia: {}", asistenciaOpt);
               
                return ResponseEntity.ok(List.of(asistenciaOpt));

        } catch (Exception e) {
            log.error("Error fetching asistencia for alumno {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(List.of()); // return empty list on error
        }
    }

    @PostMapping("/pagar")
    public ResponseEntity<String> pagar(@RequestBody AlumnoAsistenciaDTO dto) {
        if(reciboServices.dateValidator()==false){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Pago no permitido: Año límite alcanzado.");
            
        }else{
            log.info("Processing payment for DTO: {}", dto);
            try {
                // 1. Generate PDF
                log.info("Generating PDF for pago: {}", dto);
                File pdf = pdfService.generateReciboPDFxyz99(dto);
                // 2. Print PDF
                log.info("Imprimiendo:", dto.folio());
                pdfService.printPDF(pdf);

                // 3. Update payment in database
                int updatedRows = reciboServices.markAsPaid(dto);

                // ------------------------------
                // SUCCESS CASE
                // ------------------------------
                if (updatedRows == 1) {
                    return ResponseEntity.ok("Pago procesado y recibo enviado a impresión.");
                }

                // ------------------------------
                // PROBLEM: No row updated
                // ------------------------------
                if (updatedRows == 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró el registro de asistencia para actualizar (idGrupoAlumno="
                                + dto.idGrupoAlumno() + ", numeroSemana=" + dto.numeroSemana() + ").");
                }

                // ------------------------------
                // PROBLEM: More than 1 row updated (should NEVER happen)
                // ------------------------------
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Error: Se actualizaron múltiples registros. Verificar clave compuesta.");

            }catch (Exception e) {
                log.error("Error al procesar el pago o imprimir el recibo para alumno {}: {}", dto.idAlumno(), e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el pago o imprimir el recibo.");
            }
        }
        
    }
    @GetMapping("/recibo/pdf/{folio}")
    public ResponseEntity<byte[]> getReciboPDF(@PathVariable String folio) throws IOException {
        log.info("pdf to be dowloaded: {} .pdf",folio);
        String baseDir = System.getProperty("user.home") + "/recibos";
        
        File file = new File(baseDir + "/" + folio+ ".pdf");

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=recibo.pdf")
                .body(bytes);
    }



}
