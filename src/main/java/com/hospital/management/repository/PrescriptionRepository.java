package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Randevuya göre reçete bul
    Optional<Prescription> findByAppointment(Appointment appointment);

    // Doktora göre reçeteleri listele (Doktor bazlı filtreleme için)
    List<Prescription> findByDoctorIdOrderByIdDesc(Long doctorId);

    // Belirli tarih aralığındaki reçeteleri listele
    List<Prescription> findByPrescriptionDateBetween(LocalDate startDate, LocalDate endDate);

    // Doktorun belirli tarihteki reçetelerini listele
    @Query("SELECT p FROM Prescription p WHERE p.doctor = :doctor " +
            "AND p.prescriptionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDoctorAndDateRange(
            @Param("doctor") Doctor doctor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Hastanın tüm reçetelerini listele (Triaj ve Randevu dahil)
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
            "ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);

    // Hastanın son reçetelerini getir
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
            "ORDER BY p.prescriptionDate DESC")
    List<Prescription> findRecentByPatientId(@Param("patientId") Long patientId);
}