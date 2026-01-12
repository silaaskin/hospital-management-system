package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Hastaya göre tüm randevuları listele
    List<Appointment> findByPatient(Patient patient);

    // Doktora göre tüm randevuları listele
    List<Appointment> findByDoctor(Doctor doctor);

    // Duruma göre randevuları listele
    List<Appointment> findByStatus(AppointmentStatus status);

    // Hastaya ve duruma göre randevuları listele
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    // Doktora ve duruma göre randevuları listele
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);

    // Belirli tarih aralığındaki randevuları listele
    List<Appointment> findByAppointmentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // GÜNCELLEME: Çakışma kontrolü için doktorun iki tarih arasındaki bekleyen randevularını bulur
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
            "AND a.status = 'SCHEDULED'")
    List<Appointment> findDoctorAppointmentsByDateAndStatus(
            @Param("doctor") Doctor doctor,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") AppointmentStatus status);

    // Hastanın gelecek randevularını listele
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient " +
            "AND a.appointmentDate > :currentDate " +
            "AND a.status = 'SCHEDULED' " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByPatient(
            @Param("patient") Patient patient,
            @Param("currentDate") LocalDateTime currentDate);

    // Doktorun gelecek randevularını listele
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate > :currentDate " +
            "AND a.status = 'SCHEDULED' " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByDoctor(
            @Param("doctor") Doctor doctor,
            @Param("currentDate") LocalDateTime currentDate);

    // YENİ: Doktorun belirli bir zaman aralığındaki (Örn: 30 dk) herhangi bir randevusunu kontrol eder
    List<Appointment> findByDoctorAndAppointmentDateBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    // Sadece bekleyen (SCHEDULED) randevuları tarihe göre sıralı getir
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    // Belirli bir tarihteki randevuları getir
    List<Appointment> findByAppointmentDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, AppointmentStatus status);

    // --- PROCEDURE YETENEKLERİ ---

    // Bir hastanın bekleyen randevu sayısını döndüren Procedure
    @Procedure(procedureName = "GetPatientUpcomingAppointmentCount")
    Integer getPatientUpcomingAppointmentCount(@Param("p_tc") String tcNo);

    // Bir hastanın tüm randevu detaylarını listeleyen Procedure
    @Procedure(procedureName = "GetPatientAppointments")
    List<Object[]> getPatientAppointments(@Param("p_patient_id") Long patientId);
}