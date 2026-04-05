package com.iteci.cobro.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.iteci.cobro.dto.ListaAsistencia;
import com.iteci.cobro.dto.ListaAsistenciaId;
import com.iteci.cobro.dto.StudentDTO;
import com.iteci.cobro.entities.Student;
import com.iteci.cobro.exceptions.IteciPrinterException;
import com.iteci.cobro.repository.StudentRepository;
import com.iteci.cobro.services.GcsStorageService;
import com.iteci.cobro.services.PdfService;
import com.iteci.cobro.services.ReciboServices;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/students")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000") // allow React dev server
public class StudentController {

    @Autowired
    private ReciboServices reciboServices;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private GcsStorageService gcsStorageService;

    @GetMapping("/test")
    public String testQuery() {
        long count = reciboServices.countStudents();
        return "Students in database: " + count;
    }

    @GetMapping("/nombre")
    public ResponseEntity<List<StudentDTO>> getStudents(@RequestParam String nombre) {
        log.info("nombre {} ", nombre);

        try {
            List<Student> students = reciboServices.getEverybody();
            log.info("--------------------- ALL STUDENTS ---------------------");
            long count = reciboServices.countStudents();
            log.info("Students in database: {}", count);
            log.info("--------------------- ALL STUDENTS ---------------------");
            students.forEach(s -> log.info("student {} ", s));

            List<StudentDTO> dtos = students.stream()
                    .map(StudentDTO::from)
                    .filter(dto -> dto.fullName().toLowerCase().contains(nombre.toLowerCase()))
                    .toList();
            // 3️⃣ Return ResponseEntity with status 200 OK
            log.info("---------------------FILTERED STUDENTS {}---------------------", nombre);
            dtos.forEach(s -> log.info("student {} ", s));
            if (dtos.isEmpty()) {
                // return ResponseEntity.noContent().build(); // 204 No Content
                return ResponseEntity.ok(dtos);
            }
            if (reciboServices.dateValidator() == false) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
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

    @PostMapping("/registrar-pago")
    public ResponseEntity<String> pagar(@RequestBody AlumnoAsistenciaDTO dto) throws IOException, IteciPrinterException {
        System.out.println("update payment for: " + dto);
        if (reciboServices.dateValidator() == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Pago no permitido: Año límite alcanzado. Se requiere actualización anual.");

        }else {
            if ((dto.montoEditado() != null && dto.montoEditado().booleanValue() == true
                    && dto.montoModificado() != dto.monto())) {
                log.info("Monto modificado por el usuario: {}", dto.montoModificado());
                // Process payment with modified amount
                ResponseEntity<String> response = montoModificado(dto);

                return response;
            }else{
                log.info("Monto original sin modificaciones: {}", dto.monto());
                ResponseEntity<String> response = montoOriginalSinModificar(dto);
                return response;
            }
        }
    }

    @GetMapping("/recibo/pdf/{folio}")
    public ResponseEntity<byte[]> getReciboPDF(@PathVariable String folio) throws IOException {
        log.info("pdf to be dowloaded: {} .pdf", folio);
        String baseDir = System.getProperty("user.home") + "/recibos";

        File file = new File(baseDir + "/" + folio + ".pdf");

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=recibo.pdf")
                .body(bytes);
    }

    @PostMapping("/validar-pago-previo")
    public ResponseEntity<String> pagarPrevio(@RequestBody AlumnoAsistenciaDTO dto) {
        log.info("Validating payment for DTO: {}", dto);
        try {
            // Check for existing payment with same fechaPago and folio
            boolean exists = reciboServices.folioValidator(dto.idGrupoAlumno().intValue());

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("ALERTA: Ya existe un pago con la misma fecha de este ALUMNO, ¿Esta adelantando o pagando semana atrasada??");
            } else {
                return ResponseEntity.ok("No existe un pago previo con la misma fecha para este alumno.");
            }

        }catch (Exception e) {
            log.error("Error al procesar el pago o imprimir el recibo para alumno {}: {}", dto.idAlumno(),
                    e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el pago o imprimir el recibo.");
        }
    }

    public ResponseEntity<String> montoOriginalSinModificar(AlumnoAsistenciaDTO dto) throws IOException, IteciPrinterException {
        
            // 1. Generate PDF
            log.info("Generating PDF for pago: {}", dto);
            File pdf = pdfService.generateReciboPDFxyz99(dto);
            // 2. Print PDF
            log.info("Imprimiendo recibo para folio: {}", dto.folio());
            /** ELIMINAR AFTER TEST 
            if (true)
                return ResponseEntity.ok("Pago NORMAL SIN MODIFICAR.");
            /** ELIMINAR AFTER TEST */

            pdfService.printPDF(pdf);

            // 3. Update payment in database
            int updatedRows = reciboServices.markAsPaid(dto);

            

            // ------------------------------
            // PROBLEM: No row updated
            // ------------------------------
            if (updatedRows == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró el registro de asistencia para actualizar (idGrupoAlumno="
                                + dto.idGrupoAlumno() + ", numeroSemana=" + dto.numeroSemana() + ").");
            }
            // ------------------------------
            // SUCCESS CASE
            // ------------------------------
            if (updatedRows == 1) {
                gcsStorageService.uploadPdf(Files.readAllBytes(pdf.toPath()), dto.folio() + ".pdf");
                return ResponseEntity.ok("Pago completado correctamente");
            }

            // ------------------------------
            // PROBLEM: More than 1 row updated (should NEVER happen)
            // ------------------------------
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: Se actualizaron múltiples registros. Verificar clave compuesta.");

        
    }

    private ResponseEntity<String> montoModificado(AlumnoAsistenciaDTO dto) {
        System.out.println("Procesando pago con monto modificado: " + dto);

        // 1. Generate PDF with modified amount
        log.info("Generating PDF for modified pago: {}", dto);
        File pdf;
        try {
            pdf = pdfService.generateReciboPDFxyz99ModifiedAmount(dto);
            // 2. Print PDF
            log.info("Imprimiendo recibo modificado para folio: {}", dto.folio());
            pdfService.printPDF(pdf);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            
        

        int comparacion = dto.montoModificado().compareTo(dto.monto());

        BigDecimal[] resultado = dto.montoModificado().divideAndRemainder(dto.monto());

        BigDecimal veces = resultado[0]; // 3
        BigDecimal restante = resultado[1]; // 150

        int vecesActualizar = 1;
        if (comparacion > 0) {// monto modificado es mayor A 200 ES 750
            log.info("Monto modificado es mayor al original por : {} veces y sobra {}",veces, restante);
            if (restante.compareTo(BigDecimal.ZERO) == 0) {
                vecesActualizar = veces.intValue();
            } else {
                vecesActualizar = veces.intValue() + 1;
            }
            int updatedRows = 0;
            int numeroSemana = dto.numeroSemana();
            double montoModificado = dto.montoModificado().doubleValue();
            for (int i = 0; i < vecesActualizar; i++) {
                try {
                    // 3. Update payment in database

                    if (i == 0) {
                        updatedRows = reciboServices.markAsPaid(dto);
                        numeroSemana++;
                        montoModificado -= dto.monto().doubleValue();
                        log.info("Primera actualización: updatedRows={}, siguiente numeroSemana={}, montoRestante={}",
                                updatedRows, numeroSemana, montoModificado);

                    } else if (i == (vecesActualizar - 1)) {
                        if (montoModificado < dto.monto().doubleValue()) {
                            
                            updatedRows = reciboServices.markAsPaidModified(
                                    dto.idGrupoAlumno().longValue(),
                                    numeroSemana,
                                    montoModificado,
                                    dto.folio().toString(),
                                    "P");
                            log.info("Última actualización con monto restante menor al original: montoPendiente={}",
                                    montoModificado-dto.monto().doubleValue());
                        } else {
                            updatedRows = reciboServices.markAsPaidModified(
                                    dto.idGrupoAlumno().longValue(),
                                    numeroSemana,
                                    dto.monto().doubleValue(),
                                    dto.folio().toString(),
                                    "P");
                            log.info("Actualización con monto  igual al original: montoOriginal={} ZEROS",
                                    dto.monto().doubleValue());
                        }

                    } else {
                        updatedRows = reciboServices.markAsPaidModified(
                                dto.idGrupoAlumno().longValue(),
                                numeroSemana,
                                dto.monto().doubleValue(),
                                dto.folio().toString(),
                                "P");
                        numeroSemana++;
                        montoModificado -= dto.monto().doubleValue();
                        log.info("Actualización intermedia: updatedRows={}, siguiente numeroSemana={}, montoRestante={}",
                                updatedRows, numeroSemana, montoModificado);
                    }

                    // ------------------------------
                    // SUCCESS CASE
                    // ------------------------------
                    /*
                    * if (updatedRows == 1) {
                    * gcsStorageService.uploadPdf(Files.readAllBytes(pdf.toPath()), dto.folio() +
                    * ".pdf");
                    * 
                    * }
                    */

                    // ------------------------------
                    // PROBLEM: No row updated
                    // ------------------------------
                    if (updatedRows == 0) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("No se encontró el registro de asistencia para actualizar (idGrupoAlumno="
                                        + dto.idGrupoAlumno() + ", numeroSemana=" + dto.numeroSemana() + ").");
                    }
                } catch (Exception e) {
                    log.error("Error al procesar el pago o imprimir el recibo para alumno {}: {}", dto.idAlumno(),
                            e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al procesar el pago o imprimir el recibo.");
                }
            }
        }else{//CASE: When modified amount is less than or equal to original amount, we just update once with the modified amount
            log.info("----------------------------------------------------------");
            log.info("Monto modificado es MENOR al original {}  y falta {}",dto.monto().doubleValue(), (dto.monto().doubleValue() - dto.montoModificado().doubleValue()));
            log.info("----------------------------------------------------------");
            try {
                int updatedRows = reciboServices.markAsPaidModified(
                        dto.idGrupoAlumno().longValue(),
                        dto.numeroSemana(),
                        dto.montoModificado().doubleValue(),
                        dto.folio().toString(),
                        "P");
                log.info("Actualización con monto modificado menor o igual al original: montoModificado={}", dto.montoModificado());

                
                // ------------------------------
                // PROBLEM: No row updated
                // ------------------------------
                if (updatedRows == 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No se encontró el registro de asistencia para actualizar (idGrupoAlumno="
                                    + dto.idGrupoAlumno() + ", numeroSemana=" + dto.numeroSemana() + ").");
                }
            } catch (Exception e) {
                log.error("Error al procesar el pago o imprimir el recibo para alumno {}: {}", dto.idAlumno(),
                        e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al procesar el pago o imprimir el recibo.");
            }

        }
        
        return ResponseEntity.ok("Pago completado correctamente");
    }

}
