package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByAppointment(Appointment appointment);

    List<Prescription> findByDoctorIdOrderByIdDesc(Long doctorId);

    // createdDate üzerinden sorgulama
    List<Prescription> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM Prescription p WHERE p.doctor = :doctor " +
            "AND p.createdDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.createdDate DESC")
    List<Prescription> findByDoctorAndDateRange(
            @Param("doctor") Doctor doctor,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
            "ORDER BY p.createdDate DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
            "ORDER BY p.createdDate DESC")
    List<Prescription> findRecentByPatientId(@Param("patientId") Long patientId);
}