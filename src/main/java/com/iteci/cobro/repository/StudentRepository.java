package com.iteci.cobro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iteci.cobro.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // custom query example
    @Query("SELECT s FROM Student s WHERE s.telefono = :email")
    Student findByEmail(@Param("email") String email);


    List<Student> findAll();

}

