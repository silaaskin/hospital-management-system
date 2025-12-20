package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Hastaya göre randevuları listele
    List<Appointment> findByPatient(Patient patient);

    // Doktora göre randevuları listele
    List<Appointment> findByDoctor(Doctor doctor);

    // Duruma göre randevuları listele
    List<Appointment> findByStatus(AppointmentStatus status);

    // Hastaya ve duruma göre randevuları listele
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    // Doktora ve duruma göre randevuları listele
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);

    // Belirli tarih aralığındaki randevuları listele
    List<Appointment> findByAppointmentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Doktorun belirli tarihteki randevularını listele
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
            "AND a.status = :status")
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
}