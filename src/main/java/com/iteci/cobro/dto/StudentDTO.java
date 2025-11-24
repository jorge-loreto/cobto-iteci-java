// StudentDTO.java
package com.iteci.cobro.dto;

import com.iteci.cobro.entities.Student;

public record StudentDTO(Long id, String fullName) {

    // Static mapper method
    public static StudentDTO from(Student s) {
        return new StudentDTO(
            s.getIdAlumnos(),
            s.getApellidoPaterno() + " " + s.getApellidoMaterno() + ", " + s.getNombre()
        );
    }
}
