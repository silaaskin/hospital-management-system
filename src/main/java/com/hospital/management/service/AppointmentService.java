package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    // --- Bugün/Yarın Filtreleme İçin Kullanılan Metot ---
    public List<Appointment> getAppointmentsInDateRange(LocalDateTime start, LocalDateTime end) {
        // Sadece bekleyen (SCHEDULED) randevuları getirir
        return appointmentRepository.findByAppointmentDateBetweenAndStatus(start, end, AppointmentStatus.SCHEDULED);
    }

    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        return appointmentRepository.findUpcomingAppointmentsByPatient(patient, LocalDateTime.now());
    }

    // Randevu Oluşturma ve Çakışma Kontrolü
    public Appointment createAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Geçmiş tarih için randevu oluşturamazsınız!");
        }

        Doctor doctor = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

        // 30 dakikalık çakışma kontrolü
        LocalDateTime startRange = appointmentDate.minusMinutes(29);
        LocalDateTime endRange = appointmentDate.plusMinutes(29);

        List<Appointment> conflicts = appointmentRepository.findDoctorAppointmentsByDateAndStatus(
                doctor, startRange, endRange, AppointmentStatus.SCHEDULED);

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Seçilen saatte doktorun başka bir randevusu bulunmaktadır. Lütfen en az 30 dakika sonrasını deneyin.");
        }

        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));

        Appointment appointment = new Appointment(patient, doctor, appointmentDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Long id, Appointment details) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));

        appointment.setAppointmentDate(details.getAppointmentDate());
        appointment.setStatus(details.getStatus());
        return appointmentRepository.save(appointment);
    }

    public Appointment cancelAppointment(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        app.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(app);
    }

    public Appointment completeAppointment(Long id) {
        Appointment app = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
        app.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(app);
    }

    public Appointment saveAppointment(Appointment appointment) {
        // Kaydetmeden önce tarih çakışma kontrolü eklemek isterseniz buraya da createAppointment'taki mantığı ekleyebiliriz.
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
}